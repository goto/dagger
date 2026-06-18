package com.gotocompany.dagger.core.processors.external.http;

import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.types.StreamDecorator;
import com.gotocompany.dagger.core.processors.external.ExternalMetricConfig;

import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.util.concurrent.TimeUnit;

/**
 * The decorator for Http stream.
 */
public class HttpStreamDecorator implements StreamDecorator {

    /**
     * Configuration describing the HTTP endpoint, verb and output mapping to enrich the stream with.
     */
    private final HttpSourceConfig httpSourceConfig;
    /**
     * Metric configuration carrying the telemetry settings shared with the created connector.
     */
    private final ExternalMetricConfig externalMetricConfig;
    /**
     * Schema configuration providing the descriptors and column metadata for the stream.
     */
    private final SchemaConfig schemaConfig;

    /**
     * Instantiates a new Http stream decorator.
     *
     * @param httpSourceConfig     the http source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     */
    public HttpStreamDecorator(HttpSourceConfig httpSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig) {
        this.httpSourceConfig = httpSourceConfig;
        this.externalMetricConfig = externalMetricConfig;
        this.schemaConfig = schemaConfig;
    }

    /**
     * {@inheritDoc}
     *
     * <p>An HTTP decorator can decorate the stream only when an {@code HttpSourceConfig} has been configured.
     *
     * @return {@code true} when the HTTP source config is present, {@code false} otherwise
     */
    @Override
    public Boolean canDecorate() {
        return httpSourceConfig != null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the input stream in an order-preserving asynchronous operator backed by a
     * {@link HttpAsyncConnector}, using the configured stream timeout and capacity. The connector is
     * subscribed to the telemetry subscriber before the operator is created.
     *
     * @param inputStream the stream of {@link Row} records to enrich with HTTP responses
     * @return the decorated stream emitting enriched {@link Row} records
     */
    @Override
    public DataStream<Row> decorate(DataStream<Row> inputStream) {
        HttpAsyncConnector httpAsyncConnector = new HttpAsyncConnector(httpSourceConfig, externalMetricConfig, schemaConfig);
        httpAsyncConnector.notifySubscriber(externalMetricConfig.getTelemetrySubscriber());
        return AsyncDataStream.orderedWait(inputStream, httpAsyncConnector, httpSourceConfig.getStreamTimeout(), TimeUnit.MILLISECONDS, httpSourceConfig.getCapacity());
    }
}
