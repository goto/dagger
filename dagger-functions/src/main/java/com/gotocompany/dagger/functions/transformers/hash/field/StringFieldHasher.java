package com.gotocompany.dagger.functions.transformers.hash.field;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.functions.exceptions.RowHashException;

import java.nio.charset.StandardCharsets;

/**
 * The String field hasher.
 */
public class StringFieldHasher implements FieldHasher {

    /**
     * Dot-separated path identifying the field this hasher masks; for a primitive it holds a single segment.
     */
    private final String[] fieldPath;

    /**
     * Instantiates a new String field hasher.
     *
     * @param fieldPath the field path
     */
    public StringFieldHasher(String[] fieldPath) {
        this.fieldPath = fieldPath;
    }

    /**
     * Hashes the given string value using SHA-256 and returns the hexadecimal hash.
     *
     * @param elem the string field value to hash
     * @return the hashed string value
     * @throws RowHashException if the value cannot be hashed as a string
     */
    @Override
    public Object maskRow(Object elem) {
        try {
            String fieldValue = getHashFunction()
                    .hashString((String) elem, StandardCharsets.UTF_8)
                    .toString();
            return fieldValue;
        } catch (Exception ex) {
            throw new RowHashException("Unable to hash String value for field : " + fieldPath[0], ex);
        }
    }

    /**
     * Determines whether this hasher can mask the given field.
     *
     * <p>Returns {@code true} only for a single-segment path that points to a valid, non-repeated field
     * of protobuf string type.
     *
     * @param fieldDescriptor the descriptor of the field to be masked
     * @return {@code true} if this hasher can process the field, {@code false} otherwise
     */
    @Override
    public boolean canProcess(Descriptors.FieldDescriptor fieldDescriptor) {
        return fieldPath.length == 1
                && isValidNonRepeatedField(fieldDescriptor)
                && fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING;
    }

    /**
     * Returns this hasher unchanged, since a string field has no child to configure.
     *
     * @param fieldDescriptor the descriptor of the field being masked
     * @return this hasher
     */
    @Override
    public FieldHasher setChild(Descriptors.FieldDescriptor fieldDescriptor) {
        return this;
    }
}
