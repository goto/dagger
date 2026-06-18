package com.gotocompany.dagger.core.processors.external.http;

import com.gotocompany.dagger.core.processors.common.OutputMapping;
import com.gotocompany.dagger.core.processors.types.SourceConfig;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A class that holds Http configuration.
 */
public class HttpSourceConfig implements Serializable, SourceConfig {
    /**
     * The base HTTP endpoint URL, optionally containing format placeholders for endpoint variables.
     */
    private String endpoint;
    /**
     * Comma-separated input columns whose values are substituted into the endpoint placeholders.
     */
    private String endpointVariables;
    /**
     * The HTTP verb (for example {@code GET}, {@code POST} or {@code PUT}) used for the request.
     */
    private String verb;
    /**
     * Format pattern used to build the request path or body from the request variables.
     */
    private String requestPattern;
    /**
     * Comma-separated input columns whose values populate the request pattern.
     */
    private String requestVariables;
    /**
     * Format pattern used to build the dynamic request headers.
     */
    private String headerPattern;
    /**
     * Comma-separated input columns whose values populate the header pattern.
     */
    private String headerVariables;
    /**
     * Maximum time, in milliseconds, to wait for the asynchronous response before timing out.
     */
    private String streamTimeout;
    /**
     * Maximum time, in milliseconds, to wait while establishing the connection.
     */
    private String connectTimeout;
    /**
     * Whether a failed call should raise a fatal error instead of being reported as non-fatal.
     */
    private boolean failOnErrors;
    /**
     * Comma-separated, hyphen-delimited status code ranges excluded from fail-on-errors handling.
     */
    private String excludeFailOnErrorsCodeRange;
    /**
     * Fully qualified protobuf message type used for type-aware conversion of the response.
     */
    @SerializedName(value = "type", alternate = {"Type", "TYPE"})
    private String type;
    /**
     * Maximum number of concurrent asynchronous requests allowed for this connector.
     */
    private String capacity;
    /**
     * Static headers added to every request, keyed by header name.
     */
    @SerializedName(value = "headers", alternate = {"Headers", "HEADERS"})
    private Map<String, String> headers;
    /**
     * Mapping of output column names to the configuration describing how to extract their value from the response.
     */
    private Map<String, OutputMapping> outputMapping;
    /**
     * Identifier used to tag the metrics emitted for this external source.
     */
    @SerializedName(value = "metricId", alternate = {"MetricId", "METRICID"})
    private String metricId;
    /**
     * Whether the raw response value should be retained as-is instead of being converted to the configured type.
     */
    private boolean retainResponseType;

    /**
     * Instantiates a new Http source config.
     *
     * @param endpoint           the endpoint
     * @param endpointVariables  the endpoint variables
     * @param verb               the verb
     * @param requestPattern     the request pattern
     * @param requestVariables   the request variables
     * @param headerPattern      the dynamic header pattern
     * @param headerVariables    the header variables
     * @param streamTimeout      the stream timeout
     * @param connectTimeout     the connect timeout
     * @param failOnErrors       the fail on errors
     * @param excludeFailOnErrorsCodeRange the exclude fail on errors code range
     * @param type               the type
     * @param capacity           the capacity
     * @param headers            the static headers
     * @param outputMapping      the output mapping
     * @param metricId           the metric id
     * @param retainResponseType the retain response type
     */
    public HttpSourceConfig(String endpoint, String endpointVariables, String verb, String requestPattern, String requestVariables, String headerPattern, String headerVariables, String streamTimeout, String connectTimeout, boolean failOnErrors, String excludeFailOnErrorsCodeRange,  String type, String capacity, Map<String, String> headers, Map<String, OutputMapping> outputMapping, String metricId, boolean retainResponseType) {
        this.endpoint = endpoint;
        this.endpointVariables = endpointVariables;
        this.verb = verb;
        this.requestPattern = requestPattern;
        this.requestVariables = requestVariables;
        this.headerPattern = headerPattern;
        this.headerVariables = headerVariables;
        this.streamTimeout = streamTimeout;
        this.connectTimeout = connectTimeout;
        this.failOnErrors = failOnErrors;
        this.excludeFailOnErrorsCodeRange = excludeFailOnErrorsCodeRange;
        this.type = type;
        this.capacity = capacity;
        this.headers = headers;
        this.outputMapping = outputMapping;
        this.metricId = metricId;
        this.retainResponseType = retainResponseType;
    }

    /**
     * Gets connect timeout.
     *
     * @return the connect timeout
     */
    public Integer getConnectTimeout() {
        return Integer.parseInt(connectTimeout);
    }

    /**
     * Gets endpoint.
     *
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Gets endpoint variables.
     *
     * @return the endpointVariables
     */
    public String getEndpointVariables() {
        return endpointVariables;
    }


    /**
     * Gets verb.
     *
     * @return the verb
     */
    public String getVerb() {
        return verb;
    }

    /**
     * Gets request variables.
     *
     * @return the request variables
     */
    public String getRequestVariables() {
        return requestVariables;
    }

    /**
     * Gets header pattern.
     *
     * @return the header pattern
     */
    public String getHeaderPattern() {
        return headerPattern;
    }

    /**
     * Gets header Variable.
     *
     * @return the header Variable
     */
    public String getHeaderVariables() {
        return headerVariables;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For an HTTP source the pattern is the configured request pattern.
     *
     * @return the request format pattern
     */
    @Override
    public String getPattern() {
        return requestPattern;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For an HTTP source the variables are the configured request variables.
     *
     * @return the comma-separated request variable columns
     */
    @Override
    public String getVariables() {
        return requestVariables;
    }

    /**
     * Gets stream timeout.
     *
     * @return the stream timeout
     */
    public Integer getStreamTimeout() {
        return Integer.valueOf(streamTimeout);
    }

    /**
     * Returns whether the connector should fail fatally when the call errors.
     *
     * @return {@code true} if failures should be treated as fatal, {@code false} otherwise
     */
    public boolean isFailOnErrors() {
        return failOnErrors;
    }

    /**
     * Gets failOnErrorsCodeRange Variable.
     *
     * @return the failOnErrorsCodeRange Variable
     */
    public String getExcludeFailOnErrorsCodeRange() {
        return excludeFailOnErrorsCodeRange;
    }


    /**
     * {@inheritDoc}
     *
     * @return the metric identifier configured for this source
     */
    @Override
    public String getMetricId() {
        return metricId;
    }

    /**
     * Gets the configured protobuf message type.
     *
     * @return the fully qualified type name, or {@code null} when no type is configured
     */
    public String getType() {
        return type;
    }

    /**
     * Check if type config is not empty.
     *
     * @return the boolean
     */
    public boolean hasType() {
        return StringUtils.isNotEmpty(type);
    }

    /**
     * Gets headers.
     *
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets output mapping.
     *
     * @return the output mapping
     */
    public Map<String, OutputMapping> getOutputMapping() {
        return outputMapping;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The output columns are derived from the keys of the configured output mapping.
     *
     * @return the {@code List<String>} of output column names
     */
    @Override
    public List<String> getOutputColumns() {
        return new ArrayList<>(outputMapping.keySet());
    }

    /**
     * Builds the map of fields that must be present for the configuration to be valid.
     *
     * <p>The returned map is used by validation to ensure required settings such as the endpoint, verb,
     * patterns, timeouts and output mapping have been supplied.
     *
     * @return a {@code HashMap<String, Object>} of mandatory field names to their configured values
     */
    public HashMap<String, Object> getMandatoryFields() {
        HashMap<String, Object> mandatoryFields = new HashMap<>();
        mandatoryFields.put("endpoint", endpoint);
        mandatoryFields.put("verb", verb);
        mandatoryFields.put("failOnErrors", failOnErrors);
        mandatoryFields.put("capacity", capacity);
        mandatoryFields.put("requestPattern", requestPattern);
        mandatoryFields.put("requestVariables", requestVariables);
        mandatoryFields.put("streamTimeout", streamTimeout);
        mandatoryFields.put("connectTimeout", connectTimeout);
        mandatoryFields.put("outputMapping", outputMapping);

        return mandatoryFields;
    }

    /**
     * Gets capacity.
     *
     * @return the capacity
     */
    public Integer getCapacity() {
        return Integer.parseInt(capacity);
    }

    /**
     * Check if it is retain response type.
     *
     * @return the boolean
     */
    public boolean isRetainResponseType() {
        return retainResponseType;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Two configs are equal when all of their configuration fields are equal.
     *
     * @param o the object to compare with this configuration
     * @return {@code true} if the given object is an equivalent {@code HttpSourceConfig}, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpSourceConfig that = (HttpSourceConfig) o;
        return failOnErrors == that.failOnErrors && excludeFailOnErrorsCodeRange == that.excludeFailOnErrorsCodeRange && retainResponseType == that.retainResponseType && Objects.equals(endpoint, that.endpoint) && Objects.equals(verb, that.verb) && Objects.equals(requestPattern, that.requestPattern) && Objects.equals(requestVariables, that.requestVariables) && Objects.equals(headerPattern, that.headerPattern) && Objects.equals(headerVariables, that.headerVariables) && Objects.equals(streamTimeout, that.streamTimeout) && Objects.equals(connectTimeout, that.connectTimeout) && Objects.equals(type, that.type) && Objects.equals(capacity, that.capacity) && Objects.equals(headers, that.headers) && Objects.equals(outputMapping, that.outputMapping) && Objects.equals(metricId, that.metricId);
    }

    /**
     * {@inheritDoc}
     *
     * @return a hash code derived from all configuration fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(endpoint, endpointVariables, verb, requestPattern, requestVariables, headerPattern, headerVariables, streamTimeout, connectTimeout, failOnErrors, excludeFailOnErrorsCodeRange, type, capacity, headers, outputMapping, metricId, retainResponseType);
    }
}
