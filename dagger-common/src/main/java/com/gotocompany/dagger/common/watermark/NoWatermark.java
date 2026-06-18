package com.gotocompany.dagger.common.watermark;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.types.Row;

/**
 * {@link WatermarkStrategyDefinition} that disables watermarking entirely.
 *
 * <p>It returns Flink's {@link WatermarkStrategy#noWatermarks()} strategy, so no event-time
 * watermarks are generated for the {@link Row} stream. This is appropriate for pipelines that do not
 * rely on event-time progress, such as purely processing-time jobs.
 */
public class NoWatermark implements WatermarkStrategyDefinition {
    /**
     * {@inheritDoc}
     *
     * <p>Always returns {@link WatermarkStrategy#noWatermarks()}; the {@code waterMarkDelayInMs}
     * argument is ignored because no watermarks are emitted.
     *
     * @param waterMarkDelayInMs ignored, since this strategy emits no watermarks
     * @return a no-op watermark strategy that never advances event time
     */
    @Override
    public WatermarkStrategy<Row> getWatermarkStrategy(long waterMarkDelayInMs) {
        return WatermarkStrategy.noWatermarks();
    }
}
