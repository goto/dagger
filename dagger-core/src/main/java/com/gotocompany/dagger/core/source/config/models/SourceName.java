package com.gotocompany.dagger.core.source.config.models;

import com.google.gson.annotations.SerializedName;

import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_DETAILS_SOURCE_NAME_KAFKA_CONSUMER;
import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_DETAILS_SOURCE_NAME_KAFKA;
import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_DETAILS_SOURCE_NAME_PARQUET;

/**
 * Names the kind of backing source a Dagger stream reads from, as declared in the stream config.
 *
 * <p>Each constant carries the {@code @SerializedName} alias used in the {@code SOURCE_DETAILS} JSON of
 * a {@code StreamConfig}, so a job selects a source by its configured string. The chosen name
 * determines which Dagger source implementation is instantiated for the stream.
 */
public enum SourceName {
    /** Flink FLIP-27 {@code KafkaSource}-based reader; serialized as {@code "KAFKA_SOURCE"}. */
    @SerializedName(STREAM_SOURCE_DETAILS_SOURCE_NAME_KAFKA)
    KAFKA_SOURCE,
    /** Bounded Parquet file reader used for backfills from object storage; serialized as {@code "PARQUET_SOURCE"}. */
    @SerializedName(STREAM_SOURCE_DETAILS_SOURCE_NAME_PARQUET)
    PARQUET_SOURCE,
    /** Legacy {@code FlinkKafkaConsumer}-based Kafka reader retained for compatibility; serialized as {@code "KAFKA_CONSUMER"}. */
    @SerializedName(STREAM_SOURCE_DETAILS_SOURCE_NAME_KAFKA_CONSUMER)
    KAFKA_CONSUMER
}
