package com.gotocompany.dagger.core.source.parquet.splitassigner;

import lombok.Getter;
import org.apache.flink.connector.file.src.FileSourceSplit;

import java.io.Serializable;
import java.time.Instant;

/**
 * A value object pairing a Flink {@link FileSourceSplit} with the {@link Instant} parsed from its
 * file path.
 *
 * <p>Used by {@link ChronologyOrderedSplitAssigner} to keep file splits in a priority queue ordered
 * by their event time so that Parquet files are processed chronologically. It is
 * {@link Serializable} so it can participate in the Flink job graph.
 */
public class InstantEnrichedSplit implements Serializable {
    /**
     * The wrapped Flink file source split.
     */
    @Getter
    private final FileSourceSplit fileSourceSplit;
    /**
     * The event-time instant derived from the split's file path, used for ordering.
     */
    @Getter
    private final Instant instant;

    /**
     * Creates a split enriched with its parsed event-time instant.
     *
     * @param fileSourceSplit the Flink file source split being wrapped
     * @param instant         the instant parsed from the split's file path
     */
    public InstantEnrichedSplit(FileSourceSplit fileSourceSplit, Instant instant) {
        this.fileSourceSplit = fileSourceSplit;
        this.instant = instant;
    }
}
