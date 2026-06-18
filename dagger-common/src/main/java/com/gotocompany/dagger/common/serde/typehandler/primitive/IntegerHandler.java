package com.gotocompany.dagger.common.serde.typehandler.primitive;

import com.google.common.primitives.Ints;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.List;

/**
 * The type Integer primitive type handler.
 */
public class IntegerHandler implements PrimitiveHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the integer field this handler processes.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Integer primitive type handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public IntegerHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field's Java type is {@code INT}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == JavaType.INT;
    }

    /**
     * Parses the given value into a Java {@code int}.
     *
     * @param field the value to parse, defaulting to {@code 0} when {@code null}
     * @return the parsed integer value
     */
    @Override
    public Object parseObject(Object field) {
        return Integer.parseInt(getValueOrDefault(field, "0"));
    }

    /**
     * Reads the integer value for this field from a Parquet {@code SimpleGroup}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the integer value, or {@code 0} when the field is absent
     */
    @Override
    public Object parseSimpleGroup(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();

        /* this if branch checks that the field name exists in the simple group schema and is initialized */
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            return simpleGroup.getInteger(fieldName, 0);
        } else {
            /* return default value */
            return 0;
        }
    }

    /**
     * Converts a list of integer values into a primitive {@code int[]}.
     *
     * @param field the list of integer values, or {@code null}
     * @return the values as an {@code int[]}, empty when {@code field} is {@code null}
     */
    @Override
    public Object parseRepeatedObjectField(Object field) {
        int[] inputValues = new int[0];
        if (field != null) {
            inputValues = Ints.toArray((List<Integer>) field);
        }
        return inputValues;
    }

    /**
     * Reads the repeated integer field from a Parquet {@code SimpleGroup} into an {@code int[]}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the integer array, empty when the field is absent
     */
    @Override
    public Object parseRepeatedSimpleGroupField(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            int[] intArray = new int[repetitionCount];
            for (int i = 0; i < repetitionCount; i++) {
                intArray[i] = simpleGroup.getInteger(fieldName, i);
            }
            return intArray;
        }
        return new int[0];
    }

    /**
     * Returns the Flink {@code TypeInformation} for a single integer value.
     *
     * @return {@code Types.INT}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.INT;
    }

    /**
     * Returns the Flink {@code TypeInformation} for a repeated integer field.
     *
     * @return a primitive int-array type
     */
    @Override
    public TypeInformation getArrayType() {
        return Types.PRIMITIVE_ARRAY(Types.INT);
    }
}
