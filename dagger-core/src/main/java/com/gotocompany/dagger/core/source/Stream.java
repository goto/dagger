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

public class Stream implements Serializable {
    @Getter
    private final DaggerSource<Row> daggerSource;
    @Getter
    private final String streamName;

    Stream(DaggerSource<Row> daggerSource, String streamName) {
        this.daggerSource = daggerSource;
        this.streamName = streamName;
    }

    public DataStream<Row> registerSource(StreamExecutionEnvironment executionEnvironment, WatermarkStrategy<Row> watermarkStrategy) {
        return daggerSource.register(executionEnvironment, watermarkStrategy);
    }

    public static class Builder {
        private final StreamConfig streamConfig;
        private final Configuration configuration;
        private final StencilClientOrchestrator stencilClientOrchestrator;
        private final SerializedStatsDReporterSupplier statsDReporterSupplier;

        public Builder(StreamConfig streamConfig, Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, SerializedStatsDReporterSupplier statsDReporterSupplier) {
            this.streamConfig = streamConfig;
            this.configuration = configuration;
            this.stencilClientOrchestrator = stencilClientOrchestrator;
            this.statsDReporterSupplier = statsDReporterSupplier;
        }

        public Stream build() {
            DaggerDeserializer<Row> daggerDeserializer = DaggerDeserializerFactory.create(streamConfig, configuration, stencilClientOrchestrator, statsDReporterSupplier);
            DaggerSource<Row> daggerSource = DaggerSourceFactory.create(streamConfig, configuration, daggerDeserializer, statsDReporterSupplier);
            return new Stream(daggerSource, streamConfig.getSchemaTable());
        }
    }
}
