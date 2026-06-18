package com.gotocompany.dagger.core.processors.external.es;

import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.types.StreamDecorator;
import com.gotocompany.dagger.core.processors.external.ExternalMetricConfig;

import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.util.concurrent.TimeUnit;

/**
 * The decorator for ElasticSearch stream.
 */
public class EsStreamDecorator implements StreamDecorator {

    /**
     * The Elasticsearch source configuration passed to the connector created by this decorator.
     */
    private final EsSourceConfig esSourceConfig;
    /**
     * The metric configuration applied to the connector, including telemetry and the metric id.
     */
    private final ExternalMetricConfig externalMetricConfig;
    /**
     * The schema configuration providing column and proto metadata to the connector.
     */
    private final SchemaConfig schemaConfig;

    /**
     * Instantiates a new ElasticSearch stream decorator.
     *
     * @param esSourceConfig       the es source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     */
    public EsStreamDecorator(EsSourceConfig esSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig) {
        this.esSourceConfig = esSourceConfig;
        this.externalMetricConfig = externalMetricConfig;
        this.schemaConfig = schemaConfig;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} when an Elasticsearch source configuration is present
     */
    @Override
    public Boolean canDecorate() {
        return esSourceConfig != null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the input stream in an ordered asynchronous Elasticsearch lookup, registering the telemetry
     * subscriber and applying the configured stream timeout and capacity.
     *
     * @param inputStream the stream to enrich with Elasticsearch lookups
     * @return the asynchronously enriched stream
     */
    @Override
    public DataStream<Row> decorate(DataStream<Row> inputStream) {
        EsAsyncConnector esAsyncConnector = new EsAsyncConnector(esSourceConfig, externalMetricConfig, schemaConfig);
        esAsyncConnector.notifySubscriber(externalMetricConfig.getTelemetrySubscriber());
        return AsyncDataStream.orderedWait(inputStream, esAsyncConnector, esSourceConfig.getStreamTimeout(), TimeUnit.MILLISECONDS, esSourceConfig.getCapacity());
    }
}
