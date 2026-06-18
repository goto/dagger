package com.gotocompany.dagger.common.serde.typehandler.primitive;

import com.google.common.primitives.Booleans;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.parquet.example.data.simple.SimpleGroup;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;

import java.util.List;

/**
 * The type Boolean primitive type handler.
 */
public class BooleanHandler implements PrimitiveHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the boolean field this handler processes.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Boolean primitive type handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public BooleanHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field's Java type is {@code BOOLEAN}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == JavaType.BOOLEAN;
    }

    /**
     * Parses the given value into a Java {@code boolean}.
     *
     * @param field the value to parse, defaulting to {@code false} when {@code null}
     * @return the parsed boolean value
     */
    @Override
    public Object parseObject(Object field) {
        return Boolean.parseBoolean(getValueOrDefault(field, "false"));
    }

    /**
     * Reads the boolean value for this field from a Parquet {@code SimpleGroup}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the boolean value, or {@code false} when the field is absent
     */
    @Override
    public Object parseSimpleGroup(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();

        /* this if branch checks that the field name exists in the simple group schema and is initialized */
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            return simpleGroup.getBoolean(fieldName, 0);
        } else {
            /* return default value */
            return false;
        }
    }

    /**
     * Converts a list of boolean values into a primitive {@code boolean[]}.
     *
     * @param field the list of boolean values, or {@code null}
     * @return the values as a {@code boolean[]}, empty when {@code field} is {@code null}
     */
    @Override
    public Object parseRepeatedObjectField(Object field) {
        boolean[] inputValues = new boolean[0];
        if (field != null) {
            inputValues = Booleans.toArray((List<Boolean>) field);
        }
        return inputValues;
    }

    /**
     * Reads the repeated boolean field from a Parquet {@code SimpleGroup} into a {@code boolean[]}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the boolean array, empty when the field is absent
     */
    @Override
    public Object parseRepeatedSimpleGroupField(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            boolean[] booleanArray = new boolean[repetitionCount];
            for (int i = 0; i < repetitionCount; i++) {
                booleanArray[i] = simpleGroup.getBoolean(fieldName, i);
            }
            return booleanArray;
        }
        return new boolean[0];
    }

    /**
     * Returns the Flink {@code TypeInformation} for a single boolean value.
     *
     * @return {@code Types.BOOLEAN}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.BOOLEAN;
    }

    /**
     * Returns the Flink {@code TypeInformation} for a repeated boolean field.
     *
     * @return a primitive boolean-array type
     */
    @Override
    public TypeInformation getArrayType() {
        return Types.PRIMITIVE_ARRAY(Types.BOOLEAN);
    }
}
