package com.gotocompany.dagger.common.watermark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.types.Row;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * {@link WatermarkStrategyDefinition} that derives event time from the last column of each row.
 *
 * <p>It builds a bounded-out-of-orderness strategy whose timestamp assigner reads the final field of
 * each {@link Row} (index {@code arity - 1}), expects it to be a {@link Timestamp}, and uses its
 * epoch-millis value as the event-time timestamp. This is appropriate when the stream's event-time
 * column is known to be appended as the last column of the schema.
 */
public class LastColumnWatermark implements WatermarkStrategyDefinition {

    /**
     * {@inheritDoc}
     *
     * <p>Returns a bounded-out-of-orderness strategy that extracts the event-time timestamp from the
     * last field of each {@link Row}, casting it to {@link Timestamp} and reading its epoch-millis
     * value.
     *
     * @param waterMarkDelayInMs the allowed out-of-orderness, in milliseconds, applied to the
     *                           generated watermarks
     * @return a watermark strategy keyed off the last column's timestamp
     */
    @Override
    public WatermarkStrategy<Row> getWatermarkStrategy(long waterMarkDelayInMs) {
        return WatermarkStrategy.
                <Row>forBoundedOutOfOrderness(Duration.ofMillis(waterMarkDelayInMs))
                .withTimestampAssigner((SerializableTimestampAssigner<Row>) (element, recordTimestamp) -> {
                    int index = element.getArity() - 1;
                    return ((Timestamp) element.getField(index)).getTime();
                });
    }
}
