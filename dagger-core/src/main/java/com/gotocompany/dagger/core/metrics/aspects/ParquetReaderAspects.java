package com.gotocompany.dagger.core.metrics.aspects;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;

/**
 * Metric aspects emitted by the Parquet file source reader as it reads and deserializes rows.
 *
 * <p>When Dagger sources data from Parquet files, each reader iterates a Parquet file, reads
 * {@code SimpleGroup} records, and deserializes them into Flink {@code Row} instances emitted
 * downstream. The constants below name the counters and histograms — reader lifecycle, rows emitted,
 * and per-row read/deserialization timings — that the StatsD-backed counter and histogram managers
 * report. The {@link AspectType} on each constant decides whether it is reported as a counter or a
 * histogram.
 */
public enum ParquetReaderAspects implements Aspects {
    /**
     * Counter incremented once each time a Parquet reader instance is created.
     */
    READER_CREATED("reader_created", AspectType.Counter),
    /**
     * Counter incremented once each time a Parquet reader instance is closed.
     */
    READER_CLOSED("reader_closed", AspectType.Counter),
    /**
     * Counter incremented for every row the reader emits downstream.
     */
    READER_ROWS_EMITTED("reader_rows_emitted", AspectType.Counter),
    /**
     * Histogram of the time, in milliseconds, taken to deserialize a single record into a Flink
     * {@code Row}.
     */
    READER_ROW_DESERIALIZATION_TIME("reader_row_deserialization_time", AspectType.Histogram),
    /**
     * Histogram of the time, in milliseconds, taken to read a single record from the Parquet file.
     */
    READER_ROW_READ_TIME("reader_row_read_time", AspectType.Histogram);

    /** The metric name published for this aspect on the Flink metric group. */
    private final String value;
    /** The kind of metric this aspect is reported as; see {@link AspectType}. */
    private final AspectType aspectType;

    /**
     * Binds the metric name and metric type for this aspect constant.
     *
     * @param value      the metric name to publish on the Flink metric group
     * @param aspectType the kind of metric this aspect is reported as
     */
    ParquetReaderAspects(String value, AspectType aspectType) {
        this.value = value;
        this.aspectType = aspectType;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the metric name configured for this constant (the first constructor argument).
     *
     * @return the metric name for this aspect
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the {@link AspectType} configured for this constant, which controls whether it is
     * registered and reported as a meter, histogram, gauge, or counter.
     *
     * @return the metric type for this aspect
     */
    @Override
    public AspectType getAspectType() {
        return aspectType;
    }
}
