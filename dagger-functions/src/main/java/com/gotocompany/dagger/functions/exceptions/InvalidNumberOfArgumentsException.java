package com.gotocompany.dagger.functions.exceptions;

/**
 * The class Exception for Invalid number of arguments.
 */
public class InvalidNumberOfArgumentsException extends RuntimeException {
    /**
     * Default message used when the exception is raised without an explicit reason, indicating that the
     * number of arguments passed to the UDF must be divisible by three.
     */
    private static final String DEFAULT_ERROR_MESSAGE = "Invalid number of arguments given to Udf. Requires arguments that is divisible by 3.";

    /**
     * Instantiates a new Invalid number of arguments exception.
     */
    public InvalidNumberOfArgumentsException() {
        super(DEFAULT_ERROR_MESSAGE);
    }

    /**
     * Instantiates a new Invalid number of arguments exception.
     *
     * @param message the message
     */
    public InvalidNumberOfArgumentsException(String message) {
        super(message);
    }
}
