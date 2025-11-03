package com.gotocompany.dagger.common.serde.typehandler.complex;

import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;

import com.gotocompany.dagger.common.serde.typehandler.primitive.StringHandler;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.io.IOException;

/**
 * Handles google.protobuf.Value fields by delegating to a {@link StringHandler}.
 * There are two concerns with Value type
 * 1. It has cyclic reference - https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/struct.proto#L94
 * 2. oneof usage; i.e. It has no specific type information, every message can be of different type
 *  - https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/struct.proto#L64
 *
 * <p>This handler treats {@link Value} as a plain string:
 * - Converts between String and Value
 * - Ignores nested structures (STRUCT_VALUE, LIST_VALUE)
 * - Useful as a lightweight workaround to recursion in google.protobuf.Value
 */
public class GoogleProtobufValueHandler implements TypeHandler {

    private final Descriptors.FieldDescriptor fieldDescriptor;
    private final StringHandler stringHandler;

    /**
     * Instantiates a new Value handler with an internal {@link StringHandler}.
     *
     * @param fieldDescriptor the protobuf field descriptor
     */
    public GoogleProtobufValueHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
        // Initialize a basic string handler for fallback operations
        this.stringHandler = new StringHandler(fieldDescriptor);
    }

    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE &&
                "google.protobuf.Value".equals(fieldDescriptor.getMessageType().getFullName());
    }

    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }

        Value valueMessage;
        Object parsedField = stringHandler.parseObject(field);

        if (parsedField instanceof String) {
            String stringValue = (String) parsedField;
            try {
                // Attempt JSON parsing
                Value.Builder valBuilder = Value.newBuilder();
                JsonFormat.parser().merge(stringValue, valBuilder);
                valueMessage = valBuilder.build();
            } catch (IOException e) {
                // Not JSON — store as plain string
                valueMessage = Value.newBuilder().setStringValue(stringValue).build();
            }
        } else {
            // Fallback: stringify the object
            valueMessage = Value.newBuilder().setStringValue(parsedField.toString()).build();
        }

        builder.setField(fieldDescriptor, valueMessage);
        return builder;
    }

    @Override
    public Object transformFromPostProcessor(Object field) {
        return field == null ? "" : field.toString();
    }

    @Override
    public Object transformFromProto(Object field) {
        if (field instanceof Value) {
            Value value = (Value) field;
            return extractString(value);
        } else if (field instanceof DynamicMessage) {
            // Convert DynamicMessage back to Value then to string
            try {
                Value.Builder builder = Value.newBuilder();
                JsonFormat.parser().merge(JsonFormat.printer().print((DynamicMessage) field), builder);
                return extractString(builder.build());
            } catch (Exception e) {
                return field.toString();
            }
        }
        return "";
    }

    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return transformFromProto(field);
    }

    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        return stringHandler.parseSimpleGroup(simpleGroup);
    }

    @Override
    public Object transformToJson(Object field) {
        return field == null ? "" : field.toString();
    }

    @Override
    public TypeInformation getTypeInformation() {
        return Types.STRING;
    }

    /**
     * Extracts a string representation of a {@link Value} protobuf.
     */
    private String extractString(Value value) {
        switch (value.getKindCase()) {
            case STRING_VALUE:
                return value.getStringValue();
            case NUMBER_VALUE:
                return String.valueOf(value.getNumberValue());
            case BOOL_VALUE:
                return String.valueOf(value.getBoolValue());
            case NULL_VALUE:
                return "null";
            case STRUCT_VALUE:
            case LIST_VALUE:
                try {
                    return JsonFormat.printer().print(value);
                } catch (IOException e) {
                    return value.toString();
                }
            case KIND_NOT_SET:
            default:
                return "";
        }
    }
}
