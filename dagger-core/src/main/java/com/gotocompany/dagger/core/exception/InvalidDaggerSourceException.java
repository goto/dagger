package com.gotocompany.dagger.core.exception;

/**
 * Unchecked exception signalling that no Dagger source could be created for the configured source
 * details.
 *
 * <p>{@code DaggerSourceFactory} inspects the {@code SOURCE_DETAILS} stream configuration and selects
 * a matching source implementation (Kafka, Parquet, and so on). When none of the known sources can
 * satisfy the configuration this exception is thrown and reported as a fatal exception, since the job
 * has no valid input to read from.
 */
public class InvalidDaggerSourceException extends RuntimeException {
    /**
     * Creates a new exception describing why a Dagger source could not be created.
     *
     * @param message human-readable detail, typically echoing the unsupported source configuration
     */
    public InvalidDaggerSourceException(String message) {
        super(message);
    }
}
