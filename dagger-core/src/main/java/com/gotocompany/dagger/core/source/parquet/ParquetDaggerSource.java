package com.gotocompany.dagger.core.source.parquet;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.exception.DaggerConfigurationException;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.StatsDErrorReporter;
import com.gotocompany.dagger.core.source.parquet.reader.ParquetReader;
import com.gotocompany.dagger.core.source.parquet.splitassigner.ChronologyOrderedSplitAssigner;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.common.serde.parquet.deserialization.SimpleGroupDeserializer;
import com.gotocompany.dagger.core.source.DaggerSource;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import com.gotocompany.dagger.core.source.config.models.SourceDetails;
import com.gotocompany.dagger.core.source.config.models.SourceName;
import com.gotocompany.dagger.core.source.config.models.SourceType;
import com.gotocompany.dagger.core.source.parquet.path.HourDatePathParser;
import com.gotocompany.dagger.core.source.parquet.reader.ReaderProvider;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.assigners.FileSplitAssigner;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Supplier;

import static com.gotocompany.dagger.core.source.config.models.SourceName.PARQUET_SOURCE;
import static com.gotocompany.dagger.core.source.config.models.SourceType.BOUNDED;

/**
 * {@link DaggerSource} implementation that reads bounded batches of records from Parquet files via
 * Flink's {@code FileSource}.
 *
 * <p>It is selected when the configured {@code SOURCE_DETAILS} declare a single
 * {@link SourceName#PARQUET_SOURCE} of type {@link SourceType#BOUNDED} and the deserializer is a
 * {@link SimpleGroupDeserializer}. The source discovers the configured Parquet paths, builds a
 * {@link ParquetFileRecordFormat} (which yields a {@code ParquetReader} per file), and orders the
 * resulting splits according to the configured {@link SourceParquetReadOrderStrategy} — currently
 * only chronological ordering via {@link ChronologyOrderedSplitAssigner} is supported. Fatal
 * configuration errors are reported through StatsD before being thrown.
 */
public class ParquetDaggerSource implements DaggerSource<Row> {
    /**
     * Deserializer applied to each Parquet record; must be a {@link SimpleGroupDeserializer}.
     */
    private final DaggerDeserializer<Row> deserializer;
    /**
     * The per-stream configuration supplying the Parquet paths, date range, and read-order strategy.
     */
    private final StreamConfig streamConfig;
    /**
     * The global Dagger job configuration.
     */
    private final Configuration configuration;
    /**
     * Supplier of the StatsD reporter propagated into the file source, readers, and split assigner.
     */
    private final SerializedStatsDReporterSupplier statsDReporterSupplier;
    /**
     * The single source type this implementation supports ({@code BOUNDED}).
     */
    private static final SourceType SUPPORTED_SOURCE_TYPE = BOUNDED;
    /**
     * The single source name this implementation supports ({@code PARQUET_SOURCE}).
     */
    private static final SourceName SUPPORTED_SOURCE_NAME = PARQUET_SOURCE;
    /**
     * Error reporter used to surface fatal configuration errors to StatsD.
     */
    private final StatsDErrorReporter statsDErrorReporter;

    /**
     * Creates a Parquet source from the given configuration, deserializer, and StatsD supplier.
     *
     * @param streamConfig           the per-stream configuration carrying Parquet paths and ordering
     * @param configuration          the global Dagger job configuration
     * @param deserializer           the record deserializer; expected to be a {@link SimpleGroupDeserializer}
     * @param statsDReporterSupplier supplier of the StatsD reporter used for error/metric reporting
     */
    public ParquetDaggerSource(StreamConfig streamConfig, Configuration configuration, DaggerDeserializer<Row> deserializer, SerializedStatsDReporterSupplier statsDReporterSupplier) {
        this.streamConfig = streamConfig;
        this.configuration = configuration;
        this.deserializer = deserializer;
        this.statsDReporterSupplier = statsDReporterSupplier;
        this.statsDErrorReporter = new StatsDErrorReporter(statsDReporterSupplier);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds the bounded Parquet {@code FileSource} and registers it on the environment via
     * {@code fromSource}, using the supplied watermark strategy and the stream's schema table as the
     * source name.
     */
    @Override
    public DataStream<Row> register(StreamExecutionEnvironment executionEnvironment, WatermarkStrategy<Row> watermarkStrategy) {
        return executionEnvironment.fromSource(buildFileSource(), watermarkStrategy, streamConfig.getSchemaTable());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} only when exactly one {@code SOURCE_DETAILS} entry is configured with
     * source name {@link SourceName#PARQUET_SOURCE} and type {@link SourceType#BOUNDED}, and the
     * deserializer is a {@link SimpleGroupDeserializer}.
     */
    @Override
    public boolean canBuild() {
        SourceDetails[] sourceDetailsArray = streamConfig.getSourceDetails();
        if (sourceDetailsArray.length != 1) {
            return false;
        } else {
            SourceName sourceName = sourceDetailsArray[0].getSourceName();
            SourceType sourceType = sourceDetailsArray[0].getSourceType();
            return sourceName.equals(SUPPORTED_SOURCE_NAME) && sourceType.equals(SUPPORTED_SOURCE_TYPE)
                    && deserializer instanceof SimpleGroupDeserializer;
        }
    }

    /**
     * Assembles the Flink {@code FileSource} for the configured Parquet inputs.
     *
     * <p>Wires together the file paths, the {@link ParquetFileRecordFormat}, the source type, and the
     * read-order-derived {@code FileSplitAssigner.Provider} through {@link ParquetFileSource.Builder},
     * then delegates to {@link ParquetFileSource#buildFileSource()}.
     *
     * @return the configured Flink {@code FileSource} of {@code Row}
     */
    FileSource<Row> buildFileSource() {
        ParquetFileSource.Builder parquetFileSourceBuilder = ParquetFileSource.Builder.getInstance();
        ParquetFileRecordFormat parquetFileRecordFormat = buildParquetFileRecordFormat();
        FileSplitAssigner.Provider splitAssignerProvider = buildParquetFileSplitAssignerProvider();
        Path[] filePaths = buildFlinkFilePaths();

        ParquetFileSource parquetFileSource = parquetFileSourceBuilder.setFilePaths(filePaths)
                .setConfiguration(configuration)
                .setFileRecordFormat(parquetFileRecordFormat)
                .setSourceType(SUPPORTED_SOURCE_TYPE)
                .setFileSplitAssigner(splitAssignerProvider)
                .setStatsDReporterSupplier(statsDReporterSupplier)
                .build();
        return parquetFileSource.buildFileSource();
    }

    /**
     * Converts the configured Parquet path strings into Flink {@link Path} instances.
     *
     * @return an array of Flink {@code Path}s, one per configured Parquet file path
     */
    private Path[] buildFlinkFilePaths() {
        String[] parquetFilePaths = streamConfig.getParquetFilePaths();
        return Arrays.stream(parquetFilePaths)
                .map(Path::new)
                .toArray(Path[]::new);
    }

    /**
     * Selects the split-assigner provider matching the configured read-order strategy.
     *
     * <p>For {@link SourceParquetReadOrderStrategy#EARLIEST_TIME_URL_FIRST} it returns a provider that
     * builds a {@link ChronologyOrderedSplitAssigner} configured with the parquet date range, a
     * {@link HourDatePathParser}, and the StatsD supplier. The index-ordered strategy is not yet
     * supported: a {@link DaggerConfigurationException} is reported to StatsD and thrown.
     *
     * @return a {@code FileSplitAssigner.Provider} for the configured ordering strategy
     * @throws DaggerConfigurationException if the configured read-order strategy is unsupported
     */
    private FileSplitAssigner.Provider buildParquetFileSplitAssignerProvider() {
        SourceParquetReadOrderStrategy readOrderStrategy = streamConfig.getParquetFilesReadOrderStrategy();
        switch (readOrderStrategy) {
            case EARLIEST_TIME_URL_FIRST:
                ChronologyOrderedSplitAssigner.ChronologyOrderedSplitAssignerBuilder chronologyOrderedSplitAssignerBuilder =
                        new ChronologyOrderedSplitAssigner.ChronologyOrderedSplitAssignerBuilder()
                                .addTimeRanges(streamConfig.getParquetFileDateRange())
                                .addStatsDReporterSupplier(statsDReporterSupplier)
                                .addPathParser(new HourDatePathParser());
                return chronologyOrderedSplitAssignerBuilder::build;
            case EARLIEST_INDEX_FIRST:
            default:
                DaggerConfigurationException daggerConfigurationException = new DaggerConfigurationException("Error: file split assignment strategy not configured or not supported yet.");
                statsDErrorReporter.reportFatalException(daggerConfigurationException);
                throw daggerConfigurationException;
        }
    }

    /**
     * Builds the {@link ParquetFileRecordFormat} that produces a reader per Parquet file.
     *
     * <p>Wraps the {@link SimpleGroupDeserializer} in a {@link ParquetReader.ParquetReaderProvider}
     * and supplies a serializable {@code Supplier} of the produced {@code TypeInformation} so the
     * record format can advertise its output type to Flink.
     *
     * @return the configured {@code ParquetFileRecordFormat}
     */
    private ParquetFileRecordFormat buildParquetFileRecordFormat() {
        SimpleGroupDeserializer simpleGroupDeserializer = (SimpleGroupDeserializer) deserializer;
        ReaderProvider parquetFileReaderProvider = new ParquetReader.ParquetReaderProvider(simpleGroupDeserializer, statsDReporterSupplier);
        ParquetFileRecordFormat.Builder parquetFileRecordFormatBuilder = ParquetFileRecordFormat.Builder.getInstance();
        Supplier<TypeInformation<Row>> typeInformationProvider = (Supplier<TypeInformation<Row>> & Serializable) simpleGroupDeserializer::getProducedType;
        return parquetFileRecordFormatBuilder
                .setParquetFileReaderProvider(parquetFileReaderProvider)
                .setTypeInformationProvider(typeInformationProvider)
                .setStatsDReporterSupplier(statsDReporterSupplier)
                .build();
    }
}
