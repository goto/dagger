package com.gotocompany.dagger.core.processors.external.es;

import com.gotocompany.dagger.core.exception.HttpFailureException;
import com.gotocompany.dagger.core.metrics.aspects.ExternalSourceAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.PostResponseTelemetry;
import com.gotocompany.dagger.core.processors.common.RowManager;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.gotocompany.dagger.common.serde.typehandler.RowFactory.createRow;
import static java.util.Collections.singleton;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * The ElasticSearch response handler.
 */
public class EsResponseHandler implements ResponseListener {
    /**
     * Logger used to record Elasticsearch response parsing and processing errors.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EsResponseHandler.class.getName());
    /**
     * The Elasticsearch source configuration describing output mappings and error behaviour.
     */
    private EsSourceConfig esSourceConfig;
    /**
     * Manager wrapping the input and output rows that response values are written into.
     */
    private RowManager rowManager;
    /**
     * The protobuf descriptor of the output message used to type-cast response values.
     */
    private Descriptor outputDescriptor;
    /**
     * The future completed with the enriched row once the response has been handled.
     */
    private ResultFuture<Row> resultFuture;
    /**
     * The time at which the request was issued, used to compute response latency telemetry.
     */
    private Instant startTime;
    /**
     * Manager used to emit meter-style metrics for response outcomes.
     */
    private MeterStatsManager meterStatsManager;
    /**
     * Resolver mapping output column names to their positions in the output row.
     */
    private ColumnNameManager columnNameManager;
    /**
     * Reporter used to surface fatal and non-fatal errors raised while handling responses.
     */
    private ErrorReporter errorReporter;
    /**
     * Helper that emits success and failure telemetry for the external call.
     */
    private PostResponseTelemetry postResponseTelemetry;

    /**
     * Instantiates a new ElasticSearch response handler.
     *
     * @param esSourceConfig        the es source config
     * @param meterStatsManager     the meter stats manager
     * @param rowManager            the row manager
     * @param columnNameManager     the column name manager
     * @param outputDescriptor      the output descriptor
     * @param resultFuture          the result future
     * @param errorStatsReporter    the error stats reporter
     * @param postResponseTelemetry the post response telemetry
     */
    public EsResponseHandler(EsSourceConfig esSourceConfig, MeterStatsManager meterStatsManager, RowManager rowManager, ColumnNameManager columnNameManager, Descriptor outputDescriptor, ResultFuture<Row> resultFuture, ErrorReporter errorStatsReporter, PostResponseTelemetry postResponseTelemetry) {
        this.esSourceConfig = esSourceConfig;
        this.rowManager = rowManager;
        this.outputDescriptor = outputDescriptor;
        this.resultFuture = resultFuture;
        this.meterStatsManager = meterStatsManager;
        this.columnNameManager = columnNameManager;
        this.errorReporter = errorStatsReporter;
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
     * <p>For a successful ({@code 200 OK}) response, reads each configured output column from the JSON body
     * using its JSON path and writes the value into the output row. Path, parse, read, and other errors are
     * recorded as telemetry and reported. The result future is always completed with the current row.
     *
     * @param response the Elasticsearch response to read enrichment values from
     */
    @Override
    public void onSuccess(Response response) {
        try {
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                return;
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            List<String> esOutputColumnNames = esSourceConfig.getOutputColumns();
            esOutputColumnNames.forEach(outputColumnName -> {
                String outputColumnPath = esSourceConfig.getPath(outputColumnName);
                Object outputValue;
                try {
                    outputValue = JsonPath.parse(responseBody).read(outputColumnPath, new Object().getClass());
                } catch (PathNotFoundException exception) {
                    postResponseTelemetry.failureReadingPath(meterStatsManager);
                    LOGGER.error(exception.getMessage());
                    reportAndThrowError(exception);
                    return;
                }
                int outputColumnIndex = columnNameManager.getOutputIndex(outputColumnName);
                setField(esSourceConfig, outputColumnIndex, outputValue, outputColumnName);
            });
        } catch (ParseException e) {
            meterStatsManager.markEvent(ExternalSourceAspects.ERROR_PARSING_RESPONSE);
            System.err.printf("ESResponseHandler : error parsing response, error msg : %s, response : %s\n", e.getMessage(), response.toString());
            errorReporter.reportNonFatalException(e);
            e.printStackTrace();
        } catch (IOException e) {
            meterStatsManager.markEvent(ExternalSourceAspects.ERROR_READING_RESPONSE);
            System.err.printf("ESResponseHandler : error reading response, error msg : %s, response : %s\n", e.getMessage(), response.toString());
            errorReporter.reportNonFatalException(e);
            e.printStackTrace();
        } catch (Exception e) {
            meterStatsManager.markEvent(ExternalSourceAspects.OTHER_ERRORS_PROCESSING_RESPONSE);
            System.err.printf("ESResponseHandler : other errors processing response, error msg : %s, response : %s\n", e.getMessage(), response.toString());
            errorReporter.reportNonFatalException(e);
            e.printStackTrace();
        } finally {
            postResponseTelemetry.sendSuccessTelemetry(meterStatsManager, startTime);
            resultFuture.complete(singleton(rowManager.getAll()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Records failure telemetry and either reports and throws an {@code HttpFailureException} (when the
     * source is configured to fail on errors) or reports it as non-fatal, validating the HTTP status code when
     * the cause is a {@code ResponseException}. The result future is always completed with the current row.
     *
     * @param e the exception describing the Elasticsearch failure
     */
    @Override
    public void onFailure(Exception e) {
        postResponseTelemetry.sendFailureTelemetry(meterStatsManager, startTime);
        Exception httpFailureException = new HttpFailureException("EsResponseHandler : Failed with error. " + e.getMessage());
        if (esSourceConfig.isFailOnErrors()) {
            reportAndThrowError(httpFailureException);
        } else {
            errorReporter.reportNonFatalException(e);
        }
        if (e instanceof ResponseException) {
            postResponseTelemetry.validateResponseCode(meterStatsManager, ((ResponseException) e).getResponse().getStatusLine().getStatusCode());
        } else {
            meterStatsManager.markEvent(ExternalSourceAspects.OTHER_ERRORS);
            System.err.printf("ESResponseHandler some other errors :  %s \n", e.getMessage());
        }
        resultFuture.complete(singleton(rowManager.getAll()));
    }


    /**
     * Writes a single response value into the output row at the given index.
     *
     * <p>When the response type is not retained (or an explicit type is configured), the value is converted
     * using the field's {@link TypeHandler}, and map values are built into a nested {@link Row}. A missing
     * field descriptor is reported as an error.
     *
     * @param esConfig the Elasticsearch source configuration controlling type handling
     * @param index    the output row index to write the value into
     * @param value    the raw value read from the response
     * @param name     the output column (field) name being populated
     */
    private void setField(EsSourceConfig esConfig, int index, Object value, String name) {
        if (!esConfig.isRetainResponseType() || esConfig.hasType()) {
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
     * @param exception the exception to report and propagate
     */
    private void reportAndThrowError(Exception exception) {
        errorReporter.reportFatalException(exception);
        resultFuture.completeExceptionally(exception);
    }
}
