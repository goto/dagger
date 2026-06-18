package com.gotocompany.dagger.core.source.config.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.io.Serializable;

import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_DETAILS_SOURCE_NAME_KEY;
import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_DETAILS_SOURCE_TYPE_KEY;

/**
 * Immutable pairing of a {@link SourceName} and {@link SourceType} describing one backing source.
 *
 * <p>A {@code StreamConfig} carries an array of these, defining the ordered chain of sources a stream
 * is read from (for example a {@link SourceType#BOUNDED} Parquet backfill followed by an
 * {@link SourceType#UNBOUNDED} Kafka feed). Both properties are populated by Gson from the
 * {@code SOURCE_DETAILS} configuration and are checked by {@code StreamConfigValidator}. The type is
 * {@link Serializable} so it can be shipped with Flink operator state.
 */
public class SourceDetails implements Serializable {
    /** The source connector to use, deserialized from the {@code SOURCE_NAME} configuration key. */
    @SerializedName(STREAM_SOURCE_DETAILS_SOURCE_NAME_KEY)
    @Getter
    private SourceName sourceName;

    /** Whether the source is bounded or unbounded, deserialized from the {@code SOURCE_TYPE} configuration key. */
    @SerializedName(STREAM_SOURCE_DETAILS_SOURCE_TYPE_KEY)
    @Getter
    private SourceType sourceType;

    /**
     * Creates a source descriptor pairing a connector name with its boundedness.
     *
     * @param sourceName the source connector to read from
     * @param sourceType whether that source is bounded or unbounded
     */
    public SourceDetails(SourceName sourceName, SourceType sourceType) {
        this.sourceName = sourceName;
        this.sourceType = sourceType;
    }
}
