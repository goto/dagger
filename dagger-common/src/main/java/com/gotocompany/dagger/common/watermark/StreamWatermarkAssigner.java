package com.gotocompany.dagger.common.watermark;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.io.Serializable;

/**
 * Applies a {@link WatermarkStrategyDefinition} to a Flink {@link DataStream} of {@link Row}
 * records.
 *
 * <p>This is the entry point Dagger uses to attach event-time timestamps and watermarks to a source
 * stream. It wraps a configured strategy definition and exposes overloads that either always assign
 * watermarks or skip assignment when per-partition watermarking is enabled (in which case
 * watermarks are expected to be assigned closer to the source instead). It is {@link Serializable}
 * so it can be embedded in the Flink job graph.
 */
public class StreamWatermarkAssigner implements Serializable {
    /** Strategy used to build the Flink watermark strategy applied to the stream. */
    private WatermarkStrategyDefinition watermarkStrategyDefinition;

    /**
     * Creates an assigner backed by the given watermark strategy definition.
     *
     * @param watermarkStrategyDefinition the strategy used to build the {@code WatermarkStrategy}
     *                                    applied to the stream
     */
    public StreamWatermarkAssigner(WatermarkStrategyDefinition watermarkStrategyDefinition) {
        this.watermarkStrategyDefinition = watermarkStrategyDefinition;
    }

    /**
     * Assigns timestamps and watermarks to the stream unless per-partition watermarking is enabled.
     *
     * <p>When {@code enablePerPartitionWatermark} is {@code false}, the configured strategy is
     * applied to the stream via {@code assignTimestampsAndWatermarks}. When it is {@code true}, the
     * input stream is returned unchanged, on the assumption that watermarks are assigned per
     * partition closer to the source instead.
     *
     * @param inputStream                 the source stream of {@link Row} records
     * @param watermarkDelayMs            the allowed out-of-orderness, in milliseconds, passed to the
     *                                    strategy definition
     * @param enablePerPartitionWatermark when {@code true} skip assignment here and return the input
     *                                    unchanged; when {@code false} assign watermarks now
     * @return the stream with watermarks assigned, or the unchanged input stream when per-partition
     *         watermarking is enabled
     */
    public DataStream<Row> assignTimeStampAndWatermark(DataStream<Row> inputStream, long watermarkDelayMs, boolean enablePerPartitionWatermark) {
        return !enablePerPartitionWatermark ? inputStream
                .assignTimestampsAndWatermarks(watermarkStrategyDefinition.getWatermarkStrategy(watermarkDelayMs)) : inputStream;
    }

    /**
     * Assigns timestamps and watermarks to the stream using the configured strategy.
     *
     * @param inputStream      the source stream of {@link Row} records
     * @param watermarkDelayMs the allowed out-of-orderness, in milliseconds, passed to the strategy
     *                         definition
     * @return the stream with event-time timestamps and watermarks assigned
     */
    public DataStream<Row> assignTimeStampAndWatermark(DataStream<Row> inputStream, long watermarkDelayMs) {
        return inputStream
                .assignTimestampsAndWatermarks(watermarkStrategyDefinition.getWatermarkStrategy(watermarkDelayMs));
    }

}

