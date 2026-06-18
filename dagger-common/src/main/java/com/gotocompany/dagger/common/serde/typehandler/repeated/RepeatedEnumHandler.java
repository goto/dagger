package com.gotocompany.dagger.common.serde.typehandler.repeated;

import com.google.gson.Gson;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.exceptions.serde.EnumFieldNotFoundException;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.ObjectArrayTypeInfo;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.ENUM;

/**
 * The type Repeated enum proto handler.
 */
public class RepeatedEnumHandler implements TypeHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the repeated enum field this handler processes.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;
    /**
     * Shared Gson instance used to serialize the enum-name array to JSON.
     */
    private static final Gson GSON = new Gson();

    /**
     * Instantiates a new Repeated enum proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public RepeatedEnumHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is a repeated protobuf {@code enum}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == ENUM && fieldDescriptor.isRepeated();
    }

    /**
     * Sets the repeated enum field on the builder by resolving each value's name to a constant.
     *
     * <p>The input may be an array or a {@code List} of enum constant names. When the handler
     * cannot apply or {@code field} is {@code null}, the builder is returned unchanged.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the collection of enum constant names to set, or {@code null} to skip
     * @return the same {@code builder}, with the repeated enum field set when provided
     * @throws EnumFieldNotFoundException if any name does not match a constant of the enum
     */
    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }
        List<Object> rowElements = field.getClass().isArray() ? Arrays.asList((Object[]) field) : (List) field;

        List<Descriptors.EnumValueDescriptor> value = rowElements.stream()
                .map(this::getEnumValue)
                .collect(Collectors.toList());

        builder.setField(fieldDescriptor, value);
        return builder;
    }

    /**
     * Resolves a single value to its protobuf enum value descriptor by name.
     *
     * @param field the enum constant name to resolve
     * @return the matching enum value descriptor
     * @throws EnumFieldNotFoundException if the name does not match any constant of the enum
     */
    private Descriptors.EnumValueDescriptor getEnumValue(Object field) {
        String stringValue = String.valueOf(field).trim();
        Descriptors.EnumValueDescriptor valueByName = fieldDescriptor.getEnumType().findValueByName(stringValue);
        if (valueByName == null) {
            throw new EnumFieldNotFoundException("field: " + stringValue + " not found in " + fieldDescriptor.getFullName());
        }
        return valueByName;
    }

    /**
     * Converts a post-processor value into an array of enum constant names.
     *
     * @param field the collection of enum values emitted by an upstream post processor
     * @return a {@code String[]} of enum constant names
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        return getValue(field);
    }

    /**
     * Converts the repeated enum values read from a protobuf message into an array of names.
     *
     * @param field the repeated enum value read from the message
     * @return a {@code String[]} of enum constant names
     */
    @Override
    public Object transformFromProto(Object field) {
        return getValue(field);
    }

    /**
     * Converts the repeated protobuf enum values into an array of names, ignoring the cache.
     *
     * @param field the repeated enum value read from the message
     * @param cache the field descriptor cache, unused for enum fields
     * @return a {@code String[]} of enum constant names
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return getValue(field);
    }

    /**
     * Reads the repeated enum field from a Parquet {@code SimpleGroup} into an array of names.
     *
     * <p>Unknown values fall back to the enum's zero-numbered default constant.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return a {@code String[]} of enum constant names, empty when the field is absent
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        String defaultEnumValue = fieldDescriptor.getEnumType().findValueByNumber(0).getName();
        List<String> enumArrayList = new ArrayList<>();
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            for (int positionIndex = 0; positionIndex < repetitionCount; positionIndex++) {
                String extractedValue = simpleGroup.getString(fieldName, positionIndex);
                Descriptors.EnumValueDescriptor enumValueDescriptor = fieldDescriptor.getEnumType().findValueByName(extractedValue);
                String enumValue = enumValueDescriptor == null ? defaultEnumValue : enumValueDescriptor.getName();
                enumArrayList.add(enumValue);
            }
        }
        return enumArrayList.toArray(new String[]{});
    }

    /**
     * Serializes the repeated enum values to a JSON array of constant names.
     *
     * @param field the repeated enum value to serialize
     * @return the JSON string for the array of enum names
     */
    @Override
    public Object transformToJson(Object field) {
        return GSON.toJson(getValue(field));
    }

    /**
     * Returns the Flink {@code TypeInformation} used to represent this repeated enum field.
     *
     * @return an object-array type of {@code String}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return ObjectArrayTypeInfo.getInfoFor(Types.STRING);
    }

    /**
     * Converts a collection of enum values into an array of their string names.
     *
     * @param field the collection of enum values, or {@code null}
     * @return a {@code String[]} of names, empty when {@code field} is {@code null}
     */
    private Object getValue(Object field) {
        List<String> values = new ArrayList<>();
        if (field != null) {
            values = getStringRow((List) field);
        }
        return values.toArray(new String[]{});
    }

    /**
     * Maps each element of the given list to its string representation.
     *
     * @param protos the list of enum values to stringify
     * @return the list of string names
     */
    private List<String> getStringRow(List<Object> protos) {
        return protos
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }
}
