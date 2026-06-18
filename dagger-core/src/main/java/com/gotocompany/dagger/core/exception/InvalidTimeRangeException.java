package com.gotocompany.dagger.core.exception;

/**
 * Unchecked exception signalling that a configured time range is invalid.
 *
 * <p>Dagger's bounded (Parquet) sources accept time ranges that bound which files are processed.
 * This exception is raised while parsing that configuration (for example by
 * {@code FileDateRangeAdaptor}) when a range is malformed - such as a start instant that is after the
 * end instant, a value that is not a comma-separated pair of ISO-8601 timestamps, or a timestamp that
 * cannot be parsed with the supported date formats.
 */
public class InvalidTimeRangeException extends RuntimeException {
    /**
     * Creates a new exception describing why the time range is invalid.
     *
     * @param message human-readable detail describing the malformed time range
     */
    public InvalidTimeRangeException(String message) {
        super(message);
    }
}
