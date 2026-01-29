package com.gotocompany.dagger.common.serde.typehandler.complex;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A TypeHandler to handle some of the complex Google Protobuf message types
 * that are dynamic and recursive in nature.
 * <p>
 * <a href="https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/struct.proto">github-link</a>
 * <p>
 * Struct is primarily used to represent JSON object types.
 * Value can represent any primitive, a Struct, or an array type.
 * ListValue represents a JSON array type.
 * NullValue is an enum used to represent null.
 * <p>
 * This implementation converts these message types to Protobuf's byte-array
 * representation. While outputting the data, the byte array is converted back
 * to the original structure using the associated field descriptor.
 */
public class GoogleProtobufComplexMessageHandler implements TypeHandler {

    private static final Set<String> RECOGNIZED_COMPLEX_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "google.protobuf.Struct",
            "google.protobuf.Value",
            "google.protobuf.ListValue",
            "google.protobuf.NullValue"
    )));

    private final Descriptors.FieldDescriptor fieldDescriptor;

    public GoogleProtobufComplexMessageHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                && RECOGNIZED_COMPLEX_TYPES.contains(fieldDescriptor.getMessageType().getFullName());
    }

    @Override
    public Object transformFromProto(Object field) {
        if (field == null) {
            return null;
        }

        // Struct / Value default instance or empty
        if (field instanceof DynamicMessage) {
            DynamicMessage msg = (DynamicMessage) field;

            // CRITICAL: field not actually set
            if (msg.getAllFields().isEmpty()) {
                return null;
            }

            return msg.toByteArray();
        }

        return null;
    }

    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return transformFromProto(field);
    }

    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }

        try {
            DynamicMessage parsed = DynamicMessage.parseFrom(fieldDescriptor.getMessageType(), (byte[]) field);
            builder.setField(fieldDescriptor, parsed);
            return builder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse protobuf bytes for field: " + fieldDescriptor.getFullName(), e);
        }
    }

    @Override
    public Object transformFromPostProcessor(Object field) {
        return field;
    }

    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        return null;
    }

    @Override
    public Object transformToJson(Object field) {
        return null;
    }

    @Override
    public TypeInformation getTypeInformation() {
        return Types.PRIMITIVE_ARRAY(Types.BYTE);
    }
}
