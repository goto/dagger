package com.gotocompany.dagger.core.source;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.core.deserializer.DaggerDeserializerFactory;
import lombok.Getter;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

import java.io.Serializable;

/**
 * Represents a single configured input stream of a Dagger job, pairing the {@link DaggerSource}
 * that produces records with the logical schema/table name those records are registered under.
 *
 * <p>One {@code Stream} is created per entry in the {@code STREAMS} configuration. It is
 * {@link Serializable} so that it can be captured as part of the Flink job graph. Instances are
 * created through the nested {@link Builder}.
 */
public class Stream implements Serializable {
    /**
     * The source responsible for producing this stream's records as Flink {@code Row}s.
     */
    @Getter
    private final DaggerSource<Row> daggerSource;
    /**
     * The logical name (schema/table) the stream is registered under; used as the source/table
     * name when the stream is added to the Flink execution environment.
     */
    @Getter
    private final String streamName;

    /**
     * Creates a stream binding a source to its registration name.
     *
     * @param daggerSource the source that produces this stream's records
     * @param streamName   the schema/table name the stream is registered under
     */
    Stream(DaggerSource<Row> daggerSource, String streamName) {
        this.daggerSource = daggerSource;
        this.streamName = streamName;
    }

    /**
     * Registers this stream's source onto the given execution environment.
     *
     * <p>Delegates to {@link DaggerSource#register(StreamExecutionEnvironment, WatermarkStrategy)},
     * applying the supplied watermark strategy to the produced records.
     *
     * @param executionEnvironment the Flink execution environment to attach the source to
     * @param watermarkStrategy    the watermark strategy applied to the emitted records
     * @return the {@code DataStream} of {@code Row}s emitted by the underlying source
     */
    public DataStream<Row> registerSource(StreamExecutionEnvironment executionEnvironment, WatermarkStrategy<Row> watermarkStrategy) {
        return daggerSource.register(executionEnvironment, watermarkStrategy);
    }

    /**
     * Assembles a fully wired {@link Stream} from a {@link StreamConfig}.
     *
     * <p>On {@link #build()} the builder first creates the appropriate deserializer via
     * {@link DaggerDeserializerFactory} and then resolves the matching source via
     * {@link DaggerSourceFactory}, naming the stream after the configured schema table.
     */
    public static class Builder {
        /**
         * The per-stream configuration the stream is built from.
         */
        private final StreamConfig streamConfig;
        /**
         * The global Dagger job configuration.
         */
        private final Configuration configuration;
        /**
         * The Stencil orchestrator used to resolve Protobuf descriptors for the deserializer.
         */
        private final StencilClientOrchestrator stencilClientOrchestrator;
        /**
         * Supplier of the StatsD reporter used for error and metric reporting.
         */
        private final SerializedStatsDReporterSupplier statsDReporterSupplier;

        /**
         * Creates a builder holding the inputs required to construct a {@link Stream}.
         *
         * @param streamConfig              the per-stream configuration to build from
         * @param configuration             the global Dagger job configuration
         * @param stencilClientOrchestrator the Stencil orchestrator used to resolve Protobuf
         *                                  descriptors for the deserializer
         * @param statsDReporterSupplier    supplier of the StatsD reporter for error/metric reporting
         */
        public Builder(StreamConfig streamConfig, Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, SerializedStatsDReporterSupplier statsDReporterSupplier) {
            this.streamConfig = streamConfig;
            this.configuration = configuration;
            this.stencilClientOrchestrator = stencilClientOrchestrator;
            this.statsDReporterSupplier = statsDReporterSupplier;
        }

        /**
         * Creates the deserializer and the matching source and assembles the {@link Stream}.
         *
         * @return a new {@code Stream} whose source is selected by {@link DaggerSourceFactory} and
         *         whose name is the configured schema table
         */
        public Stream build() {
            DaggerDeserializer<Row> daggerDeserializer = DaggerDeserializerFactory.create(streamConfig, configuration, stencilClientOrchestrator, statsDReporterSupplier);
            DaggerSource<Row> daggerSource = DaggerSourceFactory.create(streamConfig, configuration, daggerDeserializer, statsDReporterSupplier);
            return new Stream(daggerSource, streamConfig.getSchemaTable());
        }
    }
}
