package com.gotocompany.dagger.core.processors.external.grpc;

import com.gotocompany.dagger.core.processors.common.OutputMapping;

import java.util.Map;

/**
 * Fluent builder for {@link GrpcSourceConfig}.
 *
 * <p>Each setter assigns one configuration property and returns {@code this} so calls can be chained;
 * {@link #createGrpcSourceConfig()} then produces the config. It is a convenient way to assemble a
 * gRPC source config in code (for example in tests) without the long positional constructor.
 */
public class GrpcSourceConfigBuilder {
    /** Hostname or address of the gRPC service. */
    private String endpoint;
    /** Port of the gRPC service. */
    private int servicePort;
    /** Request message protobuf class name. */
    private String grpcRequestProtoSchema;
    /** Response message protobuf class name. */
    private String grpcResponseProtoSchema;
    /** Fully-qualified gRPC method name to invoke. */
    private String grpcMethodUrl;
    /** Channel keepalive interval in milliseconds. */
    private String grpcArgKeepaliveTimeMs;
    /** Channel keepalive timeout in milliseconds. */
    private String grpcArgKeepaliveTimeoutMs;
    /** Request body pattern populated with the request variables. */
    private String requestPattern;
    /** Comma-separated input columns filling the request pattern. */
    private String requestVariables;
    /** Output column to JSON-path mapping. */
    private Map<String, OutputMapping> outputMapping;
    /** Async-IO stream timeout in milliseconds. */
    private String streamTimeout;
    /** Connection timeout in milliseconds. */
    private String connectTimeout;
    /** Whether a failed lookup should fail the job. */
    private boolean failOnErrors;
    /** Comma-separated stencil URLs for descriptor resolution. */
    private String grpcStencilUrl;
    /** Optional output protobuf class name. */
    private String type;
    /** Whether to keep raw response values without type coercion. */
    private boolean retainResponseType;
    /** gRPC call metadata headers. */
    private Map<String, String> headers;
    /** Optional metric id grouping this source's metrics. */
    private String metricId;
    /** Maximum number of concurrent async requests. */
    private int capacity;

    /**
     * Sets the gRPC service endpoint.
     *
     * @param endpoint the endpoint host or address
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the gRPC service port.
     *
     * @param servicePort the service port
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setServicePort(int servicePort) {
        this.servicePort = servicePort;
        return this;
    }

    /**
     * Sets the request message protobuf class name.
     *
     * @param grpcRequestProtoSchema the request proto schema
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setGrpcRequestProtoSchema(String grpcRequestProtoSchema) {
        this.grpcRequestProtoSchema = grpcRequestProtoSchema;
        return this;
    }

    /**
     * Sets the response message protobuf class name.
     *
     * @param grpcResponseProtoSchema the response proto schema
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setGrpcResponseProtoSchema(String grpcResponseProtoSchema) {
        this.grpcResponseProtoSchema = grpcResponseProtoSchema;
        return this;
    }

    /**
     * Sets the fully-qualified gRPC method name to invoke.
     *
     * @param grpcMethodUrl the gRPC method URL
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setGrpcMethodUrl(String grpcMethodUrl) {
        this.grpcMethodUrl = grpcMethodUrl;
        return this;
    }

    /**
     * Sets the request body pattern.
     *
     * @param requestPattern the pattern populated with the request variables
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setRequestPattern(String requestPattern) {
        this.requestPattern = requestPattern;
        return this;
    }

    /**
     * Sets the comma-separated input columns substituted into the request pattern.
     *
     * @param requestVariables the input column names
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setRequestVariables(String requestVariables) {
        this.requestVariables = requestVariables;
        return this;
    }

    /**
     * Sets the output column to JSON-path mapping.
     *
     * @param outputMapping the mapping from output column name to its response path
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setOutputMapping(Map<String, OutputMapping> outputMapping) {
        this.outputMapping = outputMapping;
        return this;
    }

    /**
     * Sets the async-IO stream timeout.
     *
     * @param streamTimeout the stream timeout in milliseconds
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setStreamTimeout(String streamTimeout) {
        this.streamTimeout = streamTimeout;
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectTimeout the connect timeout in milliseconds
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets whether a failed lookup should fail the job.
     *
     * @param failOnErrors {@code true} to fail the job on lookup errors
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setFailOnErrors(boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
        return this;
    }

    /**
     * Sets the comma-separated stencil URLs used to resolve the request and response descriptors.
     *
     * @param grpcStencilUrl the gRPC stencil URLs
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setGrpcStencilUrl(String grpcStencilUrl) {
        this.grpcStencilUrl = grpcStencilUrl;
        return this;
    }

    /**
     * Sets the optional output protobuf class name.
     *
     * @param type the protobuf class name used to type the response
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Sets whether raw response values are kept without type coercion.
     *
     * @param retainResponseType {@code true} to retain the raw response type
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setRetainResponseType(boolean retainResponseType) {
        this.retainResponseType = retainResponseType;
        return this;
    }

    /**
     * Sets the gRPC call metadata headers.
     *
     * @param headers the headers to attach to each call
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Sets the optional metric id grouping this source's metrics.
     *
     * @param metricId the metric id
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setMetricId(String metricId) {
        this.metricId = metricId;
        return this;
    }

    /**
     * Sets the maximum number of concurrent async requests.
     *
     * @param capacity the async capacity
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Sets the channel keepalive ping interval.
     *
     * @param grpcArgKeepaliveTimeMs the keepalive time in milliseconds
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setGrpcArgKeepaliveTimeMs(String grpcArgKeepaliveTimeMs) {
        this.grpcArgKeepaliveTimeMs = grpcArgKeepaliveTimeMs;
        return this;
    }

    /**
     * Sets the channel keepalive ping timeout.
     *
     * @param grpcArgKeepaliveTimeoutMs the keepalive timeout in milliseconds
     * @return this builder for chaining
     */
    public GrpcSourceConfigBuilder setGrpcArgKeepaliveTimeoutMs(String grpcArgKeepaliveTimeoutMs) {
        this.grpcArgKeepaliveTimeoutMs = grpcArgKeepaliveTimeoutMs;
        return this;
    }

    /**
     * Builds a {@link GrpcSourceConfig} from the values accumulated in this builder.
     *
     * @return the assembled gRPC source config
     */
    public GrpcSourceConfig createGrpcSourceConfig() {
        return new GrpcSourceConfig(endpoint, servicePort, grpcRequestProtoSchema, grpcResponseProtoSchema, grpcMethodUrl, grpcArgKeepaliveTimeMs, grpcArgKeepaliveTimeoutMs, requestPattern, requestVariables,
                streamTimeout, connectTimeout, failOnErrors, grpcStencilUrl, type, retainResponseType, headers, outputMapping, metricId, capacity);
    }
}
