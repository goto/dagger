package com.gotocompany.dagger.core.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Abstraction over a concrete data source that can feed records into a Dagger Flink job.
 *
 * <p>Each implementation wraps a specific Flink source connector — for example the modern
 * {@code KafkaSource}, the legacy {@code FlinkKafkaConsumer}, or a bounded Parquet
 * {@code FileSource} — and knows both how to register itself onto a Flink
 * {@link StreamExecutionEnvironment} and whether it is applicable to the current stream
 * configuration. {@link DaggerSourceFactory} iterates over the available implementations and
 * picks the first one whose {@link #canBuild()} returns {@code true} for the configured
 * {@code SOURCE_DETAILS}.
 *
 * @param <T> the type of records produced by the source; within Dagger this is always
 *            {@code Row}
 */
public interface DaggerSource<T> {
    /**
     * Builds the underlying Flink source and attaches it to the given execution environment.
     *
     * @param executionEnvironment the Flink {@link StreamExecutionEnvironment} the source is added to
     * @param watermarkStrategy    the watermark strategy used to assign event-time timestamps and
     *                             emit watermarks for the produced records
     * @return the {@code DataStream} of records emitted by this source
     */
    DataStream<T> register(StreamExecutionEnvironment executionEnvironment, WatermarkStrategy<T> watermarkStrategy);

    /**
     * Indicates whether this source can be constructed for the current stream configuration.
     *
     * <p>Implementations typically inspect the configured {@code SOURCE_DETAILS} (source name and
     * source type) together with the supplied deserializer to decide applicability. It is used by
     * {@link DaggerSourceFactory} to select exactly one source per stream.
     *
     * @return {@code true} if this source matches the configuration and can be built;
     *         {@code false} otherwise
     */
    boolean canBuild();
}
