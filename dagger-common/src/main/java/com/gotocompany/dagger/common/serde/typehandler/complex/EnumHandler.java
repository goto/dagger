package com.gotocompany.dagger.common.serde.typehandler.complex;

import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.exceptions.serde.EnumFieldNotFoundException;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.apache.parquet.example.data.simple.SimpleGroup;

/**
 * The type Enum proto handler.
 */
public class EnumHandler implements TypeHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the enum field this handler converts to and
     * from its string name representation.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Enum proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public EnumHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is a non-repeated protobuf {@code enum}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM && !fieldDescriptor.isRepeated();
    }

    /**
     * Sets the enum field on the builder by resolving the value's name to an enum constant.
     *
     * <p>The incoming value is treated as the enum constant name (trimmed). When the handler
     * cannot apply or {@code field} is {@code null}, the builder is returned unchanged.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the enum constant name to set, or {@code null} to skip
     * @return the same {@code builder}, with the enum field set when resolvable
     * @throws EnumFieldNotFoundException if the name does not match any constant of the enum
     */
    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }
        String stringValue = String.valueOf(field).trim();
        Descriptors.EnumValueDescriptor valueByName = fieldDescriptor.getEnumType().findValueByName(stringValue);
        if (valueByName == null) {
            throw new EnumFieldNotFoundException("field: " + stringValue + " not found in " + fieldDescriptor.getFullName());
        }
        return builder.setField(fieldDescriptor, valueByName);
    }

    /**
     * Resolves a post-processor value to a protobuf enum constant name.
     *
     * <p>The input may be either the enum's numeric position or its name; when it matches
     * neither, the enum's zero-numbered (default) constant name is returned.
     *
     * @param field the value to resolve, defaulting to {@code "0"} when {@code null}
     * @return the resolved enum constant name
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        String input = field != null ? field.toString() : "0";
        try {
            int enumPosition = Integer.parseInt(input);
            Descriptors.EnumValueDescriptor valueByNumber = fieldDescriptor.getEnumType().findValueByNumber(enumPosition);
            return valueByNumber != null ? valueByNumber.getName() : fieldDescriptor.getEnumType().findValueByNumber(0).getName();
        } catch (NumberFormatException e) {
            Descriptors.EnumValueDescriptor valueByName = fieldDescriptor.getEnumType().findValueByName(input);
            return valueByName != null ? valueByName.getName() : fieldDescriptor.getEnumType().findValueByNumber(0).getName();
        }
    }

    /**
     * Converts an enum value read from a protobuf message into its trimmed string name.
     *
     * @param field the enum value descriptor read from the message
     * @return the enum constant name as a string
     */
    @Override
    public Object transformFromProto(Object field) {
        return String.valueOf(field).trim();
    }

    /**
     * Converts the protobuf enum value into its trimmed string name, ignoring the cache.
     *
     * @param field the enum value descriptor read from the message
     * @param cache the field descriptor cache, unused for enum fields
     * @return the enum constant name as a string
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return String.valueOf(field).trim();
    }

    /**
     * Reads the enum field from a Parquet {@code SimpleGroup} as its constant name.
     *
     * <p>Unknown or absent values fall back to the enum's zero-numbered default constant.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the resolved enum constant name, or the default constant name when missing
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        String defaultEnumValue = fieldDescriptor.getEnumType().findValueByNumber(0).getName();
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            String parquetEnumValue = simpleGroup.getString(fieldName, 0);
            Descriptors.EnumValueDescriptor enumValueDescriptor = fieldDescriptor.getEnumType().findValueByName(parquetEnumValue);
            return enumValueDescriptor == null ? defaultEnumValue : enumValueDescriptor.getName();
        }
        return defaultEnumValue;
    }

    /**
     * Returns the enum constant name unchanged for JSON serialization.
     *
     * @param field the enum constant name
     * @return the same {@code field} value
     */
    @Override
    public Object transformToJson(Object field) {
        return field;
    }

    /**
     * Returns the Flink {@code TypeInformation} used to represent this enum field.
     *
     * @return {@code Types.STRING}, since enum constants are represented by their name
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.STRING;
    }

}
