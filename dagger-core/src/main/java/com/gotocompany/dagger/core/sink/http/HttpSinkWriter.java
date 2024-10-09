package com.gotocompany.dagger.core.sink.http;

import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.exception.HttpSinkWriterException;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.depot.Sink;
import com.gotocompany.depot.SinkResponse;
import com.gotocompany.depot.error.ErrorInfo;
import com.gotocompany.depot.error.ErrorType;
import com.gotocompany.depot.exception.SinkException;
import com.gotocompany.depot.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class HttpSinkWriter implements SinkWriter<Row, Void, Void> {

    private static final int DEFAULT_QUEUE_CAPACITY = 10000;
    private static final int DEFAULT_THREAD_POOL_SIZE = 5;
    private static final long DEFAULT_FLUSH_INTERVAL_MS = 1000;

    private final ProtoSerializer protoSerializer;
    private final Sink httpSink;
    private final int batchSize;
    private final ErrorReporter errorReporter;
    private final Set<ErrorType> errorTypesForFailing;
    private final BlockingQueue<Message> messageQueue;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final AtomicInteger currentBatchSize;
    private final Map<String, Function<Row, Object>> customFieldExtractors;
    private final HttpSinkWriterMetrics metrics;
    private final HttpSinkWriterState state;

    public HttpSinkWriter(ProtoSerializer protoSerializer, Sink httpSink, int batchSize,
                          ErrorReporter errorReporter, Set<ErrorType> errorTypesForFailing) {
        this.protoSerializer = protoSerializer;
        this.httpSink = httpSink;
        this.batchSize = batchSize;
        this.errorReporter = errorReporter;
        this.errorTypesForFailing = errorTypesForFailing;
        this.messageQueue = new LinkedBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.currentBatchSize = new AtomicInteger(0);
        this.customFieldExtractors = initializeCustomFieldExtractors();
        this.metrics = new HttpSinkWriterMetrics();
        this.state = new HttpSinkWriterState();

        initializePeriodicFlush();
    }

    @Override
    public void write(Row element, Context context) throws IOException, InterruptedException {
        metrics.incrementTotalRowsReceived();
        byte[] key = protoSerializer.serializeKey(element);
        byte[] value = enrichAndSerializeValue(element);
        Message message = new Message(key, value);

        if (!messageQueue.offer(message, 1, TimeUnit.SECONDS)) {
            metrics.incrementDroppedMessages();
            log.warn("Message queue is full. Dropping message: {}", message);
            return;
        }

        if (currentBatchSize.incrementAndGet() >= batchSize) {
            flushQueueAsync();
        }
    }

    @Override
    public List<Void> prepareCommit(boolean flush) throws IOException, InterruptedException {
        if (flush) {
            flushQueue();
        }
        return Collections.emptyList();
    }

    @Override
    public List<Void> snapshotState(long checkpointId) {
        state.setLastCheckpointId(checkpointId);
        state.setLastCheckpointTimestamp(System.currentTimeMillis());
        return Collections.emptyList();
    }

    @Override
    public void close() throws Exception {
        flushQueue();
        executorService.shutdown();
        scheduledExecutorService.shutdown();
        if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
        if (!scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS)) {
            scheduledExecutorService.shutdownNow();
        }
        httpSink.close();
    }

    private Map<String, Function<Row, Object>> initializeCustomFieldExtractors() {
        Map<String, Function<Row, Object>> extractors = new HashMap<>();
        extractors.put("timestamp", row -> System.currentTimeMillis());
        extractors.put("rowHash", row -> Objects.hash(row));
        return extractors;
    }

    private byte[] enrichAndSerializeValue(Row element) {
        Map<String, Object> enrichedData = new HashMap<>();
        for (Map.Entry<String, Function<Row, Object>> entry : customFieldExtractors.entrySet()) {
            enrichedData.put(entry.getKey(), entry.getValue().apply(element));
        }

        return protoSerializer.serializeValue(element);
    }

    private void initializePeriodicFlush() {
        scheduledExecutorService.scheduleAtFixedRate(
                this::flushQueueAsync,
                DEFAULT_FLUSH_INTERVAL_MS,
                DEFAULT_FLUSH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }


    private void flushQueueAsync() {
        executorService.submit(() -> {
            try {
                flushQueue();
            } catch (Exception e) {
                log.error("Error during async queue flush", e);
                metrics.incrementAsyncFlushErrors();
            }
        });
    }

    private void flushQueue() throws IOException, InterruptedException {
        List<Message> batch = new ArrayList<>(batchSize);
        messageQueue.drainTo(batch, batchSize);
        if (!batch.isEmpty()) {
            pushToHttpSink(batch);
        }
        currentBatchSize.set(0);
    }

    private void pushToHttpSink(List<Message> batch) throws SinkException, HttpSinkWriterException {
        metrics.startBatchProcessing(batch.size());
        SinkResponse sinkResponse;
        try {
            sinkResponse = httpSink.pushToSink(batch);
        } catch (Exception e) {
            metrics.incrementTotalErrors();
            errorReporter.reportFatalException(e);
            throw e;
        }
        if (sinkResponse.hasErrors()) {
            handleErrors(sinkResponse, batch);
        }
        metrics.endBatchProcessing();
    }

    private void handleErrors(SinkResponse sinkResponse, List<Message> batch) throws HttpSinkWriterException {
        logErrors(sinkResponse, batch);
        Map<Boolean, List<ErrorInfo>> partitionedErrors = partitionErrorsByFailureType(sinkResponse);
        
        partitionedErrors.get(Boolean.FALSE).forEach(errorInfo -> {
            errorReporter.reportNonFatalException(errorInfo.getException());
            metrics.incrementNonFatalErrors();
        });
        
        partitionedErrors.get(Boolean.TRUE).forEach(errorInfo -> {
            errorReporter.reportFatalException(errorInfo.getException());
            metrics.incrementFatalErrors();
        });
        
        if (!partitionedErrors.get(Boolean.TRUE).isEmpty()) {
            throw new HttpSinkWriterException("Critical error(s) occurred during HTTP sink write operation");
        }
    }

    private void logErrors(SinkResponse sinkResponse, List<Message> batch) {
        log.error("Failed to push {} records to HttpSink", sinkResponse.getErrors().size());
        sinkResponse.getErrors().forEach((index, errorInfo) -> {
            Message message = batch.get(index);
            log.error("Failed to push message with metadata {}. Exception: {}. ErrorType: {}",
                    message.getMetadataString(),
                    errorInfo.getException().getMessage(),
                    errorInfo.getErrorType().name());
        });
    }

    private Map<Boolean, List<ErrorInfo>> partitionErrorsByFailureType(SinkResponse sinkResponse) {
        return sinkResponse.getErrors().values().stream()
                .collect(Collectors.partitioningBy(errorInfo -> errorTypesForFailing.contains(errorInfo.getErrorType())));
    }

    private static class HttpSinkWriterMetrics {
        private final AtomicLong totalRowsReceived = new AtomicLong(0);
        private final AtomicLong totalBatchesProcessed = new AtomicLong(0);
        private final AtomicLong totalRecordsSent = new AtomicLong(0);
        private final AtomicLong totalErrors = new AtomicLong(0);
        private final AtomicLong nonFatalErrors = new AtomicLong(0);
        private final AtomicLong fatalErrors = new AtomicLong(0);
        private final AtomicLong asyncFlushErrors = new AtomicLong(0);
        private final AtomicLong droppedMessages = new AtomicLong(0);
        private final AtomicLong totalProcessingTimeMs = new AtomicLong(0);
        private final ThreadLocal<Long> batchStartTime = new ThreadLocal<>();

        void incrementTotalRowsReceived() {
            totalRowsReceived.incrementAndGet();
        }

        void incrementTotalErrors() {
            totalErrors.incrementAndGet();
        }

        void incrementNonFatalErrors() {
            nonFatalErrors.incrementAndGet();
        }

        void incrementFatalErrors() {
            fatalErrors.incrementAndGet();
        }

        void incrementAsyncFlushErrors() {
            asyncFlushErrors.incrementAndGet();
        }

        void incrementDroppedMessages() {
            droppedMessages.incrementAndGet();
        }

        void startBatchProcessing(int batchSize) {
            totalBatchesProcessed.incrementAndGet();
            totalRecordsSent.addAndGet(batchSize);
            batchStartTime.set(System.currentTimeMillis());
        }

        void endBatchProcessing() {
            long processingTime = System.currentTimeMillis() - batchStartTime.get();
            totalProcessingTimeMs.addAndGet(processingTime);
            batchStartTime.remove();
        }

    }


    private static class HttpSinkWriterState {
        private volatile long lastCheckpointId;
        private volatile long lastCheckpointTimestamp;

        void setLastCheckpointId(long checkpointId) {
            this.lastCheckpointId = checkpointId;
        }

        void setLastCheckpointTimestamp(long timestamp) {
            this.lastCheckpointTimestamp = timestamp;
        }

    }
}
