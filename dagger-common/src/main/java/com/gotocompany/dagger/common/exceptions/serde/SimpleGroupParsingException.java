package com.gotocompany.dagger.common.exceptions.serde;

/**
 * This runtime exception is thrown when a field cannot be parsed from a Parquet SimpleGroup.
 **/
public class SimpleGroupParsingException extends RuntimeException {

    /**
     * Instantiates a new Simple group parsing exception with the specified detail message.
     *
     * @param message the detail message describing why the field could not be parsed
     */
    public SimpleGroupParsingException(String message) {
        super(message);
    }
}
