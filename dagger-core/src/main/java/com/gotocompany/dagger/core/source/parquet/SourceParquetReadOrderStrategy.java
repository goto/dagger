package com.gotocompany.dagger.core.source.parquet;

import com.google.gson.annotations.SerializedName;

import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_PARQUET_READ_ORDER_STRATEGY_EARLIEST_INDEX_FIRST;
import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_PARQUET_READ_ORDER_STRATEGY_EARLIEST_TIME_URL_FIRST;

/**
 * Strategy controlling the order in which discovered Parquet files (splits) are handed to readers.
 *
 * <p>Configured via the parquet read-order stream property; the {@link SerializedName} annotations
 * bind each constant to its configuration string during Gson deserialization.
 * {@code ParquetDaggerSource} uses the selected strategy to pick the matching split assigner.
 */
public enum SourceParquetReadOrderStrategy {
    /**
     * Process splits in ascending chronological order of the timestamp parsed from each file path
     * (earliest partition time first), backed by the {@code ChronologyOrderedSplitAssigner}.
     */
    @SerializedName(STREAM_SOURCE_PARQUET_READ_ORDER_STRATEGY_EARLIEST_TIME_URL_FIRST)
    EARLIEST_TIME_URL_FIRST,
    /**
     * Process splits in their discovery (index) order; not yet supported and currently rejected at
     * source-construction time.
     */
    @SerializedName(STREAM_SOURCE_PARQUET_READ_ORDER_STRATEGY_EARLIEST_INDEX_FIRST)
    EARLIEST_INDEX_FIRST
}
