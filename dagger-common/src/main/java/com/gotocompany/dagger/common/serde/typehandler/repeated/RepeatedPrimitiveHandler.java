package com.gotocompany.dagger.common.serde.typehandler.repeated;

import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.serde.typehandler.primitive.PrimitiveHandler;
import com.gotocompany.dagger.common.serde.typehandler.primitive.PrimitiveHandlerFactory;
import com.gotocompany.dagger.common.serde.typehandler.PrimitiveTypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import com.google.gson.Gson;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;

/**
 * The type Repeated primitive proto handler.
 */
public class RepeatedPrimitiveHandler implements TypeHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the repeated primitive field being converted.
     */
    private final FieldDescriptor fieldDescriptor;
    /**
     * Shared Gson instance used to serialize the primitive array to JSON.
     */
    private static final Gson GSON = new Gson();

    /**
     * Instantiates a new Repeated primitive proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public RepeatedPrimitiveHandler(FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is repeated and neither a message nor an enum
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.isRepeated() && fieldDescriptor.getJavaType() != MESSAGE && fieldDescriptor.getJavaType() != ENUM;
    }

    /**
     * Sets the repeated primitive field on the builder from a list or array of values.
     *
     * <p>An array input is first wrapped in a {@code List}. When the handler cannot apply or
     * {@code field} is {@code null}, the builder is returned unchanged.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the collection of primitive values to set, or {@code null} to skip
     * @return the same {@code builder}, with the repeated field set when provided
     */
    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }
        if (field.getClass().isArray()) {
            field = Arrays.asList((Object[]) field);
        }
        return builder.setField(fieldDescriptor, field);
    }

    /**
     * Converts a post-processor list of values into a list of parsed primitives.
     *
     * @param field the list of raw values emitted by an upstream post processor, or {@code null}
     * @return a list of parsed primitive values, empty when {@code field} is {@code null}
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        ArrayList<Object> outputValues = new ArrayList<>();
        if (field != null) {
            List<Object> inputValues = (List<Object>) field;
            PrimitiveTypeHandler primitiveTypeHandler = new PrimitiveTypeHandler(fieldDescriptor);
            for (Object inputField : inputValues) {
                outputValues.add(primitiveTypeHandler.transformFromPostProcessor(inputField));
            }
        }
        return outputValues;
    }

    /**
     * Converts the repeated primitive values read from a protobuf message into a primitive array.
     *
     * @param field the repeated primitive value read from the message
     * @return the values as a primitive array of the field's type
     */
    @Override
    public Object transformFromProto(Object field) {
        PrimitiveHandler primitiveHandler = PrimitiveHandlerFactory.getTypeHandler(fieldDescriptor);
        return primitiveHandler.parseRepeatedObjectField(field);
    }

    /**
     * Converts the repeated protobuf primitives into a primitive array, ignoring the cache.
     *
     * @param field the repeated primitive value read from the message
     * @param cache the field descriptor cache, unused for primitive fields
     * @return the values as a primitive array of the field's type
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        PrimitiveHandler primitiveHandler = PrimitiveHandlerFactory.getTypeHandler(fieldDescriptor);
        return primitiveHandler.parseRepeatedObjectField(field);
    }

    /**
     * Reads the repeated primitive field from a Parquet {@code SimpleGroup} into a primitive array.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the values as a primitive array of the field's type
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        PrimitiveHandler primitiveHandler = PrimitiveHandlerFactory.getTypeHandler(fieldDescriptor);
        return primitiveHandler.parseRepeatedSimpleGroupField(simpleGroup);
    }

    /**
     * Serializes the repeated primitive values to a JSON array.
     *
     * @param field the primitive array to serialize
     * @return the JSON string for the array
     */
    @Override
    public Object transformToJson(Object field) {
        return GSON.toJson(field);
    }

    /**
     * Returns the Flink {@code TypeInformation} used to represent this repeated primitive field.
     *
     * @return the array type supplied by the matching {@code PrimitiveHandler}
     */
    @Override
    public TypeInformation getTypeInformation() {
        return PrimitiveHandlerFactory.getTypeHandler(fieldDescriptor).getArrayType();
    }
}
