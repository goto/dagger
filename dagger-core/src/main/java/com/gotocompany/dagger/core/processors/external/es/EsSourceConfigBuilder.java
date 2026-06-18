package com.gotocompany.dagger.core.processors.external.es;

import com.gotocompany.dagger.core.processors.common.OutputMapping;

import java.util.Map;

/**
 * Fluent builder for {@link EsSourceConfig}.
 *
 * <p>Each setter assigns one configuration property and returns {@code this} so calls can be chained;
 * {@link #createEsSourceConfig()} then produces the immutable config. It is a convenient way to
 * assemble an Elasticsearch source config in code (for example in tests) without the long positional
 * constructor.
 */
public class EsSourceConfigBuilder {
    /** Comma-separated Elasticsearch hosts. */
    private String host;
    /** Port shared by the configured hosts. */
    private String port;
    /** Basic-auth username, or {@code null} when unused. */
    private String user;
    /** Basic-auth password, or {@code null} when unused. */
    private String password;
    /** Endpoint pattern populated with the endpoint variables. */
    private String endpointPattern;
    /** Comma-separated input columns filling the endpoint pattern. */
    private String endpointVariables;
    /** Optional output protobuf class name. */
    private String type;
    /** Maximum number of concurrent async requests. */
    private String capacity;
    /** Connection timeout in milliseconds. */
    private String connectTimeout;
    /** Maximum retry timeout in milliseconds. */
    private String retryTimeout;
    /** Socket timeout in milliseconds. */
    private String socketTimeout;
    /** Async-IO stream timeout in milliseconds. */
    private String streamTimeout;
    /** Whether a failed lookup should fail the job. */
    private boolean failOnErrors;
    /** Output column to JSON-path mapping. */
    private Map<String, OutputMapping> outputMapping;
    /** Optional metric id grouping this source's metrics. */
    private String metricId;
    /** Whether to keep raw response values without type coercion. */
    private boolean retainResponseType;

    /**
     * Sets the comma-separated Elasticsearch hosts.
     *
     * @param host the hosts to connect to
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the port shared by the hosts.
     *
     * @param port the Elasticsearch port
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setPort(String port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the basic-auth username.
     *
     * @param user the username, or {@code null} when auth is not used
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * Sets the basic-auth password.
     *
     * @param password the password, or {@code null} when auth is not used
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Sets the endpoint pattern used to build the request URI.
     *
     * @param endpointPattern the pattern populated with the endpoint variables
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setEndpointPattern(String endpointPattern) {
        this.endpointPattern = endpointPattern;
        return this;
    }

    /**
     * Sets the comma-separated input columns substituted into the endpoint pattern.
     *
     * @param endpointVariables the input column names
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setEndpointVariables(String endpointVariables) {
        this.endpointVariables = endpointVariables;
        return this;
    }

    /**
     * Sets the optional output protobuf class name.
     *
     * @param type the protobuf class name used to type the response
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the maximum number of concurrent async requests.
     *
     * @param capacity the async capacity
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setCapacity(String capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectTimeout the connect timeout in milliseconds
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the maximum retry timeout.
     *
     * @param retryTimeout the retry timeout in milliseconds
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setRetryTimeout(String retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    /**
     * Sets the socket timeout.
     *
     * @param socketTimeout the socket timeout in milliseconds
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setSocketTimeout(String socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * Sets the async-IO stream timeout.
     *
     * @param streamTimeout the stream timeout in milliseconds
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setStreamTimeout(String streamTimeout) {
        this.streamTimeout = streamTimeout;
        return this;
    }

    /**
     * Sets whether a failed lookup should fail the job.
     *
     * @param failOnErrors {@code true} to fail the job on lookup errors
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setFailOnErrors(boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
        return this;
    }

    /**
     * Sets the output column to JSON-path mapping.
     *
     * @param outputMapping the mapping from output column name to its response path
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setOutputMapping(Map<String, OutputMapping> outputMapping) {
        this.outputMapping = outputMapping;
        return this;
    }

    /**
     * Sets the optional metric id grouping this source's metrics.
     *
     * @param metricId the metric id
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setMetricId(String metricId) {
        this.metricId = metricId;
        return this;
    }

    /**
     * Sets whether raw response values are kept without type coercion.
     *
     * @param retainResponseType {@code true} to retain the raw response type
     * @return this builder for chaining
     */
    public EsSourceConfigBuilder setRetainResponseType(boolean retainResponseType) {
        this.retainResponseType = retainResponseType;
        return this;
    }

    /**
     * Builds an immutable {@link EsSourceConfig} from the values accumulated in this builder.
     *
     * @return the assembled Elasticsearch source config
     */
    public EsSourceConfig createEsSourceConfig() {
        return new EsSourceConfig(host, port, user, password, endpointPattern, endpointVariables, type, capacity, connectTimeout, retryTimeout, socketTimeout, streamTimeout, failOnErrors, outputMapping, metricId, retainResponseType);
    }
}
