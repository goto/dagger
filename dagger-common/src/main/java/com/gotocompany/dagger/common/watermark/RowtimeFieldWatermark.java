package com.gotocompany.dagger.common.watermark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.types.Row;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Arrays;

/**
 * {@link WatermarkStrategyDefinition} that derives event time from a column named {@code rowtime}.
 *
 * <p>Given the stream's ordered column names, it locates the position of the {@code rowtime} column
 * and builds a bounded-out-of-orderness strategy whose timestamp assigner reads that field from each
 * {@link Row}, expects a {@link Timestamp}, and uses its epoch-millis value as the event-time
 * timestamp. Use this when the event-time column is identified by name rather than by position.
 */
public class RowtimeFieldWatermark implements WatermarkStrategyDefinition {
    /** Conventional name of the event-time column this strategy looks up. */
    private static final String ROWTIME = "rowtime";
    /** Ordered stream column names, used to resolve the index of the {@code rowtime} column. */
    private final String[] columnNames;

    /**
     * Creates a strategy definition that reads event time from the {@code rowtime} column.
     *
     * @param columnNames the ordered column names of the stream, used to resolve the index of the
     *                    {@code rowtime} column
     */
    public RowtimeFieldWatermark(String[] columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a bounded-out-of-orderness strategy that extracts the event-time timestamp from the
     * field at the index of the {@code rowtime} column (resolved from the configured column names),
     * casting it to {@link Timestamp} and reading its epoch-millis value.
     *
     * @param waterMarkDelayInMs the allowed out-of-orderness, in milliseconds, applied to the
     *                           generated watermarks
     * @return a watermark strategy keyed off the {@code rowtime} column's timestamp
     */
    @Override
    public WatermarkStrategy<Row> getWatermarkStrategy(long waterMarkDelayInMs) {
        return WatermarkStrategy.
                <Row>forBoundedOutOfOrderness(Duration.ofMillis(waterMarkDelayInMs))
                .withTimestampAssigner((SerializableTimestampAssigner<Row>)
                        (element, recordTimestamp) -> ((Timestamp) element.getField(Arrays.asList(columnNames).indexOf(ROWTIME))).getTime());
    }
}
