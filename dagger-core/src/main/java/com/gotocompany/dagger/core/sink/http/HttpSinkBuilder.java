package com.gotocompany.dagger.core.sink.http;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.api.java.utils.ParameterTool;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.net.URL;
import java.net.MalformedURLException;

public class HttpSinkBuilder {
    private static final Logger LOGGER = Logger.getLogger(HttpSinkBuilder.class.getName());

    private String[] columnNames;
    private StencilClientOrchestrator stencilClientOrchestrator;
    private Configuration configuration;
    private DaggerStatsDReporter daggerStatsDReporter;
    private String endpoint;
    private int maxRetries;
    private long retryBackoffMs;
    private int connectTimeoutMs;
    private int readTimeoutMs;
    private String authType;
    private String authToken;
    private boolean compressionEnabled;
    private String compressionType;
    private int batchSize;
    private long flushIntervalMs;
    private boolean asyncEnabled;
    private int maxConnections;
    private boolean validateSslCertificate;
    private String proxyHost;
    private int proxyPort;

    private HttpSinkBuilder() {
        this.maxRetries = 3;
        this.retryBackoffMs = 1000;
        this.connectTimeoutMs = 5000;
        this.readTimeoutMs = 30000;
        this.authType = "None";
        this.compressionEnabled = false;
        this.compressionType = "gzip";
        this.batchSize = 100;
        this.flushIntervalMs = 1000;
        this.asyncEnabled = true;
        this.maxConnections = 10;
        this.validateSslCertificate = true;
    }

    public static HttpSinkBuilder create() {
        return new HttpSinkBuilder();
    }

    public HttpSink build() {
        validateConfiguration();
        ProtoSerializer protoSerializer = createProtoSerializer();
        Configuration conf = setDefaultValues(configuration);
        return new HttpSink(conf, protoSerializer, daggerStatsDReporter);
    }

    private ProtoSerializer createProtoSerializer() {
        return new ProtoSerializer(
                configuration.getString("SINK_CONNECTOR_SCHEMA_PROTO_KEY_CLASS", ""),
                configuration.getString("SINK_CONNECTOR_SCHEMA_PROTO_MESSAGE_CLASS", ""),
                columnNames,
                stencilClientOrchestrator);
    }

    private Configuration setDefaultValues(Configuration inputConf) {
        Map<String, String> configMap = new HashMap<>(inputConf.getParam().toMap());
        configMap.put("SCHEMA_REGISTRY_STENCIL_CACHE_AUTO_REFRESH", "false");
        configMap.put("SCHEMA_REGISTRY_STENCIL_CACHE_TTL_MS", "86400000");
        configMap.put("SCHEMA_REGISTRY_STENCIL_FETCH_RETRIES", "4");
        configMap.put("SCHEMA_REGISTRY_STENCIL_FETCH_BACKOFF_MIN_MS", "5000");
        configMap.put("SCHEMA_REGISTRY_STENCIL_REFRESH_STRATEGY", "LONG_POLLING");
        configMap.put("SCHEMA_REGISTRY_STENCIL_FETCH_TIMEOUT_MS", "60000");
        configMap.put("SCHEMA_REGISTRY_STENCIL_FETCH_HEADERS", "");
        configMap.put("SINK_METRICS_APPLICATION_PREFIX", "dagger_");
        configMap.put(Constants.SINK_HTTP_ENDPOINT_KEY, endpoint);
        configMap.put(Constants.SINK_HTTP_MAX_RETRIES, String.valueOf(maxRetries));
        configMap.put(Constants.SINK_HTTP_RETRY_BACKOFF_MS, String.valueOf(retryBackoffMs));
        configMap.put(Constants.SINK_HTTP_CONNECT_TIMEOUT_MS, String.valueOf(connectTimeoutMs));
        configMap.put(Constants.SINK_HTTP_READ_TIMEOUT_MS, String.valueOf(readTimeoutMs));
        configMap.put(Constants.SINK_HTTP_AUTH_TYPE, authType);
        configMap.put(Constants.SINK_HTTP_AUTH_TOKEN, authToken);
        configMap.put(Constants.SINK_HTTP_COMPRESSION_ENABLED, String.valueOf(compressionEnabled));
        configMap.put(Constants.SINK_HTTP_COMPRESSION_TYPE, compressionType);
        configMap.put(Constants.SINK_HTTP_BATCH_SIZE, String.valueOf(batchSize));
        configMap.put(Constants.SINK_HTTP_FLUSH_INTERVAL_MS, String.valueOf(flushIntervalMs));
        configMap.put(Constants.SINK_HTTP_ASYNC_ENABLED, String.valueOf(asyncEnabled));
        configMap.put(Constants.SINK_HTTP_MAX_CONNECTIONS, String.valueOf(maxConnections));
        configMap.put(Constants.SINK_HTTP_VALIDATE_SSL_CERTIFICATE, String.valueOf(validateSslCertificate));
        configMap.put(Constants.SINK_HTTP_PROXY_HOST, proxyHost);
        configMap.put(Constants.SINK_HTTP_PROXY_PORT, String.valueOf(proxyPort));
        return new Configuration(ParameterTool.fromMap(configMap));
    }

    private void validateConfiguration() {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("HTTP endpoint must be set");
        }
        validateEndpointUrl(endpoint);
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be greater than 0");
        }
        if (flushIntervalMs <= 0) {
            throw new IllegalArgumentException("Flush interval must be greater than 0");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries must be non-negative");
        }
        if (retryBackoffMs < 0) {
            throw new IllegalArgumentException("Retry backoff must be non-negative");
        }
        if (connectTimeoutMs <= 0) {
            throw new IllegalArgumentException("Connect timeout must be greater than 0");
        }
        if (readTimeoutMs <= 0) {
            throw new IllegalArgumentException("Read timeout must be greater than 0");
        }
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("Max connections must be greater than 0");
        }
        if (proxyPort < 0 || proxyPort > 65535) {
            throw new IllegalArgumentException("Proxy port must be between 0 and 65535");
        }
    }

    private void validateEndpointUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid endpoint URL: " + url, e);
        }
    }

    public HttpSinkBuilder setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public HttpSinkBuilder setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
        return this;
    }

    public HttpSinkBuilder setStencilClientOrchestrator(StencilClientOrchestrator stencilClientOrchestrator) {
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        return this;
    }

    public HttpSinkBuilder setDaggerStatsDReporter(DaggerStatsDReporter daggerStatsDReporter) {
        this.daggerStatsDReporter = daggerStatsDReporter;
        return this;
    }

    public HttpSinkBuilder setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public HttpSinkBuilder setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public HttpSinkBuilder setRetryBackoffMs(long retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
        return this;
    }

    public HttpSinkBuilder setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        return this;
    }

    public HttpSinkBuilder setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
        return this;
    }

    public HttpSinkBuilder setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public HttpSinkBuilder setAuthToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    public HttpSinkBuilder setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
        return this;
    }

    public HttpSinkBuilder setCompressionType(String compressionType) {
        this.compressionType = compressionType;
        return this;
    }

    public HttpSinkBuilder setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public HttpSinkBuilder setFlushIntervalMs(long flushIntervalMs) {
        this.flushIntervalMs = flushIntervalMs;
        return this;
    }

    public HttpSinkBuilder setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
        return this;
    }

    public HttpSinkBuilder setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public HttpSinkBuilder setValidateSslCertificate(boolean validateSslCertificate) {
        this.validateSslCertificate = validateSslCertificate;
        return this;
    }

    public HttpSinkBuilder setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public HttpSinkBuilder setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }
}
