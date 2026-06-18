package com.gotocompany.dagger.common.exceptions.serde;

/**
 * Unchecked exception thrown when a configured JSON schema cannot be parsed or is structurally
 * invalid.
 *
 * <p>Dagger can derive types and field mappings from a JSON schema when consuming or producing
 * JSON-encoded data. If that schema is malformed or cannot be interpreted, this exception wraps the
 * underlying parsing failure so the job fails fast instead of proceeding with an unusable schema.
 */
public class InvalidJSONSchemaException extends RuntimeException {
    /**
     * Creates a new exception that wraps the underlying cause of the schema parsing failure.
     *
     * @param innerException the exception thrown while reading or parsing the JSON schema
     */
    public InvalidJSONSchemaException(Exception innerException) {
        super(innerException);
    }
}
