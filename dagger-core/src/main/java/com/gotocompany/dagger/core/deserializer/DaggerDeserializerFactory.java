package com.gotocompany.dagger.core.deserializer;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.core.exception.DaggerConfigurationException;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.StatsDErrorReporter;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import org.apache.flink.types.Row;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory that selects and builds the {@link DaggerDeserializer} appropriate for a stream's source
 * and data type.
 *
 * <p>It evaluates the known providers in order — JSON, Protobuf over Kafka, and Protobuf over
 * Parquet — and returns the deserializer from the first provider that can handle the supplied
 * {@link StreamConfig}. When no provider matches, the failure is reported through StatsD and a
 * {@link DaggerConfigurationException} is thrown.
 */
public class DaggerDeserializerFactory {
    /**
     * Builds the deserializer that matches the given stream configuration.
     *
     * @param streamConfig              the stream configuration describing the source(s) and data type
     * @param configuration             the job configuration passed through to the providers
     * @param stencilClientOrchestrator the Stencil orchestrator used by Protobuf-based providers
     * @param statsDReporterSupplier    supplies the StatsD reporter used to report a fatal error when
     *                                  no compatible deserializer is found
     * @return the deserializer produced by the first compatible provider
     * @throws DaggerConfigurationException if no provider can deserialize the configured stream
     */
    public static DaggerDeserializer<Row> create(StreamConfig streamConfig, Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, SerializedStatsDReporterSupplier statsDReporterSupplier) {
        return getDaggerDeserializerProviders(streamConfig, configuration, stencilClientOrchestrator)
                .stream()
                .filter(DaggerDeserializerProvider::canProvide)
                .findFirst()
                .orElseThrow(() -> {
                    StatsDErrorReporter statsDErrorReporter = new StatsDErrorReporter(statsDReporterSupplier);
                    DaggerConfigurationException ex = new DaggerConfigurationException("No suitable deserializer could be constructed for the given stream configuration.");
                    statsDErrorReporter.reportFatalException(ex);
                    return ex;
                })
                .getDaggerDeserializer();
    }

    /**
     * Builds the ordered list of candidate deserializer providers to evaluate.
     *
     * <p>The order defines precedence when more than one provider could match: JSON first, then
     * Protobuf over Kafka, then Protobuf over Parquet.
     *
     * @param streamConfig              the stream configuration handed to each provider
     * @param configuration             the job configuration handed to each provider
     * @param stencilClientOrchestrator the Stencil orchestrator handed to the Protobuf-based providers
     * @return the list of providers in evaluation order
     */
    private static List<DaggerDeserializerProvider<Row>> getDaggerDeserializerProviders(StreamConfig streamConfig, Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator) {
        return Stream.of(
                        new JsonDeserializerProvider(streamConfig),
                        new ProtoDeserializerProvider(streamConfig, configuration, stencilClientOrchestrator),
                        new SimpleGroupDeserializerProvider(streamConfig, configuration, stencilClientOrchestrator))
                .collect(Collectors.toList());
    }
}
