package com.gotocompany.dagger.common.serde.typehandler.complex;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.gotocompany.dagger.common.serde.typehandler.RowFactory;
import com.gotocompany.dagger.common.serde.typehandler.TypeInformationFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.formats.json.JsonRowSerializationSchema;
import org.apache.flink.types.Row;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.DynamicMessage.Builder;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.List;
import java.util.Map;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;

/**
 * The type Message proto handler.
 */
public class MessageHandler implements TypeHandler {
    /**
     * The protobuf {@code FieldDescriptor} of the nested message field being converted.
     */
    private FieldDescriptor fieldDescriptor;
    /**
     * Lazily created schema used to serialize the message row to JSON.
     */
    private JsonRowSerializationSchema jsonRowSerializationSchema;
    /**
     * The default (empty) instance of the message, used when a Parquet value is absent.
     */
    private DynamicMessage defaultMessageInstance;
    /**
     * The descriptor of the nested message type, cached for deserialization.
     */
    private Descriptors.Descriptor fieldMessageDescriptor;

    /**
     * Instantiates a new Message proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public MessageHandler(FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
        if (canHandle()) {
            this.defaultMessageInstance = DynamicMessage.getDefaultInstance(fieldDescriptor.getMessageType());
            this.fieldMessageDescriptor = fieldDescriptor.getMessageType();
        }
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is a message type other than {@code google.protobuf.Timestamp}
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == MESSAGE && !fieldDescriptor.getMessageType().getFullName().equals("google.protobuf.Timestamp");
    }

    /**
     * Builds the nested protobuf message from a Flink {@code Row} and sets it on the builder.
     *
     * <p>Each nested field present in the row is converted with its own handler before the
     * assembled message is attached. When the handler cannot apply or {@code field} is
     * {@code null}, the builder is returned unchanged.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the row holding the nested message's field values, or {@code null} to skip
     * @return the same {@code builder}, with the nested message set when provided
     */
    @Override
    public Builder transformToProtoBuilder(Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }

        Builder elementBuilder = DynamicMessage.newBuilder(fieldDescriptor.getMessageType());
        List<FieldDescriptor> nestedFieldDescriptors = fieldDescriptor.getMessageType().getFields();
        Row rowElement = (Row) field;

        for (FieldDescriptor nestedFieldDescriptor : nestedFieldDescriptors) {
            int index = nestedFieldDescriptor.getIndex();
            if (index < rowElement.getArity()) {
                TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(nestedFieldDescriptor);
                if (rowElement.getField(index) != null) {
                    typeHandler.transformToProtoBuilder(elementBuilder, rowElement.getField(index));
                }
            }
        }

        return builder.setField(fieldDescriptor, elementBuilder.build());
    }

    /**
     * Converts a post-processor map into a Flink {@code Row} for the nested message.
     *
     * @param field the nested message values as a map keyed by field name
     * @return the populated row representing the nested message
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        return RowFactory.createRow((Map<String, Object>) field, fieldDescriptor.getMessageType());
    }

    /**
     * Converts a nested protobuf message read from the parent into a Flink {@code Row}.
     *
     * @param field the nested {@code DynamicMessage} read from the parent message
     * @return the populated row representing the nested message
     */
    @Override
    public Object transformFromProto(Object field) {
        return RowFactory.createRow((DynamicMessage) field);
    }

    /**
     * Converts the nested protobuf message into a row using the descriptor cache.
     *
     * @param field the nested {@code DynamicMessage} read from the parent message
     * @param cache the field descriptor cache used to resolve nested field indices
     * @return the populated row representing the nested message
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return RowFactory.createRow((DynamicMessage) field, cache);
    }

    /**
     * Reads the nested message field from a Parquet {@code SimpleGroup} into a row.
     *
     * <p>When the field is missing, a row built from the message's default instance is returned.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return the populated row, or a default row when the field is absent
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            SimpleGroup nestedGroup = (SimpleGroup) simpleGroup.getGroup(fieldName, 0);
            return RowFactory.createRow(fieldMessageDescriptor, nestedGroup);
        }
        return RowFactory.createRow(defaultMessageInstance);
    }

    /**
     * Serializes the nested message row to its JSON string representation.
     *
     * <p>The JSON serialization schema is created lazily on first use.
     *
     * @param field the nested message row to serialize
     * @return the JSON string for the row
     */
    @Override
    public Object transformToJson(Object field) {
        if (jsonRowSerializationSchema == null) {
            jsonRowSerializationSchema = createJsonRowSchema();
        }
        return new String(jsonRowSerializationSchema.serialize((Row) field));
    }

    /**
     * Returns the Flink {@code TypeInformation} used to represent this nested message.
     *
     * @return the row type derived from the message descriptor
     */
    @Override
    public TypeInformation getTypeInformation() {
        return TypeInformationFactory.getRowType(fieldDescriptor.getMessageType());
    }

    /**
     * Builds the JSON row serialization schema for the nested message type.
     *
     * @return a schema configured with this message's row type information
     */
    private JsonRowSerializationSchema createJsonRowSchema() {
        return JsonRowSerializationSchema
                .builder()
                .withTypeInfo(getTypeInformation())
                .build();
    }
}
