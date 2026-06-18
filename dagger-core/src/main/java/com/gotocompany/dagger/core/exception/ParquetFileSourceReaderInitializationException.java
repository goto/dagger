package com.gotocompany.dagger.core.exception;

/***
 * This exception is thrown when the reader for Parquet FileSource could not be initialized.
 */
public class ParquetFileSourceReaderInitializationException extends RuntimeException {
    /**
     * Instantiates a new Parquet file source reader initialization exception wrapping the cause.
     *
     * @param cause the underlying error that prevented the Parquet {@code FileSource} reader from
     *              being initialized
     */
    public ParquetFileSourceReaderInitializationException(Throwable cause) {
        super(cause);
    }
}
