package com.gotocompany.dagger.core.source.parquet;

import static com.google.api.client.util.Preconditions.checkArgument;

import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.StatsDErrorReporter;
import com.gotocompany.dagger.core.source.parquet.reader.ReaderProvider;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.reader.FileRecordFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.types.Row;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Flink {@link FileRecordFormat} that reads Parquet files and emits their rows as Flink
 * {@code Row}s.
 *
 * <p>For each split, {@link #createReader} delegates to a {@link ReaderProvider} (typically a
 * {@code ParquetReader.ParquetReaderProvider}) to open a reader over the file. The format is
 * non-splittable — a Parquet file is always read whole by a single reader — and advertises its
 * produced {@code TypeInformation} via a supplied provider. Because Parquet readers are not
 * offset-based, {@link #restoreReader} is unsupported. All error reporting goes through a
 * serializable {@code Supplier} of {@link StatsDErrorReporter} so the format can be safely shipped
 * as part of the Flink job graph.
 */
public class ParquetFileRecordFormat implements FileRecordFormat<Row> {
    /* FileRecordFormat object and all it's fields need to be serializable in order to construct the Flink job graph. Even though
    StatsDErrorReporter is serializable, it contains StatsDErrorReporter, which in turn contains more fields which may not be
    serializable.Hence, in order to mitigate job graph creation failures, we wrap the error reporter inside a serializable lambda.
    This is a common idiom to make un-serializable fields serializable in Java 8: https://stackoverflow.com/a/22808112 */

    /**
     * Factory that opens a reader over a given Parquet file path.
     */
    private final ReaderProvider parquetFileReaderProvider;
    /**
     * Supplies the {@code TypeInformation} describing the {@code Row}s produced by this format.
     */
    private final Supplier<TypeInformation<Row>> typeInformationProvider;
    /**
     * Serializable supplier of the StatsD error reporter used to report fatal errors.
     */
    private final Supplier<StatsDErrorReporter> statsDErrorReporterSupplier;

    /**
     * Creates a record format; use {@link Builder} to construct one.
     *
     * <p>The supplied StatsD reporter supplier is wrapped in a serializable lambda so the resulting
     * error reporter can be created lazily on the task managers.
     *
     * @param parquetFileReaderProvider the provider that opens a reader per Parquet file
     * @param typeInformationProvider   supplier of the produced {@code Row} type information
     * @param statsDReporterSupplier    supplier of the StatsD reporter for error reporting
     */
    private ParquetFileRecordFormat(ReaderProvider parquetFileReaderProvider, Supplier<TypeInformation<Row>> typeInformationProvider, SerializedStatsDReporterSupplier statsDReporterSupplier) {
        this.parquetFileReaderProvider = parquetFileReaderProvider;
        this.typeInformationProvider = typeInformationProvider;
        this.statsDErrorReporterSupplier = (Supplier<StatsDErrorReporter> & Serializable) () -> new StatsDErrorReporter(statsDReporterSupplier);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Opens a fresh Parquet reader for the given file via the {@link ReaderProvider}. The split
     * offset and length are ignored because the format is non-splittable and Parquet files are read
     * in full.
     *
     * @param config      the Flink configuration (unused)
     * @param filePath    the path of the Parquet file to read
     * @param splitOffset the split start offset (ignored)
     * @param splitLength the split length (ignored)
     * @return a reader over the whole Parquet file
     */
    @Override
    public Reader<Row> createReader(Configuration config, Path filePath, long splitOffset, long splitLength) {
        return parquetFileReaderProvider.getReader(filePath.toString());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Unsupported for Parquet: readers have no notion of offsets and therefore cannot be restored
     * to a previous position. Always reports a fatal error and throws.
     *
     * @param config         the Flink configuration (unused)
     * @param filePath       the path of the Parquet file (unused)
     * @param restoredOffset the offset to restore from (unused)
     * @param splitOffset    the split start offset (unused)
     * @param splitLength    the split length (unused)
     * @return never returns normally
     * @throws UnsupportedOperationException always, since Parquet readers cannot be restored by offset
     */
    @Override
    public Reader<Row> restoreReader(Configuration config, Path filePath, long restoredOffset, long splitOffset, long splitLength) {
        UnsupportedOperationException ex = new UnsupportedOperationException("Error: ParquetReader do not have offsets and hence cannot be restored "
                + "via this method.");
        statsDErrorReporterSupplier.get().reportFatalException(ex);
        throw ex;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Always returns {@code false}: a Parquet file is read in its entirety by a single reader and
     * is never broken into sub-splits.
     *
     * @return {@code false}, indicating the format is not splittable
     */
    @Override
    public boolean isSplittable() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the {@code Row} type information supplied at construction time.
     *
     * @return the {@code TypeInformation} of the produced rows
     */
    @Override
    public TypeInformation<Row> getProducedType() {
        return typeInformationProvider.get();
    }

    /**
     * Fluent builder for {@link ParquetFileRecordFormat} that validates required dependencies before
     * constructing the format.
     */
    public static class Builder {
        /**
         * The provider that opens a reader per Parquet file; required.
         */
        private ReaderProvider parquetFileReaderProvider;
        /**
         * Supplier of the produced {@code Row} type information; required.
         */
        private Supplier<TypeInformation<Row>> typeInformationProvider;
        /**
         * Supplier of the StatsD reporter for error reporting; required.
         */
        private SerializedStatsDReporterSupplier statsDReporterSupplier;

        /**
         * Creates a new builder with all dependencies unset.
         *
         * @return a fresh {@link Builder} instance
         */
        public static Builder getInstance() {
            return new Builder();
        }

        /**
         * Initializes an empty builder; all dependencies must be set before {@link #build()}.
         */
        private Builder() {
            this.parquetFileReaderProvider = null;
            this.typeInformationProvider = null;
            this.statsDReporterSupplier = null;
        }

        /**
         * Sets the provider that opens a reader per Parquet file.
         *
         * @param parquetFileReaderProvider the reader provider
         * @return this builder
         */
        public Builder setParquetFileReaderProvider(ReaderProvider parquetFileReaderProvider) {
            this.parquetFileReaderProvider = parquetFileReaderProvider;
            return this;
        }

        /**
         * Sets the supplier of the produced {@code Row} type information.
         *
         * @param typeInformationProvider the type information supplier
         * @return this builder
         */
        public Builder setTypeInformationProvider(Supplier<TypeInformation<Row>> typeInformationProvider) {
            this.typeInformationProvider = typeInformationProvider;
            return this;
        }

        /**
         * Sets the supplier of the StatsD reporter used for error reporting.
         *
         * @param statsDReporterSupplier the StatsD reporter supplier
         * @return this builder
         */
        public Builder setStatsDReporterSupplier(SerializedStatsDReporterSupplier statsDReporterSupplier) {
            this.statsDReporterSupplier = statsDReporterSupplier;
            return this;
        }

        /**
         * Validates that all dependencies are set and constructs the {@link ParquetFileRecordFormat}.
         *
         * <p>If any required dependency is missing, the resulting {@link IllegalArgumentException} is
         * reported to StatsD (when a supplier is available) and rethrown.
         *
         * @return the built {@code ParquetFileRecordFormat}
         * @throws IllegalArgumentException if any required dependency is {@code null}
         */
        public ParquetFileRecordFormat build() {
            try {
                checkArgument(parquetFileReaderProvider != null, "ReaderProvider is required but is set as null");
                checkArgument(typeInformationProvider != null, "TypeInformationProvider is required but is set as null");
                checkArgument(statsDReporterSupplier != null, "SerializedStatsDReporterSupplier is required but is set as null");
                return new ParquetFileRecordFormat(parquetFileReaderProvider, typeInformationProvider, statsDReporterSupplier);
            } catch (IllegalArgumentException ex) {
                if (statsDReporterSupplier != null) {
                    new StatsDErrorReporter(statsDReporterSupplier).reportFatalException(ex);
                }
                throw ex;
            }
        }
    }
}
