package com.gotocompany.dagger.common.serde.typehandler.primitive;

import com.google.common.primitives.Doubles;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.List;

/**
 * The type Double primitive type handler.
 */
public class DoubleHandler implements PrimitiveHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the double field this handler processes.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Double primitive type handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public DoubleHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field's Java type is {@code DOUBLE}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == JavaType.DOUBLE;
    }

    /**
     * Parses the given value into a Java {@code double}.
     *
     * @param field the value to parse, defaulting to {@code 0} when {@code null}
     * @return the parsed double value
     */
    @Override
    public Object parseObject(Object field) {
        return Double.parseDouble(getValueOrDefault(field, "0"));
    }

    /**
     * Reads the double value for this field from a Parquet {@code SimpleGroup}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the double value, or {@code 0.0} when the field is absent
     */
    @Override
    public Object parseSimpleGroup(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();

        /* this if branch checks that the field name exists in the simple group schema and is initialized */
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            return simpleGroup.getDouble(fieldName, 0);
        } else {
            /* return default value */
            return 0.0D;
        }
    }

    /**
     * Converts a list of double values into a primitive {@code double[]}.
     *
     * @param field the list of double values, or {@code null}
     * @return the values as a {@code double[]}, empty when {@code field} is {@code null}
     */
    @Override
    public Object parseRepeatedObjectField(Object field) {
        double[] inputValues = new double[0];
        if (field != null) {
            inputValues = Doubles.toArray((List<Double>) field);
        }
        return inputValues;
    }

    /**
     * Reads the repeated double field from a Parquet {@code SimpleGroup} into a {@code double[]}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the double array, empty when the field is absent
     */
    @Override
    public Object parseRepeatedSimpleGroupField(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            double[] doubleArray = new double[repetitionCount];
            for (int i = 0; i < repetitionCount; i++) {
                doubleArray[i] = simpleGroup.getDouble(fieldName, i);
            }
            return doubleArray;
        }
        return new double[0];
    }

    /**
     * Returns the Flink {@code TypeInformation} for a single double value.
     *
     * @return {@code Types.DOUBLE}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.DOUBLE;
    }

    /**
     * Returns the Flink {@code TypeInformation} for a repeated double field.
     *
     * @return a primitive double-array type
     */
    @Override
    public TypeInformation getArrayType() {
        return Types.PRIMITIVE_ARRAY(Types.DOUBLE);
    }
}
