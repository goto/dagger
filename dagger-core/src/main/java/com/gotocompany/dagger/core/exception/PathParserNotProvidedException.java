package com.gotocompany.dagger.core.exception;

/**
 * Unchecked exception signalling that a Parquet file-path parser was required but none was supplied.
 *
 * <p>When Dagger reads bounded/Parquet sources it derives the chronological ordering of file splits
 * from the timestamp encoded in each file path, which requires a configured path parser. If the
 * parser is missing this exception is raised (for example from
 * {@code ChronologyOrderedSplitAssigner} with the message {@code "Path parser is null"}) and reported
 * as a fatal exception, so the job cannot proceed with an undefined split ordering.
 */
public class PathParserNotProvidedException extends RuntimeException {
    /**
     * Creates a new exception indicating that a required path parser was not provided.
     *
     * @param message human-readable detail describing the missing path parser
     */
    public PathParserNotProvidedException(String message) {
        super(message);
    }
}
