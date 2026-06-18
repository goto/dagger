package com.gotocompany.dagger.common.serde.typehandler.repeated;

import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.apache.parquet.example.data.simple.SimpleGroup;

/**
 * The type Repeated struct message proto handler.
 */
public class RepeatedStructMessageHandler implements TypeHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the repeated {@code google.protobuf.Struct} field handled here.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Repeated struct message proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public RepeatedStructMessageHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is a repeated {@code google.protobuf.Struct} message
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                && fieldDescriptor.toProto().getTypeName().equals(".google.protobuf.Struct") && fieldDescriptor.isRepeated();
    }

    /**
     * Returns the builder unchanged.
     *
     * <p>This handler does not currently serialize repeated {@code Struct} values into protobuf.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the value that would be set, ignored here
     * @return the supplied {@code builder}, unchanged
     */
    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        return builder;
    }

    /**
     * Returns {@code null}, as repeated {@code Struct} values are not produced from post-processor input.
     *
     * @param field the value supplied by an upstream post processor
     * @return {@code null}, always
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        return null;
    }

    /**
     * Returns {@code null}, as repeated {@code Struct} values are not read from protobuf by this handler.
     *
     * @param field the value read from the protobuf message
     * @return {@code null}, always
     */
    @Override
    public Object transformFromProto(Object field) {
        return null;
    }

    /**
     * Returns {@code null}, ignoring the descriptor cache.
     *
     * @param field the value read from the protobuf message
     * @param cache the field descriptor cache, unused here
     * @return {@code null}, always
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return null;
    }

    /**
     * Returns {@code null}, as repeated {@code Struct} values are not read from Parquet by this handler.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return {@code null}, always
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        return null;
    }

    /**
     * Returns {@code null}, as repeated {@code Struct} values are not emitted to JSON by this handler.
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
     * @return an object-array type of empty named rows
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.OBJECT_ARRAY(Types.ROW_NAMED(new String[]{}));
    }
}
