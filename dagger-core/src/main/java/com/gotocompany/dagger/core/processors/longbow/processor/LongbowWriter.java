package com.gotocompany.dagger.core.processors.longbow.processor;

import com.gotocompany.dagger.core.metrics.aspects.LongbowWriterAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporterFactory;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryPublisher;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryTypes;
import com.gotocompany.dagger.core.processors.longbow.exceptions.LongbowWriterException;
import com.gotocompany.dagger.core.processors.longbow.outputRow.WriterOutputRow;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;
import com.gotocompany.dagger.core.processors.longbow.request.PutRequestFactory;
import com.gotocompany.dagger.core.processors.longbow.storage.LongbowStore;
import com.gotocompany.dagger.core.processors.longbow.storage.PutRequest;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static java.time.Duration.between;

/**
 * The Longbow writer.
 */
public class LongbowWriter extends RichAsyncFunction<Row, Row> implements TelemetryPublisher {

    /**
     * Logger used to record table-creation and write failures for this writer.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LongbowWriter.class.getName());
    /**
     * Default BigTable column family, in bytes, under which Longbow values are written.
     */
    private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT);

    /**
     * Manager used to emit meters and histograms for writer instrumentation.
     */
    private MeterStatsManager meterStatsManager;
    /**
     * Schema describing the Longbow columns, keys and document duration for the current job.
     */
    private LongbowSchema longbowSchema;
    /**
     * Dagger configuration used to read writer settings and build the {@link LongbowStore}.
     */
    private Configuration configuration;
    /**
     * Configured time-to-live for written documents, used to derive the BigTable max-age GC rule.
     */
    private String longbowDocumentDuration;
    /**
     * Factory that builds the appropriate {@link PutRequest} for each input row.
     */
    private PutRequestFactory putRequestFactory;
    /**
     * Identifier of the BigTable table this writer persists records into.
     */
    private String tableId;
    /**
     * Strategy that assembles the row emitted after a successful write.
     */
    private WriterOutputRow writerOutputRow;
    /**
     * Client that creates tables and issues asynchronous writes against BigTable.
     */
    private LongbowStore longBowStore;
    /**
     * Telemetry collected for this processor, keyed by telemetry type.
     */
    private Map<String, List<String>> metrics = new HashMap<>();
    /**
     * Reporter used to surface fatal and non-fatal writer exceptions.
     */
    private ErrorReporter errorReporter;

    /**
     * Instantiates a new Longbow writer.
     *
     * @param configuration     the configuration
     * @param longbowSchema     the longbow schema
     * @param putRequestFactory the put request factory
     * @param tableId           the table id
     * @param writerOutputRow   the writer output row
     */
    public LongbowWriter(Configuration configuration, LongbowSchema longbowSchema, PutRequestFactory putRequestFactory, String tableId, WriterOutputRow writerOutputRow) {

        this.longbowSchema = longbowSchema;
        this.longbowDocumentDuration = configuration.getString(Constants.PROCESSOR_LONGBOW_DOCUMENT_DURATION_KEY,
                Constants.PROCESSOR_LONGBOW_DOCUMENT_DURATION_DEFAULT);
        this.putRequestFactory = putRequestFactory;
        this.tableId = tableId;
        this.writerOutputRow = writerOutputRow;
        this.configuration = configuration;
    }

    /**
     * Instantiates a new Longbow writer with specified longbow store.
     *
     * @param configuration     the configuration
     * @param longBowSchema     the longbow schema
     * @param meterStatsManager the meter stats manager
     * @param errorReporter     the error reporter
     * @param longBowStore      the longbow store
     * @param putRequestFactory the put request factory
     * @param tableId           the table id
     * @param writerOutputRow   the writer output row
     */
    LongbowWriter(Configuration configuration, LongbowSchema longBowSchema, MeterStatsManager meterStatsManager,
                  ErrorReporter errorReporter, LongbowStore longBowStore, PutRequestFactory putRequestFactory, String tableId, WriterOutputRow writerOutputRow) {
        this(configuration, longBowSchema, putRequestFactory, tableId, writerOutputRow);
        this.meterStatsManager = meterStatsManager;
        this.longBowStore = longBowStore;
        this.errorReporter = errorReporter;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily initialises the {@link LongbowStore}, {@link MeterStatsManager} and
     * {@link ErrorReporter} when they were not injected, registers the writer meter group, and
     * creates the target BigTable table if it does not yet exist. Table creation applies a
     * max-versions of one together with a max-age derived from the configured document duration, and
     * is instrumented for both success and failure.
     *
     * @param internalFlinkConfig the Flink runtime configuration supplied when the function opens
     * @throws Exception if the store cannot be created or the table cannot be provisioned
     */
    @Override
    public void open(org.apache.flink.configuration.Configuration internalFlinkConfig) throws Exception {
        super.open(internalFlinkConfig);
        if (longBowStore == null) {
            longBowStore = LongbowStore.create(this.configuration);
        }

        if (meterStatsManager == null) {
            meterStatsManager = new MeterStatsManager(getRuntimeContext().getMetricGroup(), true);
        }
        meterStatsManager.register("longbow.writer", LongbowWriterAspects.values());

        if (errorReporter == null) {
            errorReporter = ErrorReporterFactory.getErrorReporter(getRuntimeContext().getMetricGroup(), configuration);
        }

        if (!longBowStore.tableExists(tableId)) {
            Instant startTime = Instant.now();
            try {
                Duration maxAgeDuration = Duration.ofMillis(longbowSchema.getDurationInMillis(longbowDocumentDuration));
                String columnFamilyName = new String(COLUMN_FAMILY_NAME);
                longBowStore.createTable(maxAgeDuration, columnFamilyName, tableId);
                LOGGER.info("table '{}' is created with maxAge '{}' on column family '{}'", tableId,
                        maxAgeDuration, columnFamilyName);
                meterStatsManager.markEvent(LongbowWriterAspects.SUCCESS_ON_CREATE_BIGTABLE);
                meterStatsManager.updateHistogram(LongbowWriterAspects.SUCCESS_ON_CREATE_BIGTABLE_RESPONSE_TIME,
                        between(startTime, Instant.now()).toMillis());
            } catch (Exception ex) {
                LOGGER.error("failed to create table '{}'", tableId);
                meterStatsManager.markEvent(LongbowWriterAspects.FAILURES_ON_CREATE_BIGTABLE);
                errorReporter.reportFatalException(ex);
                meterStatsManager.updateHistogram(LongbowWriterAspects.FAILURES_ON_CREATE_BIGTABLE_RESPONSE_TIME,
                        between(startTime, Instant.now()).toMillis());
                throw ex;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Registers the post-processor type telemetry for the Longbow writer before subscribers are
     * notified.
     */
    @Override
    public void preProcessBeforeNotifyingSubscriber() {
        addMetric(TelemetryTypes.POST_PROCESSOR_TYPE.getValue(), Constants.LONGBOW_WRITER_PROCESSOR_KEY);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds a {@link PutRequest} for the input row and writes it to BigTable asynchronously. On
     * success it records the write metrics and completes {@code resultFuture} with the row produced by
     * the configured {@link WriterOutputRow}; failures are logged and reported.
     *
     * @param input        the input row to persist into BigTable
     * @param resultFuture the future completed with the single output row once the write succeeds
     * @throws Exception if the put request cannot be created or submitted
     */
    @Override
    public void asyncInvoke(Row input, ResultFuture<Row> resultFuture) throws Exception {
        PutRequest putRequest = putRequestFactory.create(input);
        Instant startTime = Instant.now();
        CompletableFuture<Void> writeFuture = longBowStore.put(putRequest);
        writeFuture.exceptionally(throwable -> logException(throwable, startTime)).thenAccept(aVoid -> {
            meterStatsManager.markEvent(LongbowWriterAspects.SUCCESS_ON_WRITE_DOCUMENT);
            meterStatsManager.updateHistogram(LongbowWriterAspects.SUCCESS_ON_WRITE_DOCUMENT_RESPONSE_TIME,
                    between(startTime, Instant.now()).toMillis());
            resultFuture.complete(Collections.singletonList(writerOutputRow.get(input)));
        });
    }

    /**
     * Handles a failed BigTable write by logging and reporting it.
     *
     * <p>Logs the error, marks the write-failure event, reports a non-fatal
     * {@code LongbowWriterException} and records the failure response time.
     *
     * @param ex        the throwable raised while writing to BigTable
     * @param startTime the instant the write started, used to compute the response time
     * @return {@code null} always, matching the {@code Void} completion stage signature
     */
    private Void logException(Throwable ex, Instant startTime) {
        LOGGER.error("failed to write document to table '{}'", tableId);
        ex.printStackTrace();
        meterStatsManager.markEvent(LongbowWriterAspects.FAILED_ON_WRITE_DOCUMENT);
        errorReporter.reportNonFatalException(new LongbowWriterException(ex));
        meterStatsManager.updateHistogram(LongbowWriterAspects.FAILED_ON_WRITE_DOCUMENT_RESPONSE_TIME,
                between(startTime, Instant.now()).toMillis());
        return null;
    }

    /**
     * Handles an asynchronous write that exceeded its configured timeout.
     *
     * <p>Marks a writer timeout event, reports a fatal {@link TimeoutException} and completes the
     * {@code resultFuture} exceptionally.
     *
     * @param input        the input row whose asynchronous write timed out
     * @param resultFuture the future completed exceptionally with the timeout error
     * @throws Exception if reporting the timeout fails
     */
    public void timeout(Row input, ResultFuture<Row> resultFuture) throws Exception {
        LOGGER.error("LongbowWriter : timeout when writing document");
        meterStatsManager.markEvent(LongbowWriterAspects.TIMEOUTS_ON_WRITER);
        Exception timeoutException = new TimeoutException("Async function call has timed out.");
        errorReporter.reportFatalException(timeoutException);
        resultFuture.completeExceptionally(timeoutException);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Releases the underlying {@link LongbowStore} if it was opened, marks the close-connection
     * event and logs the closure.
     *
     * @throws Exception if closing the parent function or the store fails
     */
    @Override
    public void close() throws Exception {
        super.close();
        if (longBowStore != null) {
            longBowStore.close();
        }
        meterStatsManager.markEvent(LongbowWriterAspects.CLOSE_CONNECTION_ON_WRITER);
        LOGGER.error("LongbowWriter : Connection closed");
    }

    /**
     * {@inheritDoc}
     *
     * @return the telemetry collected by this writer, keyed by telemetry type
     */
    @Override
    public Map<String, List<String>> getTelemetry() {
        return metrics;
    }

    /**
     * Appends a telemetry value under the given key.
     *
     * @param key   the telemetry type key to record under
     * @param value the telemetry value to add for that key
     */
    private void addMetric(String key, String value) {
        metrics.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }
}
