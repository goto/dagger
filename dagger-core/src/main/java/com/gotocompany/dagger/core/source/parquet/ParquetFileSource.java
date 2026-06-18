package com.gotocompany.dagger.core.source.parquet;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.StatsDErrorReporter;
import com.gotocompany.dagger.core.source.config.models.SourceType;
import lombok.Getter;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.assigners.FileSplitAssigner;
import org.apache.flink.connector.file.src.assigners.LocalityAwareSplitAssigner;
import org.apache.flink.connector.file.src.reader.FileRecordFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.types.Row;

import java.io.Serializable;

import static com.google.api.client.util.Preconditions.checkArgument;
import static com.gotocompany.dagger.core.source.config.models.SourceType.BOUNDED;

/**
 * Immutable, serializable description of everything required to build a Flink {@code FileSource}
 * over Parquet files, together with a fluent {@link Builder}.
 *
 * <p>Instances are produced by {@link ParquetDaggerSource} and hold the file paths, the
 * {@link FileRecordFormat} that turns each file into {@code Row}s, the {@link FileSplitAssigner}
 * provider that decides the processing order of splits, the source {@link SourceType}, and the
 * Dagger configuration. It is {@link Serializable} so it can participate in the Flink job graph.
 */
public class ParquetFileSource implements Serializable {
    /**
     * Whether the source is bounded or unbounded; only {@link SourceType#BOUNDED} is supported.
     */
    @Getter
    private final SourceType sourceType;
    /**
     * The Parquet file paths to read from.
     */
    @Getter
    private final Path[] filePaths;
    /**
     * The Dagger job configuration associated with this source.
     */
    @Getter
    private final Configuration configuration;
    /**
     * The record format that decodes each Parquet file into Flink {@code Row}s.
     */
    @Getter
    private final FileRecordFormat<Row> fileRecordFormat;
    /**
     * Provider of the split assigner that controls the order in which file splits are processed.
     */
    @Getter
    private final FileSplitAssigner.Provider fileSplitAssigner;

    /**
     * Creates an immutable Parquet file source description; use {@link Builder} to construct one.
     *
     * @param sourceType        the boundedness of the source (expected to be {@code BOUNDED})
     * @param configuration     the Dagger job configuration
     * @param fileRecordFormat  the record format decoding Parquet files into {@code Row}s
     * @param filePaths         the Parquet file paths to read
     * @param fileSplitAssigner the provider of the file split assigner controlling read order
     */
    private ParquetFileSource(SourceType sourceType,
                              Configuration configuration,
                              FileRecordFormat<Row> fileRecordFormat,
                              Path[] filePaths,
                              FileSplitAssigner.Provider fileSplitAssigner) {
        this.sourceType = sourceType;
        this.configuration = configuration;
        this.filePaths = filePaths;
        this.fileRecordFormat = fileRecordFormat;
        this.fileSplitAssigner = fileSplitAssigner;
    }

    /**
     * Builds the Flink {@link FileSource} from this description.
     *
     * <p>Uses {@code FileSource.forRecordFileFormat} with the configured record format and file
     * paths, applying the configured split assigner.
     *
     * @return the constructed Flink {@code FileSource} of {@code Row}
     */
    public FileSource<Row> buildFileSource() {
        return FileSource.forRecordFileFormat(fileRecordFormat, filePaths)
                .setSplitAssigner(fileSplitAssigner)
                .build();
    }

    /**
     * Fluent builder for {@link ParquetFileSource} that applies sensible defaults and validates the
     * configuration before constructing the immutable source.
     */
    public static class Builder {
        /**
         * The source boundedness; defaults to {@link SourceType#BOUNDED}.
         */
        private SourceType sourceType;
        /**
         * The Parquet file paths to read; defaults to an empty array.
         */
        private Path[] filePaths;
        /**
         * The record format decoding Parquet files into {@code Row}s; required.
         */
        private FileRecordFormat<Row> fileRecordFormat;
        /**
         * The Dagger job configuration.
         */
        private Configuration configuration;
        /**
         * Provider of the split assigner; defaults to Flink's {@code LocalityAwareSplitAssigner}.
         */
        private FileSplitAssigner.Provider fileSplitAssigner;
        /**
         * Supplier of the StatsD reporter; required for error reporting during validation.
         */
        private SerializedStatsDReporterSupplier statsDReporterSupplier;

        /**
         * Creates a new builder pre-populated with default values.
         *
         * @return a fresh {@link Builder} instance
         */
        public static Builder getInstance() {
            return new Builder();
        }

        /**
         * Initializes the builder defaults: a {@code BOUNDED} source with no paths, no record
         * format, and Flink's locality-aware split assigner.
         */
        private Builder() {
            this.sourceType = SourceType.BOUNDED;
            this.configuration = null;
            this.fileRecordFormat = null;
            this.filePaths = new Path[0];
            this.fileSplitAssigner = LocalityAwareSplitAssigner::new;
        }

        /**
         * Sets the source boundedness.
         *
         * @param sourceType the source type; only {@link SourceType#BOUNDED} is supported
         * @return this builder
         */
        public Builder setSourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        /**
         * Sets the record format that decodes Parquet files into {@code Row}s.
         *
         * @param fileRecordFormat the record format to use
         * @return this builder
         */
        public Builder setFileRecordFormat(FileRecordFormat<Row> fileRecordFormat) {
            this.fileRecordFormat = fileRecordFormat;
            return this;
        }

        /**
         * Sets the provider of the split assigner controlling the order splits are processed in.
         *
         * @param fileSplitAssigner the split assigner provider
         * @return this builder
         */
        public Builder setFileSplitAssigner(FileSplitAssigner.Provider fileSplitAssigner) {
            this.fileSplitAssigner = fileSplitAssigner;
            return this;
        }

        /**
         * Sets the Parquet file paths to read from.
         *
         * @param filePaths the file paths
         * @return this builder
         */
        public Builder setFilePaths(Path[] filePaths) {
            this.filePaths = filePaths;
            return this;
        }

        /**
         * Sets the Dagger job configuration.
         *
         * @param configuration the configuration
         * @return this builder
         */
        public Builder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        /**
         * Sets the supplier of the StatsD reporter used for error reporting during validation.
         *
         * @param statsDReporterSupplier the StatsD reporter supplier
         * @return this builder
         */
        public Builder setStatsDReporterSupplier(SerializedStatsDReporterSupplier statsDReporterSupplier) {
            this.statsDReporterSupplier = statsDReporterSupplier;
            return this;
        }

        /* other validations if required before creating the file source can be put here */
        /* for example, checking that all the file paths conform to just one partitioning strategy */
        /**
         * Validates that all required builder fields are set and the source type is supported.
         *
         * <p>Ensures the StatsD supplier, record format, and at least one file path are present and
         * that the source type is {@link SourceType#BOUNDED} (unbounded Parquet sources are not yet
         * supported). On failure the {@link IllegalArgumentException} is reported to StatsD (when a
         * supplier is available) and rethrown.
         *
         * @throws IllegalArgumentException if a required field is missing or the source type is not
         *                                  {@code BOUNDED}
         */
        private void sanityCheck() {
            try {
                checkArgument(statsDReporterSupplier != null, "SerializedStatsDReporterSupplier is required but is set as null");
                checkArgument(fileRecordFormat != null, "FileRecordFormat is required but is set as null");
                checkArgument(filePaths.length != 0, "At least one file path is required but none are provided");
                checkArgument(sourceType == BOUNDED, "Running Parquet FileSource in UNBOUNDED mode is not supported yet");
            } catch (IllegalArgumentException exception) {
                if (statsDReporterSupplier != null) {
                    new StatsDErrorReporter(statsDReporterSupplier).reportFatalException(exception);
                }
                throw exception;
            }
        }

        /**
         * Validates the builder state and constructs the immutable {@link ParquetFileSource}.
         *
         * @return the built {@code ParquetFileSource}
         * @throws IllegalArgumentException if {@link #sanityCheck()} fails
         */
        public ParquetFileSource build() {
            sanityCheck();
            return new ParquetFileSource(sourceType,
                    configuration,
                    fileRecordFormat,
                    filePaths,
                    fileSplitAssigner);
        }
    }
}
