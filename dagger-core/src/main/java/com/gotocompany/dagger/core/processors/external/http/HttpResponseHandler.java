package com.gotocompany.dagger.core.processors.external.http;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.gotocompany.dagger.core.exception.HttpFailureException;
import com.gotocompany.dagger.core.metrics.aspects.ExternalSourceAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.OutputMapping;
import com.gotocompany.dagger.core.processors.common.PostResponseTelemetry;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.utils.DescriptorsUtil;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The Http response handler.
 */
public class HttpResponseHandler extends AsyncCompletionHandler<Object> {
    /**
     * Logger used to record response failures and path resolution errors.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponseHandler.class.getName());

    /**
     * Regular expression matching any HTTP 2xx status code, used to classify a response as successful.
     */
    protected static final String SUCCESS_CODE_PATTERN = "^2.*";
    /**
     * Manages the input and output {@link Row} so resolved values can be written back to the output row.
     */
    private final RowManager rowManager;
    /**
     * Resolves output column names to their positional indexes within the output row.
     */
    private ColumnNameManager columnNameManager;
    /**
     * Protobuf descriptor describing the output message type, used for type-aware field conversion.
     */
    private Descriptors.Descriptor descriptor;
    /**
     * The future completed with the enriched output row once the response has been processed.
     */
    private ResultFuture<Row> resultFuture;
    /**
     * Configuration describing the output mapping, type handling and error behaviour for the call.
     */
    private HttpSourceConfig httpSourceConfig;
    /**
     * The set of status codes excluded from triggering a fatal failure when fail-on-errors is enabled.
     */
    private Set<Integer> failOnErrorsExclusionSet;
    /**
     * Manager used to emit success, failure and error metrics for the external call.
     */
    private MeterStatsManager meterStatsManager;
    /**
     * The instant at which the request was dispatched, used to compute response latency telemetry.
     */
    private Instant startTime;
    /**
     * Reporter used to surface fatal and non-fatal exceptions raised while handling the response.
     */
    private ErrorReporter errorReporter;
    /**
     * Helper that emits post-response telemetry such as success, failure and latency events.
     */
    private PostResponseTelemetry postResponseTelemetry;


    /**
     * Instantiates a new Http response handler.
     *
     * @param httpSourceConfig          the http source config
     * @param failOnErrorsExclusionSet  the fail on error exclusion set
     * @param meterStatsManager         the meter stats manager
     * @param rowManager                the row manager
     * @param columnNameManager         the column name manager
     * @param descriptor                the descriptor
     * @param resultFuture              the result future
     * @param errorReporter             the error reporter
     * @param postResponseTelemetry     the post response telemetry
     */
    public HttpResponseHandler(HttpSourceConfig httpSourceConfig, Set<Integer> failOnErrorsExclusionSet, MeterStatsManager meterStatsManager, RowManager rowManager,
                               ColumnNameManager columnNameManager, Descriptors.Descriptor descriptor, ResultFuture<Row> resultFuture,
                               ErrorReporter errorReporter, PostResponseTelemetry postResponseTelemetry) {

        this.httpSourceConfig = httpSourceConfig;
        this.failOnErrorsExclusionSet = failOnErrorsExclusionSet;
        this.meterStatsManager = meterStatsManager;
        this.rowManager = rowManager;
        this.columnNameManager = columnNameManager;
        this.descriptor = descriptor;
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
     * <p>Invoked when the asynchronous HTTP call completes. Responses with a 2xx status code are passed to
     * the success handler; all other codes are recorded as telemetry and routed to the failure handler.
     *
     * @param response the HTTP {@code Response} returned by the external service
     * @return the original {@code Response} object
     */
    @Override
    public Object onCompleted(Response response) {
        int statusCode = response.getStatusCode();
        boolean isSuccess = Pattern.compile(SUCCESS_CODE_PATTERN).matcher(String.valueOf(statusCode)).matches();
        if (isSuccess) {
            successHandler(response);
        } else {
            postResponseTelemetry.validateResponseCode(meterStatsManager, statusCode);
            failureHandler("Received status code : " + statusCode, statusCode);
        }
        return response;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invoked when the asynchronous HTTP call fails with an exception. Records an other-errors metric and
     * delegates to the failure handler with a status code of {@code 0}.
     *
     * @param t the throwable raised while performing the request
     */
    @Override
    public void onThrowable(Throwable t) {
        t.printStackTrace();
        meterStatsManager.markEvent(ExternalSourceAspects.OTHER_ERRORS);
        failureHandler(t.getMessage(), 0);
    }

    /**
     * Extracts the configured fields from a successful response and completes the result future.
     *
     * <p>For each configured output mapping the value is read from the response body using its JsonPath,
     * written into the output row at the resolved column index and, on success, success telemetry is sent.
     * A missing path records a failure metric and reports the error.
     *
     * @param response the successful HTTP {@code Response} whose body is parsed for output values
     */
    private void successHandler(Response response) {
        Map<String, OutputMapping> outputMappings = httpSourceConfig.getOutputMapping();
        ArrayList<String> outputMappingKeys = new ArrayList<>(outputMappings.keySet());

        outputMappingKeys.forEach(key -> {
            OutputMapping outputMappingKeyConfig = outputMappings.get(key);
            Object value;
            try {
                value = JsonPath.parse(response.getResponseBody()).read(outputMappingKeyConfig.getPath(), Object.class);
            } catch (PathNotFoundException e) {
                postResponseTelemetry.failureReadingPath(meterStatsManager);
                LOGGER.error(e.getMessage());
                reportAndThrowError(e);
                return;
            }
            int fieldIndex = columnNameManager.getOutputIndex(key);
            setField(key, value, fieldIndex);
        });
        postResponseTelemetry.sendSuccessTelemetry(meterStatsManager, startTime);
        resultFuture.complete(Collections.singleton(rowManager.getAll()));
    }

    /**
     * Failure handler.
     *
     * @param logMessage the log message
     * @param statusCode the status code
     */
    public void failureHandler(String logMessage, Integer statusCode) {
        postResponseTelemetry.sendFailureTelemetry(meterStatsManager, startTime);
        LOGGER.error(logMessage);
        Exception httpFailureException = new HttpFailureException(logMessage);
        if (shouldFailOnError(statusCode)) {
            reportAndThrowError(httpFailureException);
        } else {
            errorReporter.reportNonFatalException(httpFailureException);
        }
        resultFuture.complete(Collections.singleton(rowManager.getAll()));
    }

    /**
     * Determines whether the given status code should be treated as a fatal failure.
     *
     * @param statusCode the HTTP status code of the response, or {@code 0} when the call threw an exception
     * @return {@code true} when fail-on-errors is enabled and the code is either {@code 0} or not present in
     *         the exclusion set; {@code false} otherwise
     */
    private boolean shouldFailOnError(Integer statusCode) {
         if (httpSourceConfig.isFailOnErrors() && (statusCode == 0 || !failOnErrorsExclusionSet.contains(statusCode))) {
            return true;
        }
        return false;
    }

    /**
     * Writes a resolved response value into the output row at the given index.
     *
     * <p>When the response type is not retained or a type is configured the value is converted using the
     * matching type handler; otherwise the raw value is written directly.
     *
     * @param key the output field name the value maps to
     * @param value the value read from the response body
     * @param fieldIndex the index within the output row at which to store the value
     */
    private void setField(String key, Object value, int fieldIndex) {
        if (!httpSourceConfig.isRetainResponseType() || httpSourceConfig.hasType()) {
            setFieldUsingType(key, value, fieldIndex);
        } else {
            rowManager.setInOutput(fieldIndex, value);
        }
    }

    /**
     * Converts a response value using the protobuf field's type handler before writing it to the output row.
     *
     * <p>Resolves the field descriptor for {@code key} from the output descriptor and throws if it is not
     * found, then applies the corresponding type handler transformation.
     *
     * @param key the output field name used to look up the field descriptor
     * @param value the value read from the response body
     * @param fieldIndex the index within the output row at which to store the converted value
     */
    private void setFieldUsingType(String key, Object value, Integer fieldIndex) {
        Descriptors.FieldDescriptor fieldDescriptor = null;
        try {
            fieldDescriptor = DescriptorsUtil.getFieldDescriptor(descriptor, key);
            if (fieldDescriptor == null) {
                throw new IllegalArgumentException("Field Descriptor not found for field: " + key);
            }
        } catch (RuntimeException exception) {
            reportAndThrowError(exception);
        }
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(fieldDescriptor);
        rowManager.setInOutput(fieldIndex, typeHandler.transformFromPostProcessor(value));
    }


    /**
     * Reports the given exception as fatal and completes the result future exceptionally.
     *
     * @param e the exception to report and propagate to the result future
     */
    private void reportAndThrowError(Exception e) {
        errorReporter.reportFatalException(e);
        resultFuture.completeExceptionally(e);
    }
}
