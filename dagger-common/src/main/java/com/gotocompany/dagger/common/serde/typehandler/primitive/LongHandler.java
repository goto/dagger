package com.gotocompany.dagger.common.serde.typehandler.primitive;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Long primitive type handler.
 */
public class LongHandler implements PrimitiveHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the long field this handler processes.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Long primitive type handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public LongHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field's Java type is {@code LONG}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == JavaType.LONG;
    }

    /**
     * Parses the given value into a Java {@code long}.
     *
     * @param field the value to parse, defaulting to {@code 0} when {@code null}
     * @return the parsed long value
     */
    @Override
    public Object parseObject(Object field) {
        return Long.parseLong(getValueOrDefault(field, "0"));
    }

    /**
     * Reads the long value for this field from a Parquet {@code SimpleGroup}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the long value, or {@code 0L} when the field is absent
     */
    @Override
    public Object parseSimpleGroup(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();

        /* this if branch checks that the field name exists in the simple group schema and is initialized */
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            return simpleGroup.getLong(fieldName, 0);
        } else {
            /* return default value */
            return 0L;
        }
    }

    /**
     * Converts a list of long values into a {@code Long[]}.
     *
     * @param field the list of long values, or {@code null}
     * @return the values as a {@code Long[]}, empty when {@code field} is {@code null}
     */
    @Override
    public Object parseRepeatedObjectField(Object field) {
        List<Long> inputValues = new ArrayList<>();
        if (field != null) {
            inputValues = (List<Long>) field;
        }
        return inputValues.toArray(new Long[]{});
    }

    /**
     * Reads the repeated long field from a Parquet {@code SimpleGroup} into a {@code Long[]}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the long array, empty when the field is absent
     */
    @Override
    public Object parseRepeatedSimpleGroupField(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        ArrayList<Long> longArrayList = new ArrayList<>();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            for (int i = 0; i < repetitionCount; i++) {
                longArrayList.add(simpleGroup.getLong(fieldName, i));
            }
        }
        return longArrayList.toArray(new Long[]{});
    }

    /**
     * Returns the Flink {@code TypeInformation} for a single long value.
     *
     * @return {@code Types.LONG}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.LONG;
    }

    /**
     * Returns the Flink {@code TypeInformation} for a repeated long field.
     *
     * @return an object-array type of {@code Long}
     */
    @Override
    public TypeInformation getArrayType() {
        return Types.OBJECT_ARRAY(Types.LONG);
    }
}
