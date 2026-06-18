package com.gotocompany.dagger.core.processors.external.pg;

import com.gotocompany.dagger.core.exception.HttpFailureException;
import com.gotocompany.dagger.core.metrics.aspects.ExternalSourceAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.PostResponseTelemetry;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.google.protobuf.Descriptors;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.sqlclient.RowSet;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;
import org.elasticsearch.client.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.gotocompany.dagger.common.serde.typehandler.RowFactory.createRow;

/**
 * The Postgre response handler.
 */
public class PgResponseHandler implements Handler<AsyncResult<RowSet<io.vertx.sqlclient.Row>>> {
    /**
     * Logger used to record query failures encountered while handling the response.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PgResponseHandler.class.getName());
    /**
     * Configuration describing the output mapping, type handling and error behaviour for the query.
     */
    private final PgSourceConfig pgSourceConfig;
    /**
     * Manager used to emit success, failure and error metrics for the query.
     */
    private final MeterStatsManager meterStatsManager;
    /**
     * Manages the input and output {@link Row} so resolved values can be written back to the output row.
     */
    private final RowManager rowManager;
    /**
     * Resolves output column names to their positional indexes within the output row.
     */
    private final ColumnNameManager columnNameManager;
    /**
     * Protobuf descriptor describing the output message type, used for type-aware field conversion.
     */
    private final Descriptors.Descriptor outputDescriptor;
    /**
     * The future completed with the enriched output row once the query result has been processed.
     */
    private final ResultFuture<Row> resultFuture;
    /**
     * Reporter used to surface fatal and non-fatal exceptions raised while handling the result.
     */
    private final ErrorReporter errorReporter;
    /**
     * Helper that emits post-response telemetry such as success, failure and latency events.
     */
    private PostResponseTelemetry postResponseTelemetry;
    /**
     * The instant at which the query was dispatched, used to compute response latency telemetry.
     */
    private Instant startTime;

    /**
     * Instantiates a new Postgre response handler.
     *
     * @param pgSourceConfig        the pg source config
     * @param meterStatsManager     the meter stats manager
     * @param rowManager            the row manager
     * @param columnNameManager     the column name manager
     * @param outputDescriptor      the output descriptor
     * @param resultFuture          the result future
     * @param errorReporter         the error reporter
     * @param postResponseTelemetry the post response telemetry
     */
    public PgResponseHandler(PgSourceConfig pgSourceConfig, MeterStatsManager meterStatsManager, RowManager rowManager, ColumnNameManager columnNameManager, Descriptors.Descriptor outputDescriptor, ResultFuture<Row> resultFuture, ErrorReporter errorReporter, PostResponseTelemetry postResponseTelemetry) {

        this.pgSourceConfig = pgSourceConfig;
        this.meterStatsManager = meterStatsManager;
        this.rowManager = rowManager;
        this.columnNameManager = columnNameManager;
        this.outputDescriptor = outputDescriptor;
        this.resultFuture = resultFuture;
        this.errorReporter = errorReporter;
        this.postResponseTelemetry = postResponseTelemetry;
    }

    /**
     * Start timer.
     */
    public void startTimer() {
        startTime = Instant.now();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invoked when the asynchronous query completes. A successful result is passed to the success handler;
     * a failed result is routed to the failure handler.
     *
     * @param event the asynchronous result wrapping the query's {@code RowSet} or the failure cause
     */
    @Override
    public void handle(AsyncResult<RowSet<io.vertx.sqlclient.Row>> event) {
        if (event.succeeded()) {
            successHandler(event.result());
        } else {
            failureHandler(event.cause());
        }
    }

    /**
     * Maps a successful query result onto the output row and completes the result future.
     *
     * <p>A result set containing more than one row is treated as a configuration error. For each configured
     * output column the value is read from the mapped query parameter and written to the output row; a
     * missing column is reported as an error. Success telemetry is sent once all columns have been mapped.
     *
     * @param resultRowSet the result set returned by the query, expected to contain at most one row
     */
    private void successHandler(RowSet<io.vertx.sqlclient.Row> resultRowSet) {
        if (resultRowSet.size() > 1) {
            meterStatsManager.markEvent(ExternalSourceAspects.INVALID_CONFIGURATION);
            Exception illegalArgumentException = new IllegalArgumentException("Invalid query resulting in more than one rows. ");
            if (pgSourceConfig.isFailOnErrors()) {
                reportAndThrowError(illegalArgumentException);
            } else {
                errorReporter.reportNonFatalException(illegalArgumentException);
                resultFuture.complete(Collections.singleton(rowManager.getAll()));
            }
            return;
        }
        List<String> pgOutputColumnNames = pgSourceConfig.getOutputColumns();
        pgOutputColumnNames.forEach(outputColumnName -> {
            for (io.vertx.sqlclient.Row row : resultRowSet) {
                int outputColumnIndex = columnNameManager.getOutputIndex(outputColumnName);
                String mappedQueryParam = pgSourceConfig.getMappedQueryParam(outputColumnName);
                if (row.getColumnIndex(mappedQueryParam) == -1) {
                    Exception illegalArgumentException = new IllegalArgumentException("Invalid field " + mappedQueryParam + " is not present in the SQL. ");
                    reportAndThrowError(illegalArgumentException);
                    meterStatsManager.markEvent(ExternalSourceAspects.INVALID_CONFIGURATION);
                    return;
                } else {
                    setField(outputColumnIndex, row.getValue(mappedQueryParam), outputColumnName);
                }
            }
        });
        postResponseTelemetry.sendSuccessTelemetry(meterStatsManager, startTime);
        resultFuture.complete(Collections.singleton(rowManager.getAll()));
    }

    /**
     * Handles a failed query by emitting telemetry and reporting the error.
     *
     * <p>Sends failure telemetry and either reports a fatal error or a non-fatal exception depending on the
     * fail-on-errors setting. A {@code ResponseException} contributes its status code to telemetry, while
     * other errors are recorded as generic errors. The result future is completed with the unmodified row.
     *
     * @param e the throwable describing why the query failed
     */
    private void failureHandler(Throwable e) {
        postResponseTelemetry.sendFailureTelemetry(meterStatsManager, startTime);
        LOGGER.error(e.getMessage());
        Exception httpFailureException = new HttpFailureException("PgResponseHandler : Failed with error. " + e.getMessage());
        if (pgSourceConfig.isFailOnErrors()) {
            reportAndThrowError(httpFailureException);
        } else {
            errorReporter.reportNonFatalException(httpFailureException);
        }
        if (e instanceof ResponseException) {
            postResponseTelemetry.validateResponseCode(meterStatsManager, ((ResponseException) e).getResponse().getStatusLine().getStatusCode());
        } else {
            meterStatsManager.markEvent(ExternalSourceAspects.OTHER_ERRORS);
            System.err.printf("PGResponseHandler some other errors :  %s \n", e.getMessage());
        }
        resultFuture.complete(Collections.singleton(rowManager.getAll()));
    }

    /**
     * Writes a single query result value into the output row at the given index.
     *
     * <p>When the response type is not retained or a type is configured, map values are converted into a
     * nested {@link Row} using the field descriptor while scalar values are converted with the matching type
     * handler; otherwise the raw value is written directly. A missing field descriptor is reported as an
     * error.
     *
     * @param index the index within the output row at which to store the value
     * @param value the value read from the query result
     * @param name the output field name used to look up the field descriptor
     */
    private void setField(int index, Object value, String name) {
        if (!pgSourceConfig.isRetainResponseType() || pgSourceConfig.hasType()) {
            Descriptors.FieldDescriptor fieldDescriptor = outputDescriptor.findFieldByName(name);
            if (fieldDescriptor == null) {
                Exception illegalArgumentException = new IllegalArgumentException("Field Descriptor not found for field: " + name);
                reportAndThrowError(illegalArgumentException);
                meterStatsManager.markEvent(ExternalSourceAspects.INVALID_CONFIGURATION);
                return;
            }
            if (value instanceof Map) {
                rowManager.setInOutput(index, createRow((Map<String, Object>) value, fieldDescriptor.getMessageType()));
            } else {
                TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(fieldDescriptor);
                rowManager.setInOutput(index, typeHandler.transformFromPostProcessor(value));
            }
        } else {
            rowManager.setInOutput(index, value);
        }
    }

    /**
     * Reports the given exception as fatal and completes the result future exceptionally.
     *
     * @param exception the exception to report and propagate to the result future
     */
    private void reportAndThrowError(Exception exception) {
        errorReporter.reportFatalException(exception);
        resultFuture.completeExceptionally(exception);
    }
}
