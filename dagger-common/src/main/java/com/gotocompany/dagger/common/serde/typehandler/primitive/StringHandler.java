package com.gotocompany.dagger.common.serde.typehandler.primitive;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.ObjectArrayTypeInfo;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * The type String primitive type handler.
 */
public class StringHandler implements PrimitiveHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the string field this handler processes.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new String primitive type handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public StringHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field's Java type is {@code STRING}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == JavaType.STRING;
    }

    /**
     * Converts the given value into its string form.
     *
     * @param field the value to convert, defaulting to an empty string when {@code null}
     * @return the value's string representation
     */
    @Override
    public Object parseObject(Object field) {
        return getValueOrDefault(field, "");
    }

    /**
     * Reads the string value for this field from a Parquet {@code SimpleGroup}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the string value, or an empty string when the field is absent
     */
    @Override
    public Object parseSimpleGroup(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();

        /* this if branch checks that the field name exists in the simple group schema and is initialized */
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            return simpleGroup.getString(fieldName, 0);
        } else {
            /* return default value */
            return "";
        }
    }

    /**
     * Converts a list of string values into a {@code String[]}.
     *
     * @param field the list of string values, or {@code null}
     * @return the values as a {@code String[]}, empty when {@code field} is {@code null}
     */
    @Override
    public Object parseRepeatedObjectField(Object field) {
        List<String> inputValues = new ArrayList<>();
        if (field != null) {
            inputValues = (List<String>) field;
        }
        return inputValues.toArray(new String[]{});
    }

    /**
     * Reads the repeated string field from a Parquet {@code SimpleGroup} into a {@code String[]}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the string array, empty when the field is absent
     */
    @Override
    public Object parseRepeatedSimpleGroupField(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            String[] stringArray = new String[repetitionCount];
            for (int i = 0; i < repetitionCount; i++) {
                stringArray[i] = simpleGroup.getString(fieldName, i);
            }
            return stringArray;
        }
        return new String[0];
    }

    /**
     * Returns the Flink {@code TypeInformation} for a single string value.
     *
     * @return {@code Types.STRING}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.STRING;
    }

    /**
     * Returns the Flink {@code TypeInformation} for a repeated string field.
     *
     * @return an object-array type of {@code String}
     */
    @Override
    public TypeInformation getArrayType() {
        return ObjectArrayTypeInfo.getInfoFor(Types.STRING);
    }
}
