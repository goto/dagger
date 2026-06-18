package com.gotocompany.dagger.core.processors.external.grpc;

import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.types.StreamDecorator;
import com.gotocompany.dagger.core.processors.external.ExternalMetricConfig;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.util.concurrent.TimeUnit;

/**
 * The Decorator for Grpc stream.
 */
public class GrpcStreamDecorator implements StreamDecorator {

    /**
     * The gRPC source configuration passed to the connector created by this decorator.
     */
    private GrpcSourceConfig grpcSourceConfig;
    /**
     * The metric configuration applied to the connector, including telemetry and the metric id.
     */
    private final ExternalMetricConfig externalMetricConfig;
    /**
     * The schema configuration providing column and proto metadata to the connector.
     */
    private final SchemaConfig schemaConfig;


    /**
     * Instantiates a new Grpc stream decorator.
     *
     * @param grpcSourceConfig     the grpc source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     */
    public GrpcStreamDecorator(GrpcSourceConfig grpcSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig) {
        this.grpcSourceConfig = grpcSourceConfig;
        this.externalMetricConfig = externalMetricConfig;
        this.schemaConfig = schemaConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @return {@code true} when a gRPC source configuration is present
     */
    @Override
    public Boolean canDecorate() {
        return grpcSourceConfig != null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the input stream in an ordered asynchronous gRPC lookup, registering the telemetry subscriber
     * and applying the configured stream timeout and capacity.
     *
     * @param inputStream the stream to enrich with gRPC lookups
     * @return the asynchronously enriched stream
     */
    @Override
    public DataStream<Row> decorate(DataStream<Row> inputStream) {
        GrpcAsyncConnector grpcAsyncConnector = new GrpcAsyncConnector(grpcSourceConfig, externalMetricConfig, schemaConfig);
        grpcAsyncConnector.notifySubscriber(externalMetricConfig.getTelemetrySubscriber());
        return AsyncDataStream.orderedWait(inputStream, grpcAsyncConnector, grpcSourceConfig.getStreamTimeout(), TimeUnit.MILLISECONDS, grpcSourceConfig.getCapacity());
    }
}
