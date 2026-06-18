package com.gotocompany.dagger.core.metrics.aspects;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;

/**
 * The enum External source aspects.
 */
public enum ExternalSourceAspects implements Aspects {
    /**
     * Counts how often a connection to the external client is closed.
     */
    CLOSE_CONNECTION_ON_EXTERNAL_CLIENT("close_connection_on_external_client", AspectType.Metric),
    /**
     * Counts failures encountered while reading the configured response path.
     */
    FAILURES_ON_READING_PATH("failures_on_reading_path", AspectType.Metric),
    /**
     * Counts the total number of failed external requests.
     */
    TOTAL_FAILED_REQUESTS("total_failures", AspectType.Metric),
    /**
     * Counts the number of external calls that timed out.
     */
    TIMEOUTS("timeouts", AspectType.Metric),
    /**
     * Records the response-time distribution for successful external calls.
     */
    SUCCESS_RESPONSE_TIME("success_response_time", AspectType.Histogram),
    /**
     * Records the response-time distribution for failed external calls.
     */
    FAILURES_RESPONSE_TIME("failure_response_time", AspectType.Histogram),
    /**
     * Counts the number of successful responses received from the external source.
     */
    SUCCESS_RESPONSE("success_response", AspectType.Metric),
    /**
     * Counts errors encountered while parsing an external response.
     */
    ERROR_PARSING_RESPONSE("parse_errors", AspectType.Metric),
    /**
     * Counts errors raised while issuing an external request.
     */
    REQUEST_ERROR("request_error", AspectType.Metric),
    /**
     * Counts miscellaneous errors that do not fall into a more specific category.
     */
    OTHER_ERRORS("other_errors", AspectType.Metric),
    /**
     * Counts the total number of external calls attempted.
     */
    TOTAL_EXTERNAL_CALLS("total_external_calls", AspectType.Metric),
    /**
     * Counts records received with empty input for the external call.
     */
    EMPTY_INPUT("empty_input", AspectType.Metric),
    /**
     * Counts errors encountered while reading an external response.
     */
    ERROR_READING_RESPONSE("error_reading_response", AspectType.Metric),
    /**
     * Counts other errors encountered while processing an external response.
     */
    OTHER_ERRORS_PROCESSING_RESPONSE("other_errors_processing_response", AspectType.Metric),
    /**
     * Counts occurrences of invalid external source configuration.
     */
    INVALID_CONFIGURATION("invalid_configuration", AspectType.Metric),
    /**
     * Counts external responses that failed with a 5XX server error status code.
     */
    FAILURE_CODE_5XX("failures_code5XX", AspectType.Metric),
    /**
     * Counts external responses that failed with a 4XX client error status code.
     */
    FAILURE_CODE_4XX("failures_code4XX", AspectType.Metric),
    /**
     * Counts external responses that failed with a 404 not found status code.
     */
    FAILURE_CODE_404("failures_code404", AspectType.Metric),
    /**
     * Counts occurrences where the gRPC channel was not available for an external call.
     */
    GRPC_CHANNEL_NOT_AVAILABLE("grpc_channel_not_available", AspectType.Metric);

    /**
     * The metric identifier reported for this aspect.
     */
    private String value;
    /**
     * The metric type this aspect represents.
     */
    private AspectType aspectType;

    /**
     * Instantiates a new external source aspect.
     *
     * @param value      the metric identifier for this aspect
     * @param aspectType the metric type this aspect represents
     */
    ExternalSourceAspects(String value, AspectType aspectType) {
        this.value = value;
        this.aspectType = aspectType;
    }

    /**
     * Returns the metric identifier reported for this aspect.
     *
     * @return the metric identifier configured for this aspect
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Returns the metric type associated with this aspect.
     *
     * @return the metric type configured for this aspect
     */
    @Override
    public AspectType getAspectType() {
        return aspectType;
    }
}
