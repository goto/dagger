package com.gotocompany.dagger.core.exception;

import java.io.IOException;

/**
 * Checked exception signalling that writing records to BigQuery failed.
 *
 * <p>The BigQuery sink flushes batches of rows and inspects the resulting {@code SinkResponse}; when
 * the response contains non-retryable errors this exception (extending {@link IOException}) is thrown
 * from {@code BigQuerySinkWriter} so the failed flush propagates as a sink error rather than being
 * silently dropped.
 */
public class BigQueryWriterException extends IOException {

    /**
     * Creates a new exception with a message and the underlying cause.
     *
     * @param message human-readable detail describing the BigQuery write failure
     * @param cause   the underlying error that caused the write to fail
     */
    public BigQueryWriterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with a message describing the BigQuery write failure.
     *
     * @param message human-readable detail describing the BigQuery write failure
     */
    public BigQueryWriterException(String message) {
        super(message);
    }
}
