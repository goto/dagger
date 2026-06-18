package com.gotocompany.dagger.common.serde.typehandler.primitive;

import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Byte string primitive type handler.
 */
public class ByteStringHandler implements PrimitiveHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the byte-string field this handler processes.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Byte string primitive type handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public ByteStringHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field's Java type is {@code BYTE_STRING}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == JavaType.BYTE_STRING;
    }

    /**
     * Returns the given byte-string value unchanged.
     *
     * @param field the value to pass through
     * @return the same {@code field} value
     */
    @Override
    public Object parseObject(Object field) {
        return field;
    }

    /**
     * Reads the byte-string value for this field from a Parquet {@code SimpleGroup}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the value as a {@code ByteString}, or {@code null} when the field is absent
     */
    @Override
    public Object parseSimpleGroup(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();

        /* this if branch checks that the field name exists in the simple group schema and is initialized */
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            byte[] byteArray = simpleGroup.getBinary(fieldName, 0).getBytes();
            return ByteString.copyFrom(byteArray);
        } else {
            return null;
        }
    }

    /**
     * Converts a list of byte-string values into a {@code ByteString[]}.
     *
     * @param field the list of {@code ByteString} values, or {@code null}
     * @return the values as a {@code ByteString[]}, empty when {@code field} is {@code null}
     */
    @Override
    public Object parseRepeatedObjectField(Object field) {
        List<ByteString> inputValues = new ArrayList<>();
        if (field != null) {
            inputValues = (List<ByteString>) field;
        }
        return inputValues.toArray(new ByteString[]{});
    }

    /**
     * Reads the repeated byte-string field from a Parquet {@code SimpleGroup} into a {@code ByteString[]}.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the byte-string array, empty when the field is absent
     */
    @Override
    public Object parseRepeatedSimpleGroupField(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        ArrayList<ByteString> byteStringList = new ArrayList<>();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            for (int i = 0; i < repetitionCount; i++) {
                byte[] byteArray = simpleGroup.getBinary(fieldName, i).getBytes();
                byteStringList.add(ByteString.copyFrom(byteArray));
            }
        }
        return byteStringList.toArray(new ByteString[]{});
    }

    /**
     * Returns the Flink {@code TypeInformation} for a single byte-string value.
     *
     * @return the type information for {@code ByteString}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return TypeInformation.of(ByteString.class);
    }

    /**
     * Returns the Flink {@code TypeInformation} for a repeated byte-string field.
     *
     * @return an object-array type of {@code ByteString}
     */
    @Override
    public TypeInformation getArrayType() {
        return Types.OBJECT_ARRAY(TypeInformation.of(ByteString.class));
    }
}
