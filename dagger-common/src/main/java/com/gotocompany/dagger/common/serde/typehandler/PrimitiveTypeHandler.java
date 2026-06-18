package com.gotocompany.dagger.common.serde.typehandler;

import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.exceptions.serde.InvalidDataTypeException;
import com.gotocompany.dagger.common.serde.typehandler.primitive.PrimitiveHandler;
import com.gotocompany.dagger.common.serde.typehandler.primitive.PrimitiveHandlerFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.apache.parquet.example.data.simple.SimpleGroup;

/**
 * The type Primitive proto handler.
 */
public class PrimitiveTypeHandler implements TypeHandler {
    /**
     * The protobuf {@code FieldDescriptor} describing the primitive field this handler
     * converts between its protobuf representation and the corresponding Flink type.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Primitive proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public PrimitiveTypeHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Indicates that this handler can process the field.
     *
     * <p>{@code PrimitiveTypeHandler} is the fallback handler chosen by
     * {@code TypeHandlerFactory} when no more specific handler matches, so it always
     * reports that it can handle the field.
     *
     * @return {@code true}, always
     */
    @Override
    public boolean canHandle() {
        return true;
    }

    /**
     * Writes the given primitive value onto the supplied protobuf message builder.
     *
     * <p>When {@code field} is {@code null} the builder is returned untouched; otherwise the
     * value is parsed into the descriptor's primitive type before being set.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the Flink-side value to convert and set, or {@code null} to skip
     * @return the same {@code builder}, with the field set when a non-null value was provided
     */
    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        return field != null ? builder.setField(fieldDescriptor, transform(field)) : builder;
    }

    /**
     * Converts a value coming from a post processor into the descriptor's primitive Java type.
     *
     * @param field the raw value emitted by an upstream post processor
     * @return the parsed primitive value
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        return transform(field);
    }

    /**
     * Parses the raw value into the descriptor's primitive type using the matching
     * {@code PrimitiveHandler}.
     *
     * @param field the raw value to parse
     * @return the parsed primitive value
     * @throws InvalidDataTypeException if the value cannot be parsed into the field's expected type
     */
    private Object transform(Object field) {
        PrimitiveHandler primitiveHandler = PrimitiveHandlerFactory.getTypeHandler(fieldDescriptor);
        try {
            return primitiveHandler.parseObject(field);
        } catch (NumberFormatException e) {
            String errMessage = String.format("type mismatch of field: %s, expecting %s type, actual type %s", fieldDescriptor.getName(), fieldDescriptor.getType(), field.getClass());
            throw new InvalidDataTypeException(errMessage);
        }
    }

    /**
     * Returns the primitive value read from a protobuf message unchanged.
     *
     * <p>Primitive protobuf values already map directly onto Flink types, so no conversion
     * is required.
     *
     * @param field the value read from the protobuf message
     * @return the same {@code field} value
     */
    @Override
    public Object transformFromProto(Object field) {
        return field;
    }

    /**
     * Returns the primitive protobuf value unchanged, ignoring the descriptor cache.
     *
     * <p>The {@code cache} is accepted for interface compatibility but is not needed for
     * primitive fields, which require no nested descriptor lookups.
     *
     * @param field the value read from the protobuf message
     * @param cache the field descriptor cache, unused for primitive fields
     * @return the same {@code field} value
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return field;
    }

    /**
     * Reads the primitive value for this field from a Parquet {@code SimpleGroup}.
     *
     * @param simpleGroup the Parquet group holding the row being deserialized
     * @return the parsed primitive value, or the type's default when the field is absent
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        PrimitiveHandler primitiveHandler = PrimitiveHandlerFactory.getTypeHandler(fieldDescriptor);
        return primitiveHandler.parseSimpleGroup(simpleGroup);
    }

    /**
     * Returns the primitive value unchanged for JSON serialization.
     *
     * @param field the primitive value to emit
     * @return the same {@code field} value
     */
    @Override
    public Object transformToJson(Object field) {
        return field;
    }

    /**
     * Returns the Flink {@code TypeInformation} for this primitive field.
     *
     * @return the type information supplied by the matching {@code PrimitiveHandler}
     */
    @Override
    public TypeInformation getTypeInformation() {
        PrimitiveHandler primitiveHandler = PrimitiveHandlerFactory.getTypeHandler(fieldDescriptor);
        return primitiveHandler.getTypeInformation();
    }

}
