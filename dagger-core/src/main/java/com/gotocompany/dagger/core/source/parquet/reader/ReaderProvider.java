package com.gotocompany.dagger.core.source.parquet.reader;

import org.apache.flink.connector.file.src.reader.FileRecordFormat;
import org.apache.flink.types.Row;

import java.io.Serializable;

/**
 * Serializable factory that creates a Flink {@code FileRecordFormat.Reader} of {@code Row} for a
 * given file path.
 *
 * <p>Implementations (such as {@code ParquetReader.ParquetReaderProvider}) encapsulate how a reader
 * is opened for a file. Being a {@link Serializable} {@link FunctionalInterface}, it can be passed
 * lambda-style into the record format and shipped as part of the Flink job graph.
 */
@FunctionalInterface
public interface ReaderProvider extends Serializable {
    /**
     * Opens a reader over the file at the given path.
     *
     * @param filePath the path of the file to read
     * @return a {@code FileRecordFormat.Reader} that yields {@code Row}s from the file
     */
    FileRecordFormat.Reader<Row> getReader(String filePath);
}
