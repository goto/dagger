package com.gotocompany.dagger.functions.transformers.hash.field;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.functions.exceptions.RowHashException;

/**
 * The Integer field hasher.
 */
public class IntegerFieldHasher implements FieldHasher {

    /**
     * Dot-separated path identifying the field this hasher masks; for a primitive it holds a single segment.
     */
    private final String[] fieldPath;

    /**
     * Instantiates a new Integer field hasher.
     *
     * @param fieldPath the field path
     */
    public IntegerFieldHasher(String[] fieldPath) {
        this.fieldPath = fieldPath;
    }

    /**
     * Hashes the given integer value using SHA-256 and returns the hashed integer.
     *
     * @param elem the integer field value to hash
     * @return the hashed integer value
     * @throws RowHashException if the value cannot be hashed as an integer
     */
    @Override
    public Object maskRow(Object elem) {
        try {
            int fieldValue = getHashFunction()
                    .hashInt((Integer) elem)
                    .asInt();
            return fieldValue;
        } catch (Exception ex) {
            throw new RowHashException("Unable to hash int value for field : " + fieldPath[0], ex);
        }
    }

    /**
     * Determines whether this hasher can mask the given field.
     *
     * <p>Returns {@code true} only for a single-segment path that points to a valid, non-repeated field
     * of protobuf integer type.
     *
     * @param fieldDescriptor the descriptor of the field to be masked
     * @return {@code true} if this hasher can process the field, {@code false} otherwise
     */
    @Override
    public boolean canProcess(Descriptors.FieldDescriptor fieldDescriptor) {
        return fieldPath.length == 1
                && isValidNonRepeatedField(fieldDescriptor)
                && fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.INT;
    }

    /**
     * Returns this hasher unchanged, since an integer field has no child to configure.
     *
     * @param fieldDescriptor the descriptor of the field being masked
     * @return this hasher
     */
    @Override
    public FieldHasher setChild(Descriptors.FieldDescriptor fieldDescriptor) {
        return this;
    }
}
