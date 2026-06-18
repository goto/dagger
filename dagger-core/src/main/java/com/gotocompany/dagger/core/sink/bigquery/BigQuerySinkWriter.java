package com.gotocompany.dagger.core.sink.bigquery;

import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.exception.BigQueryWriterException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Flink {@link SinkWriter} that batches Dagger {@link Row} records and writes them to BigQuery through
 * the depot {@link Sink}.
 *
 * <p>Rows are serialized with a {@link ProtoSerializer} and buffered until the configured batch size is
 * reached, at which point the batch is flushed. Errors returned by the depot sink are reported via an
 * {@link ErrorReporter}; any error whose {@code ErrorType} is configured as fatal causes a
 * {@link BigQueryWriterException} to be thrown.
 */
@Slf4j
public class BigQuerySinkWriter implements SinkWriter<Row, Void, Void> {
    /** Serializes each {@link Row} into protobuf key and value bytes. */
    private final ProtoSerializer protoSerializer;
    /** The depot BigQuery sink that performs the actual writes. */
    private final Sink bigquerySink;
    /** Maximum number of messages buffered before a batch is flushed to BigQuery. */
    private final int batchSize;
    /** Reporter used to surface fatal and non-fatal sink errors as metrics. */
    private final ErrorReporter errorReporter;
    /** Error types that, when returned by the sink, should fail the job. */
    private final Set<ErrorType> errorTypesForFailing;
    /** Buffer of serialized messages awaiting the next flush to BigQuery. */
    private final List<Message> messages = new ArrayList<>();
    /** Number of messages currently buffered in {@link #messages}. */
    private int currentBatchSize;

    /**
     * Instantiates a new BigQuery sink writer.
     *
     * @param protoSerializer      the serializer converting rows into protobuf key and value bytes
     * @param bigquerySink         the depot sink that performs the writes
     * @param batchSize            the maximum number of messages buffered before flushing
     * @param errorReporter        the reporter for fatal and non-fatal sink errors
     * @param errorTypesForFailing the set of error types that should cause the job to fail
     */
    public BigQuerySinkWriter(ProtoSerializer protoSerializer, Sink bigquerySink, int batchSize, ErrorReporter errorReporter, Set<ErrorType> errorTypesForFailing) {
        this.protoSerializer = protoSerializer;
        this.bigquerySink = bigquerySink;
        this.batchSize = batchSize;
        this.errorReporter = errorReporter;
        this.errorTypesForFailing = errorTypesForFailing;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Serializes the given row and adds it to the buffer; once the buffer reaches the configured
     * batch size the batch is flushed to BigQuery and the buffer is reset.
     *
     * @param element the output row to write
     * @param context the sink write context
     * @throws IOException if serialization or the flush to BigQuery fails
     */
    @Override
    public void write(Row element, Context context) throws IOException {
        log.info("adding row to BQ batch : " + element);
        byte[] key = protoSerializer.serializeKey(element);
        byte[] value = protoSerializer.serializeValue(element);
        Message message = new Message(key, value);
        if (currentBatchSize < batchSize) {
            messages.add(message);
            currentBatchSize++;
        }
        if (currentBatchSize >= batchSize) {
            pushToBq();
            messages.clear();
            currentBatchSize = 0;
        }
    }

    /**
     * Pushes the currently buffered messages to the depot BigQuery sink and handles any reported errors.
     *
     * @throws SinkException           if the underlying depot sink fails while pushing the batch
     * @throws BigQueryWriterException if the response contains errors whose type is configured as fatal
     */
    private void pushToBq() throws SinkException, BigQueryWriterException {
        log.info("Pushing " + currentBatchSize + " records to bq");
        SinkResponse sinkResponse;
        try {
            sinkResponse = bigquerySink.pushToSink(messages);
        } catch (Exception e) {
            errorReporter.reportFatalException(e);
            throw e;
        }
        if (sinkResponse.hasErrors()) {
            logErrors(sinkResponse, messages);
            checkAndThrow(sinkResponse);
        }
    }

    /**
     * Partitions the errors in the sink response into fatal and non-fatal groups, reports each group to
     * the error reporter accordingly, and fails if any fatal error is present.
     *
     * @param sinkResponse the response returned by the depot sink for the last batch
     * @throws BigQueryWriterException if at least one error has a type contained in the fatal error set
     */
    protected void checkAndThrow(SinkResponse sinkResponse) throws BigQueryWriterException {
        Map<Boolean, List<ErrorInfo>> failedErrorTypes = sinkResponse.getErrors().values().stream().collect(
                Collectors.partitioningBy(errorInfo -> errorTypesForFailing.contains(errorInfo.getErrorType())));
        failedErrorTypes.get(Boolean.FALSE).forEach(errorInfo -> {
            errorReporter.reportNonFatalException(errorInfo.getException());
        });
        failedErrorTypes.get(Boolean.TRUE).forEach(errorInfo -> {
            errorReporter.reportFatalException(errorInfo.getException());
        });
        if (failedErrorTypes.get(Boolean.TRUE).size() > 0) {
            throw new BigQueryWriterException("Error occurred during writing to BigQuery");
        }
    }

    /**
     * Logs detailed information for every error in the sink response, correlating each error with the
     * message that produced it.
     *
     * @param sinkResponse the response containing the per-message errors
     * @param sentMessages the messages sent in the failed batch, indexed by their position
     */
    protected void logErrors(SinkResponse sinkResponse, List<Message> sentMessages) {
        log.error("Failed to push " + sinkResponse.getErrors().size() + " records to BigQuerySink");
        sinkResponse.getErrors().forEach((index, errorInfo) -> {
            Message message = sentMessages.get(index.intValue());
            log.error("Failed to pushed message with metadata {}. The exception was {}. The ErrorType was {}",
                    message.getMetadataString(),
                    errorInfo.getException().getMessage(),
                    errorInfo.getErrorType().name());
        });
    }

    /**
     * This will be called before we checkpoint the Writer's state in Streaming execution mode.
     *
     * @param flush – Whether flushing the un-staged data or not
     * @return The data is ready to commit.
     * @throws IOException – if fail to prepare for a commit.
     */
    @Override
    public List<Void> prepareCommit(boolean flush) throws IOException {
        pushToBq();
        messages.clear();
        currentBatchSize = 0;
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the underlying depot BigQuery sink and releases its resources.
     *
     * @throws Exception if the underlying sink fails to close
     */
    @Override
    public void close() throws Exception {
        bigquerySink.close();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This writer holds no checkpointable state, so an empty list is returned.
     *
     * @param checkpointId the id of the checkpoint being taken
     * @return an empty list
     */
    @Override
    public List<Void> snapshotState(long checkpointId) {
        // We don't snapshot anything
        return Collections.emptyList();
    }
}
