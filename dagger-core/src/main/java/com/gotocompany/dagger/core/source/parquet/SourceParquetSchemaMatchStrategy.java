package com.gotocompany.dagger.core.source.parquet;

import com.google.gson.annotations.SerializedName;

import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_PARQUET_BACKWARD_COMPATIBLE_SCHEMA_MATCH_STRATEGY;
import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_PARQUET_SAME_SCHEMA_MATCH_STRATEGY;

/**
 * Strategy controlling how the schema of the Parquet files being read is matched against the
 * expected (configured) message schema.
 *
 * <p>Configured via the parquet schema-match stream property; the {@link SerializedName} annotations
 * bind each constant to its configuration string during Gson deserialization.
 */
public enum SourceParquetSchemaMatchStrategy {
    /**
     * Require the file schema to be identical to the expected schema, failing on any mismatch.
     */
    @SerializedName(STREAM_SOURCE_PARQUET_SAME_SCHEMA_MATCH_STRATEGY)
    SAME_SCHEMA_WITH_FAIL_ON_MISMATCH,
    /**
     * Allow backward-compatible schema differences (for example added or removed fields), failing
     * only when a common field's data type does not match.
     */
    @SerializedName(STREAM_SOURCE_PARQUET_BACKWARD_COMPATIBLE_SCHEMA_MATCH_STRATEGY)
    BACKWARD_COMPATIBLE_SCHEMA_WITH_FAIL_ON_TYPE_MISMATCH
}
