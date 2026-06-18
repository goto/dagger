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

    /**
     * The set of fully qualified protobuf message names that this handler recognizes as
     * dynamic, JSON-like complex types ({@code Struct}, {@code Value}, {@code ListValue}
     * and {@code NullValue}).
     */
    private static final Set<String> RECOGNIZED_COMPLEX_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "google.protobuf.Struct",
            "google.protobuf.Value",
            "google.protobuf.ListValue",
            "google.protobuf.NullValue"
    )));

    /**
     * The protobuf {@code FieldDescriptor} of the complex message field handled here.
     */
    private final Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new handler for dynamic Google Protobuf complex message types.
     *
     * @param fieldDescriptor the descriptor of the complex message field to handle
     */
    public GoogleProtobufComplexMessageHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is a message whose type is one of the recognized
     *         complex protobuf types
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                && RECOGNIZED_COMPLEX_TYPES.contains(fieldDescriptor.getMessageType().getFullName());
    }

    /**
     * Serializes a recognized complex protobuf message into its raw byte-array form.
     *
     * <p>Because these {@code Struct}-family types are dynamic and recursive, they are stored
     * as the message's serialized bytes and reconstructed later using the field descriptor.
     *
     * @param field the protobuf value read from the message; expected to be a {@code DynamicMessage}
     * @return the serialized bytes of the message, or {@code null} when the value is empty,
     *         absent, or not a {@code DynamicMessage}
     */
    @Override
    public Object transformFromProto(Object field) {
        if (field == null) {
            return null;
        }

        if (field instanceof DynamicMessage) {
            DynamicMessage msg = (DynamicMessage) field;
            if (msg.getAllFields().isEmpty()) {
                return null;
            }
            return msg.toByteArray();
        }
        return null;
    }

    /**
     * Serializes the complex protobuf message to bytes, delegating to {@code transformFromProto}.
     *
     * <p>The descriptor cache is not required for these self-describing complex types.
     *
     * @param field the protobuf value read from the message
     * @param cache the field descriptor cache, unused for this conversion
     * @return the serialized bytes of the message, or {@code null} when there is no usable value
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return transformFromProto(field);
    }

    /**
     * Reconstructs a complex protobuf message from its byte-array form and sets it on the builder.
     *
     * <p>The {@code field} is expected to be the serialized bytes previously produced by
     * {@code transformFromProto}; it is parsed back into a {@code DynamicMessage} using the
     * field's message type. When the handler cannot apply or {@code field} is {@code null},
     * the builder is returned unchanged.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the serialized message bytes to parse and set, or {@code null} to skip
     * @return the same {@code builder}, with the field set when bytes were provided
     * @throws RuntimeException if the bytes cannot be parsed into the field's message type
     */
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

    /**
     * Returns the post-processor value unchanged.
     *
     * @param field the value supplied by an upstream post processor
     * @return the same {@code field} value
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        return field;
    }

    /**
     * Returns {@code null}, as these complex types are not read from Parquet sources.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return {@code null}, always
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        return null;
    }

    /**
     * Returns {@code null}, as these complex types are not emitted to JSON by this handler.
     *
     * @param field the value that would be serialized
     * @return {@code null}, always
     */
    @Override
    public Object transformToJson(Object field) {
        return null;
    }

    /**
     * Returns the Flink {@code TypeInformation} used to represent this field.
     *
     * @return a primitive byte-array type, matching the serialized byte representation
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.PRIMITIVE_ARRAY(Types.BYTE);
    }
}
