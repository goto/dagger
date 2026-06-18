package com.gotocompany.dagger.core.processors.longbow.processor;

import com.gotocompany.dagger.core.metrics.aspects.LongbowReaderAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporterFactory;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryPublisher;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryTypes;
import com.gotocompany.dagger.core.processors.longbow.exceptions.LongbowReaderException;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;
import com.gotocompany.dagger.core.processors.longbow.data.LongbowData;
import com.gotocompany.dagger.core.processors.longbow.outputRow.ReaderOutputRow;
import com.gotocompany.dagger.core.processors.longbow.range.LongbowRange;
import com.gotocompany.dagger.core.processors.longbow.request.ScanRequestFactory;
import com.gotocompany.dagger.core.processors.longbow.storage.LongbowStore;
import com.gotocompany.dagger.core.processors.longbow.storage.ScanRequest;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static java.time.Duration.between;

/**
 * The Longbow reader.
 */
public class LongbowReader extends RichAsyncFunction<Row, Row> implements TelemetryPublisher {

    /**
     * Logger used to record scan failures and timeout events for this reader.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LongbowReader.class.getName());
    /**
     * Dagger configuration used to lazily build the BigTable-backed {@link LongbowStore}.
     */
    private Configuration configuration;
    /**
     * Schema describing the Longbow columns, keys and durations for the current job.
     */
    private LongbowSchema longBowSchema;
    /**
     * Strategy that derives the scan key range (lower and upper bounds) from each input row.
     */
    private LongbowRange longbowRange;
    /**
     * Client that issues asynchronous scans against the BigTable storage.
     */
    private LongbowStore longBowStore;
    /**
     * Manager used to emit meters and histograms for reader instrumentation.
     */
    private MeterStatsManager meterStatsManager;
    /**
     * Telemetry collected for this processor, keyed by telemetry type.
     */
    private Map<String, List<String>> metrics = new HashMap<>();
    /**
     * Reporter used to surface fatal and non-fatal reader exceptions.
     */
    private ErrorReporter errorReporter;
    /**
     * Strategy that parses raw BigTable scan results into output column values.
     */
    private LongbowData longbowData;
    /**
     * Factory that builds the appropriate {@link ScanRequest} for each input row.
     */
    private ScanRequestFactory scanRequestFactory;
    /**
     * Strategy that assembles the emitted row from the parsed scan result and input.
     */
    private ReaderOutputRow readerOutputRow;

    /**
     * Instantiates a new Longbow reader with specified longbow store.
     *
     * @param configuration      the configuration
     * @param longBowSchema      the longbow schema
     * @param longbowRange       the longbow range
     * @param longBowStore       the longbow store
     * @param meterStatsManager  the meter stats manager
     * @param errorReporter      the error reporter
     * @param longbowData        the longbow data
     * @param scanRequestFactory the scan request factory
     * @param readerOutputRow    the reader output row
     */
    LongbowReader(Configuration configuration, LongbowSchema longBowSchema, LongbowRange longbowRange, LongbowStore longBowStore, MeterStatsManager meterStatsManager, ErrorReporter errorReporter, LongbowData longbowData, ScanRequestFactory scanRequestFactory, ReaderOutputRow readerOutputRow) {
        this(configuration, longBowSchema, longbowRange, longbowData, scanRequestFactory, readerOutputRow);
        this.longBowStore = longBowStore;
        this.meterStatsManager = meterStatsManager;
        this.errorReporter = errorReporter;
    }

    /**
     * Instantiates a new Longbow reader.
     *
     * @param configuration      the configuration
     * @param longBowSchema      the longbow schema
     * @param longbowRange       the longbow range
     * @param longbowData        the longbow data
     * @param scanRequestFactory the scan request factory
     * @param readerOutputRow    the reader output row
     */
    public LongbowReader(Configuration configuration, LongbowSchema longBowSchema, LongbowRange longbowRange, LongbowData longbowData, ScanRequestFactory scanRequestFactory, ReaderOutputRow readerOutputRow) {
        this.configuration = configuration;
        this.longBowSchema = longBowSchema;
        this.longbowRange = longbowRange;
        this.longbowData = longbowData;
        this.scanRequestFactory = scanRequestFactory;
        this.readerOutputRow = readerOutputRow;
    }


    /**
     * {@inheritDoc}
     *
     * <p>Lazily initialises the BigTable {@link LongbowStore}, the {@link MeterStatsManager} and the
     * {@link ErrorReporter} when they were not injected, then registers the reader meter group so
     * that {@link LongbowReaderAspects} can be reported.
     *
     * @param internalFlinkConfig the Flink runtime configuration supplied when the function opens
     * @throws Exception if the underlying store or metric resources cannot be initialised
     */
    @Override
    public void open(org.apache.flink.configuration.Configuration internalFlinkConfig) throws Exception {
        super.open(internalFlinkConfig);
        if (longBowStore == null) {
            longBowStore = LongbowStore.create(configuration);
        }
        if (meterStatsManager == null) {
            meterStatsManager = new MeterStatsManager(getRuntimeContext().getMetricGroup(), true);
        }
        if (errorReporter == null) {
            errorReporter = ErrorReporterFactory.getErrorReporter(getRuntimeContext().getMetricGroup(), configuration);
        }
        meterStatsManager.register("longbow.reader", LongbowReaderAspects.values());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Registers the post-processor type telemetry for the Longbow reader before subscribers are
     * notified.
     */
    @Override
    public void preProcessBeforeNotifyingSubscriber() {
        addMetric(TelemetryTypes.POST_PROCESSOR_TYPE.getValue(), Constants.LONGBOW_READER_PROCESSOR_KEY);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Marks the close-connection event, logs the closure and releases the underlying
     * {@link LongbowStore} if it was opened.
     *
     * @throws Exception if closing the parent function or the store fails
     */
    @Override
    public void close() throws Exception {
        super.close();
        meterStatsManager.markEvent(LongbowReaderAspects.CLOSE_CONNECTION_ON_READER);
        LOGGER.error("LongbowReader : Connection closed");
        if (longBowStore != null) {
            longBowStore.close();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds a {@link ScanRequest} for the input row, scans BigTable asynchronously and, once the
     * scan completes, records instrumentation, parses the scanned data and completes the
     * {@code resultFuture} with the assembled output row. Scan failures are logged and converted into
     * an empty result.
     *
     * @param input        the input row to enrich with Longbow data
     * @param resultFuture the future completed with the single enriched output row
     */
    @Override
    public void asyncInvoke(Row input, ResultFuture<Row> resultFuture) {
        ScanRequest scanRequest = scanRequestFactory.create(input, longbowRange);
        Instant startTime = Instant.now();
        longBowStore.scanAll(scanRequest)
                .exceptionally(throwable -> logException(throwable, startTime))
                .thenAccept(scanResult -> {
                    instrumentation(scanResult, startTime, input);
                    Row row = readerOutputRow.get(longbowData.parse(scanResult), input);
                    resultFuture.complete(Collections.singletonList(row));
                });
    }

    /**
     * Gets longbow range.
     *
     * @return the longbow range
     */
    public LongbowRange getLongbowRange() {
        return longbowRange;
    }

    /**
     * Records reader success metrics for a completed scan.
     *
     * <p>Marks the read-success event, updates the response-time and documents-per-scan histograms,
     * and flags a failure-to-read-last-record event when the scan is empty or its first row key does
     * not match the expected Longbow key for {@code input}.
     *
     * @param scanResult the list of BigTable results returned by the scan
     * @param startTime  the instant the scan started, used to compute the response time
     * @param input      the input row whose Longbow key is validated against the result
     */
    private void instrumentation(List<Result> scanResult, Instant startTime, Row input) {
        meterStatsManager.markEvent(LongbowReaderAspects.SUCCESS_ON_READ_DOCUMENT);
        meterStatsManager.updateHistogram(LongbowReaderAspects.SUCCESS_ON_READ_DOCUMENT_RESPONSE_TIME, between(startTime, Instant.now()).toMillis());
        meterStatsManager.updateHistogram(LongbowReaderAspects.DOCUMENTS_READ_PER_SCAN, scanResult.size());
        if (scanResult.isEmpty() || !Arrays.equals(scanResult.get(0).getRow(), longBowSchema.getKey(input, 0))) {
            meterStatsManager.markEvent(LongbowReaderAspects.FAILED_TO_READ_LAST_RECORD);
        }
    }

    /**
     * Handles a failed BigTable scan by logging and reporting it.
     *
     * <p>Logs the error, marks the read-failure event, reports a non-fatal
     * {@code LongbowReaderException}, records the failure response time and yields an empty result so
     * that the asynchronous pipeline can continue.
     *
     * @param ex        the throwable raised while scanning BigTable
     * @param startTime the instant the scan started, used to compute the response time
     * @return an empty list of results, used as the fallback scan outcome
     */
    private List<Result> logException(Throwable ex, Instant startTime) {
        LOGGER.error("LongbowReader : failed to scan document from BigTable: {}", ex.getMessage());
        ex.printStackTrace();
        meterStatsManager.markEvent(LongbowReaderAspects.FAILED_ON_READ_DOCUMENT);
        errorReporter.reportNonFatalException(new LongbowReaderException(ex));
        meterStatsManager.updateHistogram(LongbowReaderAspects.FAILED_ON_READ_DOCUMENT_RESPONSE_TIME, between(startTime, Instant.now()).toMillis());
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Marks a reader timeout event, reports a fatal {@link TimeoutException} and completes the
     * {@code resultFuture} exceptionally.
     *
     * @param input        the input row whose asynchronous lookup timed out
     * @param resultFuture the future completed exceptionally with the timeout error
     */
    @Override
    public void timeout(Row input, ResultFuture<Row> resultFuture) {
        LOGGER.error("LongbowReader : timeout when reading document");
        meterStatsManager.markEvent(LongbowReaderAspects.TIMEOUTS_ON_READER);
        Exception timeoutException = new TimeoutException("Async function call has timed out.");
        errorReporter.reportFatalException(timeoutException);
        resultFuture.completeExceptionally(timeoutException);
    }

    /**
     * {@inheritDoc}
     *
     * @return the telemetry collected by this reader, keyed by telemetry type
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
