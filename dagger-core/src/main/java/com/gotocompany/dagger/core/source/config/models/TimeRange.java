package com.gotocompany.dagger.core.source.config.models;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

/**
 * Immutable, serializable closed time window bounded by an inclusive start and end {@link Instant}.
 *
 * <p>Time ranges are aggregated in a {@link TimeRangePool} to express which Parquet files a Dagger
 * stream should ingest, based on the timestamps encoded in their paths. The class-level
 * {@code @Getter} exposes Lombok-generated accessors for both bounds, and implementing
 * {@link Serializable} lets the range travel with Flink source state.
 */
@Getter
public class TimeRange implements Serializable {
    /**
     * Creates a time range with the given inclusive start and end bounds.
     *
     * @param startInstant the start of the window (inclusive)
     * @param endInstant   the end of the window (inclusive)
     */
    public TimeRange(Instant startInstant, Instant endInstant) {
        this.startInstant = startInstant;
        this.endInstant = endInstant;
    }

    /** Inclusive start of the window. */
    private Instant startInstant;

    /** Inclusive end of the window. */
    private Instant endInstant;

    /**
     * Reports whether the given instant lies within this window, inclusive of both bounds.
     *
     * @param instant the timestamp to test
     * @return {@code true} when {@code instant} equals either bound or lies strictly between them
     */
    public boolean contains(Instant instant) {
        return instant.equals(startInstant) || instant.equals(endInstant) || (instant.isAfter(startInstant) && instant.isBefore(endInstant));
    }
}
