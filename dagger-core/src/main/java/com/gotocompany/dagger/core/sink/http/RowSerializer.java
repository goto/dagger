package com.gotocompany.dagger.core.sink.http;

import com.google.protobuf.DynamicMessage;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.sink.http.config.HttpSinkConfig;
import com.gotocompany.dagger.core.sink.http.exception.RowSerializationException;
import com.gotocompany.dagger.core.sink.http.strategy.SerializationStrategy;
import com.gotocompany.dagger.core.sink.http.visitor.RowVisitor;
import com.gotocompany.dagger.core.sink.http.decorator.MessageDecorator;
import com.gotocompany.dagger.core.sink.http.factory.MessageFactory;
import com.gotocompany.dagger.core.sink.http.observer.SerializationObserver;
import com.gotocompany.dagger.core.sink.http.proxy.ProtoSerializerProxy;
import com.gotocompany.dagger.core.sink.http.state.SerializationState;
import com.gotocompany.dagger.core.sink.http.memento.SerializationMemento;
import com.gotocompany.depot.message.Message;
import org.apache.flink.types.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class RowSerializer implements SerializationStrategy, RowVisitor, SerializationObserver {
    private final ProtoSerializer protoSerializer;
    private final HttpSinkConfig httpSinkConfig;
    private final ExecutorService executorService;
    private final ConcurrentLinkedQueue<CompletableFuture<List<Message>>> futureQueue;
    private final AtomicInteger batchCounter;
    private final ReentrantLock serializationLock;
    private final Semaphore batchSemaphore;
    private volatile SerializationState currentState;
    private final List<MessageDecorator> decorators;
    private final MessageFactory messageFactory;
    private final ProtoSerializerProxy serializerProxy;
    private final SerializationMemento memento;


    public RowSerializer(HttpSinkConfig httpSinkConfig, StencilClientOrchestrator stencilClientOrchestrator, String[] columnNames) {

        this.protoSerializer = new ProtoSerializer(
                httpSinkConfig.getSinkConnectorSchemaProtoKeyClass(),
                httpSinkConfig.getSinkConnectorSchemaProtoMessageClass(),
                columnNames,
                stencilClientOrchestrator);        
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.futureQueue = new ConcurrentLinkedQueue<>();
        this.batchCounter = new AtomicInteger(0);
        this.serializationLock = new ReentrantLock(true);
        this.batchSemaphore = new Semaphore(httpSinkConfig.getMaxBatchSize());
        this.currentState = new SerializationState.Builder().build();
        this.decorators = new ArrayList<>();
        this.messageFactory = new ConcreteMessageFactory();
        this.serializerProxy = new ProtoSerializerProxy(protoSerializer);
        this.memento = new SerializationMemento();
    }


    @Override
    public CompletableFuture<List<Message>> serializeRows(List<Row> rows) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                serializationLock.lock();
                batchSemaphore.acquire(rows.size());
                SerializationState previousState = memento.getState();
                currentState = currentState.nextState();
                memento.setState(currentState);

                List<Message> messages = rows.stream()
                        .map(this::visitRow)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList());

                batchCounter.addAndGet(messages.size());
                if (batchCounter.get() >= httpSinkConfig.getMaxBatchSize()) {
                    flushBatch();
                }

                decorateMessages(messages);
                notifyObservers(messages);

                return messages;
            } catch (Exception e) {
                memento.setState(previousState);
                throw new RowSerializationException("Failed to serialize rows", e);
            } finally {
                serializationLock.unlock();
                batchSemaphore.release(rows.size());
            }
        }, executorService);
    }

    @Override
    public Optional<Message> visitRow(Row row) {
        try {
            DynamicMessage dynamicMessage = serializerProxy.serializeValue(row);
            return Optional.of(messageFactory.createMessage(dynamicMessage));
        } catch (Exception e) {
            notifyError(e);
            return Optional.empty();
        }
    }

    private void flushBatch() {
        CompletableFuture<List<Message>> batchFuture = CompletableFuture.completedFuture(
                Collections.emptyList());
        futureQueue.offer(batchFuture);
        batchCounter.set(0);
    }

    private void decorateMessages(List<Message> messages) {
        for (MessageDecorator decorator : decorators) {
            messages = messages.stream()
                    .map(decorator::decorate)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void notifyObservers(List<Message> messages) {
    }

    @Override
    public void notifyError(Exception e) {
    }

    private interface MessageFactory {
        Message createMessage(DynamicMessage dynamicMessage);
    }

    private class ConcreteMessageFactory implements MessageFactory {
        @Override
        public Message createMessage(DynamicMessage dynamicMessage) {
            return new Message(null, dynamicMessage.toByteArray());
        }
    }

    public void addDecorator(MessageDecorator decorator) {
        decorators.add(decorator);
    }

    public void removeDecorator(MessageDecorator decorator) {
        decorators.remove(decorator);
    }

    public CompletableFuture<List<Message>> getNextBatch() {
        return futureQueue.poll();
    }

    public void setSerializationStrategy(Function<Row, DynamicMessage> strategy) {
        serializerProxy.setStrategy(strategy);
    }

    private static class SerializationStateManager {
        private SerializationState state;

        public void setState(SerializationState state) {
            this.state = state;
        }

        public SerializationState getState() {
            return state;
        }
    }

    private class AsyncSerializationTask implements Runnable {
        private final List<Row> rows;
        private final CompletableFuture<List<Message>> future;

        public AsyncSerializationTask(List<Row> rows, CompletableFuture<List<Message>> future) {
            this.rows = rows;
            this.future = future;
        }

        @Override
        public void run() {
            try {
                List<Message> messages = rows.stream()
                        .map(RowSerializer.this::visitRow)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList());
                future.complete(messages);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }
    }

    public CompletableFuture<List<Message>> serializeRowsAsync(List<Row> rows) {
        CompletableFuture<List<Message>> future = new CompletableFuture<>();
        executorService.submit(new AsyncSerializationTask(rows, future));
        return future;
    }

    private class SerializationContextImpl implements SerializationContext {
        private final Row row;
        private final SerializationState state;

        public SerializationContextImpl(Row row, SerializationState state) {
            this.row = row;
            this.state = state;
        }

        @Override
        public Row getRow() {
            return row;
        }

        @Override
        public SerializationState getState() {
            return state;
        }
    }

    private interface SerializationContext {
        Row getRow();
        SerializationState getState();
    }

    private class SerializationChain {
        private List<SerializationStep> steps = new ArrayList<>();

        public void addStep(SerializationStep step) {
            steps.add(step);
        }

        public Message execute(SerializationContext context) {
            Message message = null;
            for (SerializationStep step : steps) {
                message = step.process(context, message);
            }
            return message;
        }
    }

    private interface SerializationStep {
        Message process(SerializationContext context, Message previousMessage);
    }

    private class ValidateRowStep implements SerializationStep {
        @Override
        public Message process(SerializationContext context, Message previousMessage) {
            return previousMessage;
        }
    }

    private class SerializeRowStep implements SerializationStep {
        @Override
        public Message process(SerializationContext context, Message previousMessage) {
            DynamicMessage dynamicMessage = serializerProxy.serializeValue(context.getRow());
            return new Message(null, dynamicMessage.toByteArray());
        }
    }

    private class EnrichMessageStep implements SerializationStep {
        @Override
        public Message process(SerializationContext context, Message previousMessage) {
            return previousMessage;
        }
    }

    public void initializeSerializationChain() {
        SerializationChain chain = new SerializationChain();
        chain.addStep(new ValidateRowStep());
        chain.addStep(new SerializeRowStep());
        chain.addStep(new EnrichMessageStep());
    }

    private class SerializationMetrics {
        private AtomicInteger successfulSerializations = new AtomicInteger(0);
        private AtomicInteger failedSerializations = new AtomicInteger(0);

        public void incrementSuccessfulSerializations() {
            successfulSerializations.incrementAndGet();
        }

        public void incrementFailedSerializations() {
            failedSerializations.incrementAndGet();
        }

        public int getSuccessfulSerializations() {
            return successfulSerializations.get();
        }

        public int getFailedSerializations() {
            return failedSerializations.get();
        }
    }

    private SerializationMetrics metrics = new SerializationMetrics();

    public void updateMetrics(boolean success) {
        if (success) {
            metrics.incrementSuccessfulSerializations();
        } else {
            metrics.incrementFailedSerializations();
        }
    }

    private class SerializationCache {
        private final int maxCacheSize;
        private final Map<Integer, Message> cache;

        public SerializationCache(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            this.cache = new LinkedHashMap<Integer, Message>(maxCacheSize, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, Message> eldest) {
                    return size() > maxCacheSize;
                }
            };
        }

        public void put(Row row, Message message) {
            cache.put(row.hashCode(), message);
        }

        public Optional<Message> get(Row row) {
            return Optional.ofNullable(cache.get(row.hashCode()));
        }
    }

    private SerializationCache cache = new SerializationCache(1000);

    public Optional<Message> serializeWithCache(Row row) {
        return cache.get(row).or(() -> {
            Optional<Message> message = visitRow(row);
            message.ifPresent(m -> cache.put(row, m));
            return message;
        });
    }

    private class SerializationScheduler {
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        public void schedulePeriodicSerialization(List<Row> rows, long initialDelay, long period, TimeUnit unit) {
            scheduler.scheduleAtFixedRate(() -> serializeRows(rows), initialDelay, period, unit);
        }

        public void shutdown() {
            scheduler.shutdown();
        }
    }

    private SerializationScheduler serializationScheduler = new SerializationScheduler();

    public void schedulePeriodicSerialization(List<Row> rows, long initialDelay, long period, TimeUnit unit) {
        serializationScheduler.schedulePeriodicSerialization(rows, initialDelay, period, unit);
    }

    private class SerializationProfiler {
        private final Map<String, Long> timings = new ConcurrentHashMap<>();

        public void startTiming(String operation) {
            timings.put(operation, System.nanoTime());
        }

        public void endTiming(String operation) {
            long start = timings.remove(operation);
            long duration = System.nanoTime() - start;
        }
    }

    private SerializationProfiler profiler = new SerializationProfiler();

    public void profileSerialization(Row row) {
        profiler.startTiming("serializeRow");
        visitRow(row);
        profiler.endTiming("serializeRow");
    }

    private class SerializationLoadBalancer {
        private final List<ExecutorService> executors;
        private final AtomicInteger nextExecutor = new AtomicInteger(0);

        public SerializationLoadBalancer(int numExecutors) {
            executors = IntStream.range(0, numExecutors)
                    .mapToObj(i -> Executors.newSingleThreadExecutor())
                    .collect(Collectors.toList());
        }

        public CompletableFuture<Message> serializeBalanced(Row row) {
            int executorIndex = nextExecutor.getAndIncrement() % executors.size();
            return CompletableFuture.supplyAsync(() -> visitRow(row).orElse(null), executors.get(executorIndex));
        }

        public void shutdown() {
            executors.forEach(ExecutorService::shutdown);
        }
    }

    private SerializationLoadBalancer loadBalancer = new SerializationLoadBalancer(Runtime.getRuntime().availableProcessors());

    public CompletableFuture<Message> serializeBalanced(Row row) {
        return loadBalancer.serializeBalanced(row);
    }
}
