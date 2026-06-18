package com.gotocompany.dagger.core.exception;

import java.io.IOException;

/**
 * Checked exception signalling that writing points to InfluxDB failed.
 *
 * <p>The InfluxDB sink batches measurement points and flushes them asynchronously; when the InfluxDB
 * client reports a write failure, the error handlers wrap the underlying cause in this exception
 * (extending {@link IOException}). It is surfaced by the InfluxDB writer - for example during a
 * checkpoint snapshot - and reported as a fatal exception so failed writes are not silently dropped.
 */
public class InfluxWriteException extends IOException {
    /**
     * Creates a new exception wrapping the underlying InfluxDB write failure.
     *
     * @param err the underlying error reported by the InfluxDB client
     */
    public InfluxWriteException(Throwable err) {
        super(err);
    }
}
