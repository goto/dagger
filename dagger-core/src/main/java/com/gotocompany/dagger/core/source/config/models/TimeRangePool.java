package com.gotocompany.dagger.core.source.config.models;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable, ordered collection of {@link TimeRange} windows used to scope Parquet ingestion.
 *
 * <p>When a Dagger stream reads from a Parquet source, its configured date range is deserialized (via
 * {@code FileDateRangeAdaptor}) into one of these pools. The pool then answers whether a given event
 * {@link Instant} falls inside any configured window, which drives which timestamped Parquet files are
 * selected for processing. Implementing {@link Serializable} lets it travel with Flink source state.
 */
public class TimeRangePool implements Serializable {
    /**
     * Creates an empty pool that initially contains no time ranges.
     */
    public TimeRangePool() {
        this.timeRanges = new ArrayList<>();
    }

    /** Backing list of configured time windows, in the order they were added. */
    @Getter
    private List<TimeRange> timeRanges;

    /**
     * Appends a time range to the pool.
     *
     * @param timeRange the window to add
     * @return {@code true}, since the backing {@link List} always grows
     */
    public boolean add(TimeRange timeRange) {
        return timeRanges.add(timeRange);
    }

    /**
     * Reports whether the given instant falls within any window in this pool.
     *
     * @param instant the timestamp to test
     * @return {@code true} if at least one {@link TimeRange} contains {@code instant}, else {@code false}
     */
    public boolean contains(Instant instant) {
        return timeRanges.stream().anyMatch(timeRange -> timeRange.contains(instant));
    }
}
