package com.gotocompany.dagger.functions.exceptions;

/**
 * The class Exception for Odd number of arguments on Features Udf.
 */
public class OddNumberOfArgumentsException extends RuntimeException {

    /**
     * Default message used when the exception is raised without an explicit reason, indicating that the
     * UDF received an odd number of arguments when an even count was required.
     */
    private static final String DEFAULT_ERROR_MESSAGE = "Odd number of arguments given to Udf. Requires even.";

    /**
     * Instantiates a new Odd number of arguments exception.
     */
    public OddNumberOfArgumentsException() {
        super(DEFAULT_ERROR_MESSAGE);
    }

    /**
     * Instantiates a new Odd number of arguments exception.
     *
     * @param message the message
     */
    public OddNumberOfArgumentsException(String message) {
        super(message);
    }
}
