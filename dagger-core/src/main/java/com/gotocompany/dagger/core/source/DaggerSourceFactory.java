package com.gotocompany.dagger.core.source;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.exception.InvalidDaggerSourceException;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.StatsDErrorReporter;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import com.gotocompany.dagger.core.source.flinkkafkaconsumer.FlinkKafkaConsumerDaggerSource;
import com.gotocompany.dagger.core.source.kafka.KafkaDaggerSource;
import com.gotocompany.dagger.core.source.parquet.ParquetDaggerSource;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import org.apache.flink.types.Row;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory that selects and instantiates the {@link DaggerSource} implementation appropriate for a
 * given stream.
 *
 * <p>Dagger supports multiple source connectors (Kafka via the modern {@code KafkaSource}, the
 * legacy {@code FlinkKafkaConsumer}, and a bounded Parquet {@code FileSource}). This factory
 * constructs one candidate of each and returns the first whose {@link DaggerSource#canBuild()}
 * reports that it matches the configured {@code SOURCE_DETAILS}.
 */
public class DaggerSourceFactory {

    /**
     * Creates the single {@link DaggerSource} that matches the supplied stream configuration.
     *
     * <p>All candidate sources are built and the first one whose {@link DaggerSource#canBuild()}
     * returns {@code true} is returned. If none are applicable, an
     * {@link InvalidDaggerSourceException} is reported to StatsD and then thrown.
     *
     * @param streamConfig           the per-stream configuration describing the source details
     * @param configuration          the global Dagger job configuration
     * @param deserializer           the deserializer that converts raw source records into {@code Row}
     * @param statsDReporterSupplier supplier of the StatsD reporter used to report a fatal error
     *                               when no source can be built
     * @return the first applicable {@code DaggerSource} of {@code Row}
     * @throws InvalidDaggerSourceException if no configured source can handle the {@code SOURCE_DETAILS}
     */
    public static DaggerSource<Row> create(StreamConfig streamConfig, Configuration configuration, DaggerDeserializer<Row> deserializer, SerializedStatsDReporterSupplier statsDReporterSupplier) {
        List<DaggerSource<Row>> daggerSources = getDaggerSources(streamConfig, configuration, deserializer, statsDReporterSupplier);
        return daggerSources.stream()
                .filter(DaggerSource::canBuild)
                .findFirst()
                .orElseThrow(() -> {
                    StatsDErrorReporter statsDErrorReporter = new StatsDErrorReporter(statsDReporterSupplier);
                    String sourceDetails = Arrays.toString(streamConfig.getSourceDetails());
                    InvalidDaggerSourceException ex = new InvalidDaggerSourceException(String.format("No suitable DaggerSource can be created as per SOURCE_DETAILS config %s", sourceDetails));
                    statsDErrorReporter.reportFatalException(ex);
                    return ex;
                });
    }

    /**
     * Builds the ordered list of candidate sources considered by {@link #create}.
     *
     * <p>The order is significant: the {@code KafkaSource}-based source is preferred over the
     * legacy {@code FlinkKafkaConsumer} source, which in turn precedes the bounded Parquet source.
     * Only the first candidate reporting {@link DaggerSource#canBuild()} is ultimately used.
     *
     * @param streamConfig           the per-stream configuration describing the source details
     * @param configuration          the global Dagger job configuration
     * @param deserializer           the deserializer shared by all candidate sources
     * @param statsDReporterSupplier supplier of the StatsD reporter passed to sources that need it
     * @return the list of candidate {@code DaggerSource} instances in selection-priority order
     */
    private static List<DaggerSource<Row>> getDaggerSources(StreamConfig streamConfig, Configuration configuration, DaggerDeserializer<Row> deserializer, SerializedStatsDReporterSupplier statsDReporterSupplier) {
        KafkaDaggerSource kafkaDaggerSource = new KafkaDaggerSource(streamConfig, configuration, deserializer);
        FlinkKafkaConsumerDaggerSource flinkKafkaConsumerDaggerSource = new FlinkKafkaConsumerDaggerSource(streamConfig, configuration, deserializer);
        ParquetDaggerSource parquetDaggerSource = new ParquetDaggerSource(streamConfig, configuration, deserializer, statsDReporterSupplier);
        return Stream.of(kafkaDaggerSource, flinkKafkaConsumerDaggerSource, parquetDaggerSource)
                .collect(Collectors.toList());
    }
}
