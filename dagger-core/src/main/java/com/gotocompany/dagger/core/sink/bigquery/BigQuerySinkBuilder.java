package com.gotocompany.dagger.core.sink.bigquery;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import org.apache.flink.api.java.utils.ParameterTool;

import java.util.HashMap;
import java.util.Map;

/**
 * Fluent builder for {@link BigQuerySink}.
 *
 * <p>Collects the output column names, the Stencil client orchestrator, the job
 * {@link Configuration} and the StatsD reporter, then on {@link #build()} constructs the
 * {@link ProtoSerializer} used to encode rows and overlays a set of opinionated default settings
 * (Stencil caching/refresh behaviour and BigQuery storage options) before instantiating the sink.
 * Obtain an instance through {@link #create()} and chain the {@code set*} methods.
 */
public class BigQuerySinkBuilder {

    private String[] columnNames;
    private StencilClientOrchestrator stencilClientOrchestrator;
    private Configuration configuration;
    private DaggerStatsDReporter daggerStatsDReporter;

    /**
     * Creates an empty builder; use {@link #create()} to obtain instances.
     */
    private BigQuerySinkBuilder() {
    }

    /**
     * Creates a new, empty builder.
     *
     * @return a fresh {@link BigQuerySinkBuilder}
     */
    public static BigQuerySinkBuilder create() {
        return new BigQuerySinkBuilder();
    }

    /**
     * Builds the {@link BigQuerySink} from the configured values.
     *
     * <p>Constructs a {@link ProtoSerializer} from the configured proto key/message classes
     * ({@code SINK_CONNECTOR_SCHEMA_PROTO_KEY_CLASS} and
     * {@code SINK_CONNECTOR_SCHEMA_PROTO_MESSAGE_CLASS}), the column names and the Stencil client
     * orchestrator, applies the enforced defaults from {@link #setDefaultValues(Configuration)} and
     * returns the assembled sink.
     *
     * @return a configured {@link BigQuerySink}
     */
    public BigQuerySink build() {
        ProtoSerializer protoSerializer = new ProtoSerializer(
                configuration.getString("SINK_CONNECTOR_SCHEMA_PROTO_KEY_CLASS", ""),
                configuration.getString("SINK_CONNECTOR_SCHEMA_PROTO_MESSAGE_CLASS", ""),
                columnNames,
                stencilClientOrchestrator);
        Configuration conf = setDefaultValues(configuration);
        return new BigQuerySink(conf, protoSerializer, daggerStatsDReporter);
    }

    /**
     * Returns a copy of the given configuration with BigQuery-sink defaults overlaid.
     *
     * <p>Forces a fixed set of Stencil schema-registry caching, refresh, retry and timeout options
     * and enables BigQuery storage-API writes with row-insert-id disabled and a {@code dagger_}
     * metrics prefix. These values take precedence over the corresponding keys in the input
     * configuration.
     *
     * @param inputConf the original job configuration whose parameters are copied
     * @return a new {@link Configuration} containing the input values plus the enforced defaults
     */
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
        configMap.put("SINK_BIGQUERY_ROW_INSERT_ID_ENABLE", "false");
        configMap.put("SINK_BIGQUERY_STORAGE_API_ENABLE", "true");
        return new Configuration(ParameterTool.fromMap(configMap));
    }

    /**
     * Sets the job configuration that drives proto serialization and sink behaviour.
     *
     * @param configuration the job configuration
     * @return this builder, for chaining
     */
    public BigQuerySinkBuilder setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the output column names that are mapped onto the protobuf message fields.
     *
     * @param columnNames the output column names
     * @return this builder, for chaining
     */
    public BigQuerySinkBuilder setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
        return this;
    }

    /**
     * Sets the Stencil client orchestrator used to resolve protobuf descriptors during serialization.
     *
     * @param stencilClientOrchestrator the Stencil client orchestrator
     * @return this builder, for chaining
     */
    public BigQuerySinkBuilder setStencilClientOrchestrator(StencilClientOrchestrator stencilClientOrchestrator) {
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        return this;
    }

    /**
     * Sets the StatsD reporter forwarded to the Depot BigQuery sink for metrics.
     *
     * @param daggerStatsDReporter the StatsD reporter
     * @return this builder, for chaining
     */
    public BigQuerySinkBuilder setDaggerStatsDReporter(DaggerStatsDReporter daggerStatsDReporter) {
        this.daggerStatsDReporter = daggerStatsDReporter;
        return this;
    }
}
