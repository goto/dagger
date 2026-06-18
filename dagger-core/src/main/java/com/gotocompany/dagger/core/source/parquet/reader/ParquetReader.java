package com.gotocompany.dagger.core.source.parquet.reader;

import com.gotocompany.dagger.common.exceptions.serde.DaggerDeserializationException;
import com.gotocompany.dagger.core.exception.ParquetFileSourceReaderInitializationException;
import com.gotocompany.dagger.core.metrics.aspects.ParquetReaderAspects;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.StatsDErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.statsd.manager.DaggerCounterManager;
import com.gotocompany.dagger.core.metrics.reporters.statsd.manager.DaggerHistogramManager;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.ComponentTags;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.StatsDTag;
import com.gotocompany.dagger.common.serde.parquet.deserialization.SimpleGroupDeserializer;
import org.apache.flink.connector.file.src.reader.FileRecordFormat;
import org.apache.flink.connector.file.src.util.CheckpointedPosition;
import org.apache.flink.types.Row;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;

/**
 * Flink {@code FileRecordFormat.Reader} that streams the rows of a single Parquet file as Flink
 * {@code Row}s.
 *
 * <p>The reader iterates the Parquet file one row group at a time, decodes each record into a
 * {@link SimpleGroup}, and converts it to a {@code Row} via the {@link SimpleGroupDeserializer}.
 * Read throughput and deserialization latency are recorded through Dagger's StatsD histogram and
 * counter managers, and any deserialization failure is reported as a fatal error. Because Parquet
 * files have no byte offsets suitable for arbitrary resumption, the checkpointed position only
 * tracks the number of emitted records. Instances are created by the nested
 * {@link ParquetReaderProvider}.
 */
public class ParquetReader implements FileRecordFormat.Reader<Row> {
    /**
     * The Hadoop path of the Parquet file being read.
     */
    private final Path hadoopFilePath;
    /**
     * Deserializer converting each Parquet {@link SimpleGroup} into a Flink {@code Row}.
     */
    private final SimpleGroupDeserializer simpleGroupDeserializer;
    /**
     * Index of the next record to read within the current row group.
     */
    private long currentRecordIndex;
    /**
     * The underlying Parquet file reader that supplies row groups.
     */
    private final ParquetFileReader parquetFileReader;
    /**
     * Number of records in the current row group.
     */
    private long rowCount;
    /**
     * Whether the per-row-group record reader has been initialized.
     */
    private boolean isRecordReaderInitialized;
    /**
     * Reader over the records of the current row group.
     */
    private RecordReader<Group> recordReader;
    /**
     * The Parquet message schema of the file.
     */
    private final MessageType schema;
    /**
     * Running count of records emitted so far; used as the checkpointed position.
     */
    private long totalEmittedRowCount;
    /**
     * Counter manager recording reader lifecycle and row-emission metrics.
     */
    private DaggerCounterManager daggerCounterManager;
    /**
     * Histogram manager recording per-row read and deserialization timings.
     */
    private DaggerHistogramManager daggerHistogramManager;
    /**
     * Reporter used to surface fatal deserialization errors to StatsD.
     */
    private final StatsDErrorReporter statsDErrorReporter;
    /**
     * Logger for reader lifecycle events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParquetReader.class.getName());

    /**
     * Creates a reader over an already-opened Parquet file; use {@link ParquetReaderProvider}.
     *
     * <p>Reads the file schema from the reader's metadata, registers the StatsD measurement
     * managers, and increments the reader-created counter.
     *
     * @param hadoopFilePath          the Hadoop path of the Parquet file
     * @param simpleGroupDeserializer the deserializer converting groups to {@code Row}s
     * @param parquetFileReader       the opened Parquet file reader
     * @param statsDReporterSupplier  supplier of the StatsD reporter for metrics and errors
     * @throws IOException if reading the file metadata fails
     */
    private ParquetReader(Path hadoopFilePath, SimpleGroupDeserializer simpleGroupDeserializer, ParquetFileReader
            parquetFileReader, SerializedStatsDReporterSupplier statsDReporterSupplier) throws IOException {
        this.hadoopFilePath = hadoopFilePath;
        this.simpleGroupDeserializer = simpleGroupDeserializer;
        this.parquetFileReader = parquetFileReader;
        this.schema = this.parquetFileReader.getFileMetaData().getSchema();
        this.isRecordReaderInitialized = false;
        this.totalEmittedRowCount = 0L;
        this.registerTagsWithMeasurementManagers(statsDReporterSupplier);
        this.statsDErrorReporter = new StatsDErrorReporter(statsDReporterSupplier);
        daggerCounterManager.increment(ParquetReaderAspects.READER_CREATED);
    }

    /**
     * Initializes and registers the counter and histogram managers with the Parquet reader tags.
     *
     * @param statsDReporterSupplier supplier of the StatsD reporter the managers report through
     */
    private void registerTagsWithMeasurementManagers(SerializedStatsDReporterSupplier statsDReporterSupplier) {
        StatsDTag[] parquetReaderTags = ComponentTags.getParquetReaderTags();
        this.daggerCounterManager = new DaggerCounterManager(statsDReporterSupplier);
        this.daggerCounterManager.register(parquetReaderTags);
        this.daggerHistogramManager = new DaggerHistogramManager(statsDReporterSupplier);
        this.daggerHistogramManager.register(parquetReaderTags);
    }

    /**
     * Checks whether a row group page is {@code null}, indicating the end of the file.
     *
     * @param page the next row group page, or {@code null} if none remain
     * @return {@code true} (logging an end-of-data message) when {@code page} is {@code null};
     *         {@code false} otherwise
     */
    private boolean checkIfNullPage(PageReadStore page) {
        if (page == null) {
            String logMessage = String.format("No more data found in Parquet file %s", hadoopFilePath.getName());
            LOGGER.info(logMessage);
            return true;
        }
        return false;
    }

    /**
     * Advances the reader onto a new row group, resetting the per-group cursor.
     *
     * <p>Updates the row count, resets the current record index to zero, and builds a fresh record
     * reader for the supplied row group using the file schema.
     *
     * @param pages the row group to start reading from
     */
    private void changeReaderPosition(PageReadStore pages) {
        rowCount = pages.getRowCount();
        currentRecordIndex = 0;
        MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
        recordReader = columnIO.getRecordReader(pages, new GroupRecordConverter(schema));
    }

    /**
     * Reads the first row group and prepares the reader for record consumption.
     *
     * @throws IOException if reading the first row group fails
     */
    private void initializeRecordReader() throws IOException {
        PageReadStore nextPage = parquetFileReader.readNextRowGroup();
        changeReaderPosition(nextPage);
        this.isRecordReaderInitialized = true;
        String logMessage = String.format("Successfully created the ParquetFileReader and RecordReader for file %s", hadoopFilePath.getName());
        LOGGER.info(logMessage);
    }

    /**
     * Reads and deserializes the next record, advancing to the next row group when needed.
     *
     * <p>When the current row group is exhausted the next one is loaded; if none remain,
     * {@code null} is returned to signal end of file. Read and deserialization durations are
     * recorded to the histogram manager.
     *
     * @return the next record as a {@code Row}, or {@code null} when the file is fully read
     * @throws IOException if reading the next row group fails
     */
    private Row readRecords() throws IOException {
        long startReadTime = Instant.now().toEpochMilli();

        if (currentRecordIndex >= rowCount) {
            PageReadStore nextPage = parquetFileReader.readNextRowGroup();
            if (checkIfNullPage(nextPage)) {
                return null;
            }
            changeReaderPosition(nextPage);
        }
        SimpleGroup simpleGroup = (SimpleGroup) recordReader.read();
        long endReadTime = Instant.now().toEpochMilli();

        currentRecordIndex++;
        long startDeserializationTime = Instant.now().toEpochMilli();

        Row row = deserialize(simpleGroup);

        long endDeserializationTime = Instant.now().toEpochMilli();
        totalEmittedRowCount++;

        daggerHistogramManager.recordValue(ParquetReaderAspects.READER_ROW_READ_TIME, endReadTime - startReadTime);
        daggerHistogramManager.recordValue(ParquetReaderAspects.READER_ROW_DESERIALIZATION_TIME, endDeserializationTime - startDeserializationTime);
        return row;
    }

    /**
     * Deserializes a single Parquet group into a Flink {@code Row}.
     *
     * @param simpleGroup the Parquet record to convert
     * @return the deserialized {@code Row}
     * @throws DaggerDeserializationException if deserialization fails; the error is reported to
     *                                        StatsD before being rethrown
     */
    private Row deserialize(SimpleGroup simpleGroup) {
        try {
            return simpleGroupDeserializer.deserialize(simpleGroup);
        } catch (DaggerDeserializationException exception) {
            statsDErrorReporter.reportFatalException(exception);
            throw exception;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily initializes the record reader on the first call, reads the next record, and
     * increments the rows-emitted counter.
     *
     * @return the next {@code Row}, or {@code null} when the file is fully read
     * @throws IOException if reading fails
     */
    @Nullable
    @Override
    public Row read() throws IOException {
        if (!isRecordReaderInitialized) {
            initializeRecordReader();
        }
        Row row = readRecords();
        daggerCounterManager.increment(ParquetReaderAspects.READER_ROWS_EMITTED);
        return row;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the underlying Parquet file reader, de-references the record reader, and increments
     * the reader-closed counter.
     *
     * @throws IOException if closing the underlying file reader fails
     */
    @Override
    public void close() throws IOException {
        parquetFileReader.close();
        closeRecordReader();
        String logMessage = String.format("Closed the ParquetFileReader and de-referenced the RecordReader for file %s", hadoopFilePath.getName());
        LOGGER.info(logMessage);
        daggerCounterManager.increment(ParquetReaderAspects.READER_CLOSED);
    }

    /**
     * Marks the record reader uninitialized and releases the reference to it.
     */
    private void closeRecordReader() {
        if (isRecordReaderInitialized) {
            this.isRecordReaderInitialized = false;
        }
        recordReader = null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Parquet files expose no usable byte offset, so the position reports
     * {@code CheckpointedPosition.NO_OFFSET} together with the count of records emitted so far.
     *
     * @return the checkpointed position with no offset and the emitted-record count
     */
    @Override
    public CheckpointedPosition getCheckpointedPosition() {
        return new CheckpointedPosition(CheckpointedPosition.NO_OFFSET, totalEmittedRowCount);
    }

    /**
     * {@link ReaderProvider} that opens a {@link ParquetReader} for a given file path.
     *
     * <p>Serializable so it can be embedded in the {@link ParquetFileRecordFormat} and shipped with
     * the Flink job graph; the actual {@code ParquetFileReader} is opened lazily on the task manager
     * when {@link #getReader(String)} is invoked.
     */
    public static class ParquetReaderProvider implements ReaderProvider {
        /**
         * Deserializer handed to every {@link ParquetReader} this provider creates.
         */
        private final SimpleGroupDeserializer simpleGroupDeserializer;
        /**
         * Supplier of the StatsD reporter handed to every reader for metrics and error reporting.
         */
        private final SerializedStatsDReporterSupplier statsDReporterSupplier;

        /**
         * Creates a provider that builds readers from the given deserializer and StatsD supplier.
         *
         * @param simpleGroupDeserializer the deserializer converting Parquet groups to {@code Row}s
         * @param statsDReporterSupplier  supplier of the StatsD reporter for metrics and errors
         */
        public ParquetReaderProvider(SimpleGroupDeserializer simpleGroupDeserializer, SerializedStatsDReporterSupplier statsDReporterSupplier) {
            this.simpleGroupDeserializer = simpleGroupDeserializer;
            this.statsDReporterSupplier = statsDReporterSupplier;
        }

        /**
         * {@inheritDoc}
         *
         * <p>Opens the Parquet file at the given path with a Hadoop configuration and wraps it in a
         * {@link ParquetReader}. Any failure is wrapped in a
         * {@link ParquetFileSourceReaderInitializationException}, reported to StatsD, and rethrown.
         *
         * @param filePath the path of the Parquet file to open
         * @return a new {@link ParquetReader} positioned at the start of the file
         * @throws ParquetFileSourceReaderInitializationException if the file cannot be opened
         */
        @Override
        public ParquetReader getReader(String filePath) {
            try {
                Configuration conf = new Configuration();
                Path hadoopFilePath = new Path(filePath);
                ParquetFileReader parquetFileReader = ParquetFileReader.open(HadoopInputFile.fromPath(hadoopFilePath, conf));
                return new ParquetReader(hadoopFilePath, simpleGroupDeserializer, parquetFileReader, statsDReporterSupplier);
            } catch (IOException | RuntimeException ex) {
                ParquetFileSourceReaderInitializationException exception = new ParquetFileSourceReaderInitializationException(ex);
                new StatsDErrorReporter(statsDReporterSupplier).reportFatalException(exception);
                throw exception;
            }
        }
    }
}
