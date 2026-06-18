package com.gotocompany.dagger.core.source.config;

import com.google.common.base.Preconditions;
import com.gotocompany.dagger.core.source.config.models.SourceDetails;
import com.gotocompany.dagger.core.source.config.models.SourceName;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_DETAILS_KEY;
import static com.gotocompany.dagger.core.utils.Constants.STREAM_SOURCE_PARQUET_FILE_PATHS_KEY;

/**
 * Validation helpers applied to {@link StreamConfig} instances right after they are parsed.
 *
 * <p>{@link StreamConfig#parse} chains these static methods so that structurally invalid stream
 * definitions abort job startup with a descriptive message instead of failing later. Each method
 * returns the same config it received, which lets them be composed in a {@link java.util.stream.Stream}
 * pipeline. The checks are implemented with Guava {@link com.google.common.base.Preconditions}, so a
 * failed assertion throws {@link IllegalArgumentException}.
 */
public class StreamConfigValidator {
    /**
     * Verifies that a stream declares at least one fully-specified source.
     *
     * <p>The {@code sourceDetails} array must be non-empty, contain no {@code null} elements, and each
     * element must have both a non-null {@link com.gotocompany.dagger.core.source.config.models.SourceName}
     * and {@link com.gotocompany.dagger.core.source.config.models.SourceType} (an unknown or
     * whitespace-padded value deserializes to {@code null} and is rejected here).
     *
     * @param streamConfig the parsed stream configuration to validate
     * @return the same {@code streamConfig}, unchanged, when all source details are valid
     * @throws IllegalArgumentException if the source details are empty, contain a {@code null} entry,
     *                                  or have a missing source name or source type
     */
    public static StreamConfig validateSourceDetails(StreamConfig streamConfig) {
        SourceDetails[] sourceDetailsArray = streamConfig.getSourceDetails();
        Preconditions.checkArgument(sourceDetailsArray.length != 0, "%s config is set to "
                        + "an empty array. Please check the documentation and specify in a valid format.",
                STREAM_SOURCE_DETAILS_KEY);
        for (SourceDetails sourceDetails : sourceDetailsArray) {
            Preconditions.checkArgument(sourceDetails != null, "One or more elements inside %s "
                    + "is either null or invalid.", STREAM_SOURCE_DETAILS_KEY);
            Preconditions.checkArgument(sourceDetails.getSourceName() != null, "One or more "
                    + "elements inside %s has null or invalid SourceName. Check if it is a valid SourceName and ensure "
                    + "no trailing/leading whitespaces are present", STREAM_SOURCE_DETAILS_KEY);
            Preconditions.checkArgument(sourceDetails.getSourceType() != null, "One or more "
                    + "elements inside %s has null or invalid SourceType. Check if it is a valid SourceType and ensure "
                    + "no trailing/leading whitespaces are present", STREAM_SOURCE_DETAILS_KEY);
        }
        return streamConfig;
    }

    /**
     * Runs Parquet-specific validation when the stream uses a Parquet source.
     *
     * <p>If any configured source is {@link com.gotocompany.dagger.core.source.config.models.SourceName#PARQUET_SOURCE},
     * the config is forwarded to {@link #validateParquetFilePaths(StreamConfig)} to ensure the file
     * paths are usable; otherwise it is returned untouched.
     *
     * @param streamConfig the parsed stream configuration to inspect
     * @return the same {@code streamConfig}, after Parquet path validation when applicable
     * @throws IllegalArgumentException if a Parquet source is present but its file paths are invalid
     */
    public static StreamConfig validateParquetDataSourceStreamConfigs(StreamConfig streamConfig) {
        SourceDetails[] sourceDetailsArray = streamConfig.getSourceDetails();
        for (SourceDetails sourceDetails : sourceDetailsArray) {
            if (sourceDetails.getSourceName().equals(SourceName.PARQUET_SOURCE)) {
                return Stream.of(streamConfig)
                        .map(StreamConfigValidator::validateParquetFilePaths)
                        .findFirst()
                        .get();
            }
        }
        return streamConfig;
    }

    /**
     * Ensures the Parquet file paths required by a Parquet source are present and non-null.
     *
     * <p>The {@code parquetFilePaths} array must not be {@code null}, and no individual entry may be
     * the literal string {@code "null"} (which is how an absent path round-trips through the JSON
     * configuration).
     *
     * @param streamConfig the parsed stream configuration whose Parquet paths are validated
     * @return the same {@code streamConfig}, unchanged, when the file paths are valid
     * @throws IllegalArgumentException if the paths array is {@code null} or any entry is {@code "null"}
     */
    private static StreamConfig validateParquetFilePaths(StreamConfig streamConfig) {
        String[] parquetFilePaths = streamConfig.getParquetFilePaths();
        Preconditions.checkArgument(parquetFilePaths != null, "%s is required for configuring a "
                + "Parquet Data Source Stream, but is set to null.", STREAM_SOURCE_PARQUET_FILE_PATHS_KEY);
        Arrays.stream(parquetFilePaths)
                .forEach(filePath -> Preconditions.checkArgument(!filePath.equals("null"),
                        "One or more file path inside %s is null.", STREAM_SOURCE_PARQUET_FILE_PATHS_KEY));
        return streamConfig;
    }
}
