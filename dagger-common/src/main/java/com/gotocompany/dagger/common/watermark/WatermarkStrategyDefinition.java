package com.gotocompany.dagger.common.watermark;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.types.Row;

import java.io.Serializable;

/**
 * Strategy abstraction for building a Flink {@link WatermarkStrategy} over Dagger's {@link Row}
 * stream.
 *
 * <p>Implementations decide how event-time timestamps are extracted from each {@link Row} and how
 * watermarks are generated from them — for example from the last column, from a named
 * {@code rowtime} column, or not at all. The strategy is parameterized by an allowed
 * out-of-orderness delay so that late events within that bound are still treated as on time. It
 * extends {@link Serializable} because Flink ships the strategy to its distributed operators.
 */
public interface WatermarkStrategyDefinition extends Serializable {

    /**
     * Builds the Flink watermark strategy to assign to the {@link Row} stream.
     *
     * @param waterMarkDelayInMs the allowed out-of-orderness, in milliseconds, used when generating
     *                           bounded watermarks; implementations that emit no watermarks may
     *                           ignore this value
     * @return the watermark strategy to assign to the stream
     */
    WatermarkStrategy<Row> getWatermarkStrategy(long waterMarkDelayInMs);
}
