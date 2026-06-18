package com.gotocompany.dagger.core.processors.external.es;

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
 * A class that holds ElasticSearch configuration.
 */
public class EsSourceConfig implements Serializable, SourceConfig {
    /**
     * The comma-separated Elasticsearch host(s) to connect to.
     */
    private final String host;
    /**
     * The Elasticsearch port to connect to.
     */
    private final String port;
    /**
     * The username used for basic authentication, or empty when unauthenticated.
     */
    private final String user;
    /**
     * The password used for basic authentication, or empty when unauthenticated.
     */
    private final String password;
    /**
     * The format pattern used to build the Elasticsearch endpoint from the resolved variables.
     */
    private final String endpointPattern;
    /**
     * The comma-separated input columns whose values are substituted into the endpoint pattern.
     */
    private final String endpointVariables;
    /**
     * The fully-qualified protobuf class name used to type-cast the response, recognised under the JSON keys
     * {@code type}, {@code Type}, or {@code TYPE}.
     */
    @SerializedName(value = "type", alternate = {"Type", "TYPE"})
    private final String type;
    /**
     * The maximum number of concurrent asynchronous requests buffered by the async operator.
     */
    private final String capacity;
    /**
     * The maximum retry timeout in milliseconds applied by the Elasticsearch client.
     */
    private final String retryTimeout;
    /**
     * The socket timeout in milliseconds for an Elasticsearch request.
     */
    private final String socketTimeout;
    /**
     * The overall stream/async timeout in milliseconds for the lookup.
     */
    private final String streamTimeout;
    /**
     * The connection timeout in milliseconds for establishing an Elasticsearch connection.
     */
    private final String connectTimeout;
    /**
     * Whether a lookup failure should fail the job ({@code true}) or be tolerated as non-fatal ({@code false}).
     */
    private final boolean failOnErrors;
    /**
     * The mapping of output column name to the configuration describing how to extract its value.
     */
    private final Map<String, OutputMapping> outputMapping;
    /**
     * Optional identifier used to disambiguate metrics emitted for this source, recognised under the JSON keys
     * {@code metricId}, {@code MetricId}, or {@code METRICID}.
     */
    @SerializedName(value = "metricId", alternate = {"MetricId", "METRICID"})
    private final String metricId;
    /**
     * Whether the raw response value type is retained as-is instead of being cast to the configured proto type.
     */
    private final boolean retainResponseType;


    /**
     * Instantiates a new ElasticSearch source config.
     *
     * @param host               the host
     * @param port               the port
     * @param user               the user
     * @param password           the password
     * @param endpointPattern    the endpoint pattern
     * @param endpointVariables  the endpoint variables
     * @param type               the type
     * @param capacity           the capacity
     * @param connectTimeout     the connect timeout
     * @param retryTimeout       the retry timeout
     * @param socketTimeout      the socket timeout
     * @param streamTimeout      the stream timeout
     * @param failOnErrors       the fail on errors
     * @param outputMapping      the output mapping
     * @param metricId           the metric id
     * @param retainResponseType the retain response type
     */
    public EsSourceConfig(String host, String port, String user, String password, String endpointPattern, String endpointVariables,
                          String type, String capacity, String connectTimeout, String retryTimeout, String socketTimeout, String streamTimeout,
                          boolean failOnErrors, Map<String, OutputMapping> outputMapping, String metricId, boolean retainResponseType) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.endpointPattern = endpointPattern;
        this.endpointVariables = endpointVariables;
        this.type = type;
        this.capacity = capacity;
        this.connectTimeout = connectTimeout;
        this.retryTimeout = retryTimeout;
        this.socketTimeout = socketTimeout;
        this.streamTimeout = streamTimeout;
        this.failOnErrors = failOnErrors;
        this.outputMapping = outputMapping;
        this.metricId = metricId;
        this.retainResponseType = retainResponseType;
    }


    /**
     * Gets host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public Integer getPort() {
        return Integer.valueOf(port);
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    public String getUser() {
        return user == null ? "" : user;
    }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() {
        return password == null ? "" : password;
    }

    /**
     * {@inheritDoc}
     *
     * @return the endpoint pattern used to build the Elasticsearch request
     */
    @Override
    public String getPattern() {
        return endpointPattern;
    }

    /**
     * {@inheritDoc}
     *
     * @return the comma-separated endpoint variable column names
     */
    @Override
    public String getVariables() {
        return endpointVariables;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if lookup failures should fail the job, otherwise {@code false}
     */
    @Override
    public boolean isFailOnErrors() {
        return failOnErrors;
    }

    /**
     * {@inheritDoc}
     *
     * @return the configured metric id for this source, or {@code null} when unset
     */
    @Override
    public String getMetricId() {
        return metricId;
    }

    /**
     * {@inheritDoc}
     *
     * @return the configured protobuf type name used to cast the response, or {@code null} when unset
     */
    @Override
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
     * Gets capacity.
     *
     * @return the capacity
     */
    public Integer getCapacity() {
        return Integer.valueOf(capacity);
    }

    /**
     * Gets retry timeout.
     *
     * @return the retry timeout
     */
    public Integer getRetryTimeout() {
        return Integer.valueOf(retryTimeout);
    }

    /**
     * Gets socket timeout.
     *
     * @return the socket timeout
     */
    public Integer getSocketTimeout() {
        return Integer.valueOf(socketTimeout);
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
     * Gets connect timeout.
     *
     * @return the connect timeout
     */
    public Integer getConnectTimeout() {
        return Integer.valueOf(connectTimeout);
    }

    /**
     * Gets path.
     *
     * @param outputColumn the output column
     * @return the path
     */
    public String getPath(String outputColumn) {
        return outputMapping.get(outputColumn).getPath();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Exposes the host, port, endpoint pattern, capacity, timeouts, fail-on-errors flag, and output mapping
     * as the fields that must be present for this configuration to be valid.
     *
     * @return a map of mandatory field names to their configured values
     */
    @Override
    public HashMap<String, Object> getMandatoryFields() {
        HashMap<String, Object> mandatoryFields = new HashMap<>();
        mandatoryFields.put("host", host);
        mandatoryFields.put("port", port);
        mandatoryFields.put("endpoint_pattern", endpointPattern);
        mandatoryFields.put("capacity", capacity);
        mandatoryFields.put("connect_timeout", connectTimeout);
        mandatoryFields.put("retry_timeout", retryTimeout);
        mandatoryFields.put("socket_timeout", socketTimeout);
        mandatoryFields.put("stream_timeout", streamTimeout);
        mandatoryFields.put("fail_on_errors", failOnErrors);
        mandatoryFields.put("outputMapping", outputMapping);

        return mandatoryFields;
    }

    /**
     * {@inheritDoc}
     *
     * @return the output column names derived from the configured output mapping keys
     */
    @Override
    public List<String> getOutputColumns() {
        return new ArrayList<>(outputMapping.keySet());
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
     * @param o the object to compare with this configuration
     * @return {@code true} if the other object is an {@code EsSourceConfig} with equal fields
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EsSourceConfig that = (EsSourceConfig) o;
        return failOnErrors == that.failOnErrors && retainResponseType == that.retainResponseType && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(user, that.user) && Objects.equals(password, that.password) && Objects.equals(endpointPattern, that.endpointPattern) && Objects.equals(endpointVariables, that.endpointVariables) && Objects.equals(type, that.type) && Objects.equals(capacity, that.capacity) && Objects.equals(retryTimeout, that.retryTimeout) && Objects.equals(socketTimeout, that.socketTimeout) && Objects.equals(streamTimeout, that.streamTimeout) && Objects.equals(connectTimeout, that.connectTimeout) && Objects.equals(outputMapping, that.outputMapping) && Objects.equals(metricId, that.metricId);
    }

    /**
     * {@inheritDoc}
     *
     * @return a hash code derived from all configuration fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(host, port, user, password, endpointPattern, endpointVariables, type, capacity, retryTimeout, socketTimeout, streamTimeout, connectTimeout, failOnErrors, outputMapping, metricId, retainResponseType);
    }
}
