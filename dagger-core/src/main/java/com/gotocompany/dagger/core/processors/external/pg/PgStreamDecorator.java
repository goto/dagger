package com.gotocompany.dagger.core.processors.external.pg;

import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.types.StreamDecorator;
import com.gotocompany.dagger.core.processors.external.ExternalMetricConfig;

import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.util.concurrent.TimeUnit;

/**
 * The Decorator for Postgre stream.
 */
public class PgStreamDecorator implements StreamDecorator {


    /**
     * Configuration describing the Postgres connection, query and output mapping to enrich the stream with.
     */
    private final PgSourceConfig pgSourceConfig;
    /**
     * Metric configuration carrying the telemetry settings shared with the created connector.
     */
    private final ExternalMetricConfig externalMetricConfig;
    /**
     * Schema configuration providing the descriptors and column metadata for the stream.
     */
    private final SchemaConfig schemaConfig;

    /**
     * Instantiates a new Postgre stream decorator.
     *
     * @param pgSourceConfig       the pg source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     */
    public PgStreamDecorator(PgSourceConfig pgSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig) {
        this.pgSourceConfig = pgSourceConfig;
        this.externalMetricConfig = externalMetricConfig;
        this.schemaConfig = schemaConfig;
    }

    /**
     * {@inheritDoc}
     *
     * <p>A Postgres decorator can decorate the stream only when a {@code PgSourceConfig} has been configured.
     *
     * @return {@code true} when the Postgres source config is present, {@code false} otherwise
     */
    @Override
    public Boolean canDecorate() {
        return pgSourceConfig != null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the input stream in an order-preserving asynchronous operator backed by a
     * {@link PgAsyncConnector}, using the configured stream timeout and capacity. The connector is
     * subscribed to the telemetry subscriber before the operator is created.
     *
     * @param inputStream the stream of {@link Row} records to enrich with Postgres lookups
     * @return the decorated stream emitting enriched {@link Row} records
     */
    @Override
    public DataStream<Row> decorate(DataStream<Row> inputStream) {
        PgAsyncConnector pgAsyncConnector = new PgAsyncConnector(pgSourceConfig, externalMetricConfig, schemaConfig);
        pgAsyncConnector.notifySubscriber(externalMetricConfig.getTelemetrySubscriber());
        return AsyncDataStream.orderedWait(inputStream, pgAsyncConnector, pgSourceConfig.getStreamTimeout(), TimeUnit.MILLISECONDS, pgSourceConfig.getCapacity());
    }
}
