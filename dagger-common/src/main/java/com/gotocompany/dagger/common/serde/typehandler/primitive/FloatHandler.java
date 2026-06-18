package com.gotocompany.dagger.common.serde.typehandler.primitive;

import com.google.common.primitives.Floats;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.List;

/**
 * The type Float primitive type handler.
 */
public class FloatHandler implements PrimitiveHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the float field this handler processes.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Float primitive type handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public FloatHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field's Java type is {@code FLOAT}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == JavaType.FLOAT;
    }

    /**
     * Parses the given value into a Java {@code float}.
     *
     * @param field the value to parse, defaulting to {@code 0} when {@code null}
     * @return the parsed float value
     */
    @Override
    public Object parseObject(Object field) {
        return Float.parseFloat(getValueOrDefault(field, "0"));
    }

    /**
     * Reads the float value for this field from a Parquet {@code SimpleGroup}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the float value, or {@code 0.0F} when the field is absent
     */
    @Override
    public Object parseSimpleGroup(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();

        /* this if branch checks that the field name exists in the simple group schema and is initialized */
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            return simpleGroup.getFloat(fieldName, 0);
        } else {
            /* return default value */
            return 0.0F;
        }
    }

    /**
     * Converts a list of float values into a primitive {@code float[]}.
     *
     * @param field the list of float values, or {@code null}
     * @return the values as a {@code float[]}, empty when {@code field} is {@code null}
     */
    @Override
    public Object parseRepeatedObjectField(Object field) {

        float[] inputValues = new float[0];
        if (field != null) {
            inputValues = Floats.toArray((List<Float>) field);
        }
        return inputValues;
    }

    /**
     * Reads the repeated float field from a Parquet {@code SimpleGroup} into a {@code float[]}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the float array, empty when the field is absent
     */
    @Override
    public Object parseRepeatedSimpleGroupField(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            float[] floatArray = new float[repetitionCount];
            for (int i = 0; i < repetitionCount; i++) {
                floatArray[i] = simpleGroup.getFloat(fieldName, i);
            }
            return floatArray;
        }
        return new float[0];
    }

    /**
     * Returns the Flink {@code TypeInformation} for a single float value.
     *
     * @return {@code Types.FLOAT}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.FLOAT;
    }

    /**
     * Returns the Flink {@code TypeInformation} for a repeated float field.
     *
     * @return a primitive float-array type
     */
    @Override
    public TypeInformation getArrayType() {
        return Types.PRIMITIVE_ARRAY(Types.FLOAT);
    }
}
