package com.gotocompany.dagger.core.source;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.source.config.StreamConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory that materializes all configured input streams of a Dagger job.
 *
 * <p>It parses the {@code STREAMS} configuration into one {@link StreamConfig} per stream and uses
 * {@link Stream.Builder} to construct a ready-to-register {@link Stream} for each entry.
 */
public class StreamsFactory {
    /**
     * Parses the stream configuration and builds every declared {@link Stream}.
     *
     * @param configuration             the global Dagger job configuration containing the
     *                                  {@code STREAMS} definition
     * @param stencilClientOrchestrator the Stencil orchestrator used to resolve Protobuf
     *                                  descriptors for each stream's deserializer
     * @param statsDReporterSupplier    supplier of the StatsD reporter used for error/metric reporting
     * @return the list of fully built streams, one per entry in the {@code STREAMS} configuration
     */
    public static List<Stream> getStreams(Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, SerializedStatsDReporterSupplier statsDReporterSupplier) {
        StreamConfig[] streamConfigs = StreamConfig.parse(configuration);
        ArrayList<Stream> streams = new ArrayList<>();

        for (StreamConfig streamConfig : streamConfigs) {
            Stream.Builder builder = new Stream.Builder(streamConfig, configuration, stencilClientOrchestrator, statsDReporterSupplier);
            streams.add(builder.build());
        }
        return streams;
    }
}
