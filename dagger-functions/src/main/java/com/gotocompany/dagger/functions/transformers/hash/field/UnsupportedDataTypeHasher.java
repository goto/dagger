package com.gotocompany.dagger.functions.transformers.hash.field;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.functions.exceptions.InvalidHashFieldException;

/**
 * The Unsupported data type hasher.
 */
public class UnsupportedDataTypeHasher implements FieldHasher {
    /**
     * Dot-separated path identifying the field that could not be matched to a supported hasher.
     */
    private String[] fieldPath;

    /**
     * Instantiates a new Unsupported data type hasher.
     *
     * @param fieldPath the field path
     */
    public UnsupportedDataTypeHasher(String[] fieldPath) {
        this.fieldPath = fieldPath;
    }

    /**
     * Returns the given value unchanged, since this fallback hasher performs no masking.
     *
     * @param elem the field value
     * @return the value unchanged
     */
    @Override
    public Object maskRow(Object elem) {
        return elem;
    }

    /**
     * Always reports that this fallback hasher cannot process the field.
     *
     * @param fieldDescriptor the descriptor of the field to be masked
     * @return {@code false} always
     */
    @Override
    public boolean canProcess(Descriptors.FieldDescriptor fieldDescriptor) {
        return false;
    }

    /**
     * Always fails because the field has no supported primitive type to hash.
     *
     * @param fieldDescriptor the descriptor of the field being masked
     * @return never returns normally
     * @throws InvalidHashFieldException always, indicating the field cannot be hashed
     */
    @Override
    public FieldHasher setChild(Descriptors.FieldDescriptor fieldDescriptor) {
        if (fieldPath.length == 0 || fieldDescriptor == null) {
            throw new InvalidHashFieldException("No primitive field found for hashing");
        } else {
            throw new InvalidHashFieldException("Inner Field : " + fieldPath[0] + " of data type : " + fieldDescriptor.getJavaType()
                    + " not currently supported for hashing");
        }
    }
}
