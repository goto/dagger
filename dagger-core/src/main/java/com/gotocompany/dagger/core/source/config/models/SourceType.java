package com.gotocompany.dagger.core.source.config.models;

import com.google.gson.annotations.SerializedName;

import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_DETAILS_SOURCE_TYPE_BOUNDED;
import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_DETAILS_SOURCE_TYPE_UNBOUNDED;

/**
 * Declares whether a stream's source is bounded (finite) or unbounded (continuous).
 *
 * <p>Each constant carries the {@code @SerializedName} alias used in the {@code SOURCE_DETAILS} JSON of
 * a {@code StreamConfig}. The boundedness maps onto Flink's batch-versus-streaming execution: a bounded
 * source (such as a Parquet backfill) terminates once exhausted, while an unbounded source (such as
 * Kafka) runs indefinitely.
 */
public enum SourceType {
    /** Finite source that completes once all records are read, e.g. a Parquet backfill; serialized as {@code "BOUNDED"}. */
    @SerializedName(STREAM_SOURCE_DETAILS_SOURCE_TYPE_BOUNDED)
    BOUNDED,
    /** Continuous source that never completes on its own, e.g. Kafka; serialized as {@code "UNBOUNDED"}. */
    @SerializedName(STREAM_SOURCE_DETAILS_SOURCE_TYPE_UNBOUNDED)
    UNBOUNDED
}
