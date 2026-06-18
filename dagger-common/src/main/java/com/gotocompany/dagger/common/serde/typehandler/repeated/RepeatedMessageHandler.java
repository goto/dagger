package com.gotocompany.dagger.common.serde.typehandler.repeated;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.gotocompany.dagger.common.serde.typehandler.RowFactory;
import com.gotocompany.dagger.common.serde.typehandler.TypeInformationFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.formats.json.JsonRowSerializationSchema;
import org.apache.flink.types.Row;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.DynamicMessage.Builder;
import net.minidev.json.JSONArray;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;

/**
 * The type Repeated message proto handler.
 */
public class RepeatedMessageHandler implements TypeHandler {
    /**
     * Lazily created schema used to serialize each message row to JSON.
     */
    private JsonRowSerializationSchema jsonRowSerializationSchema;
    /**
     * The protobuf {@code FieldDescriptor} of the repeated message field being converted.
     */
    private FieldDescriptor fieldDescriptor;
    /**
     * The descriptor of the repeated message's element type, cached for deserialization.
     */
    private Descriptors.Descriptor fieldMessageDescriptor;

    /**
     * Instantiates a new Repeated message proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public RepeatedMessageHandler(FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
        if (canHandle()) {
            this.fieldMessageDescriptor = fieldDescriptor.getMessageType();
        }
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is a repeated message type
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == MESSAGE && fieldDescriptor.isRepeated();
    }

    /**
     * Builds the repeated protobuf messages from Flink rows and sets them on the builder.
     *
     * <p>The input may be an {@code ArrayList} or an array of {@code Row}s; each row is converted
     * into a nested {@code DynamicMessage}. When the handler cannot apply or {@code field} is
     * {@code null}, the builder is returned unchanged.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the rows representing the repeated messages, or {@code null} to skip
     * @return the same {@code builder}, with the repeated messages set when provided
     */
    @Override
    public Builder transformToProtoBuilder(Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }

        ArrayList<DynamicMessage> messages = new ArrayList<>();
        List<FieldDescriptor> nestedFieldDescriptors = fieldDescriptor.getMessageType().getFields();

        if (field instanceof ArrayList) {
            ArrayList<Object> rowElements = (ArrayList<Object>) field;
            for (Object row : rowElements) {
                messages.add(getNestedDynamicMessage(nestedFieldDescriptors, (Row) row));
            }
        } else {
            Object[] rowElements = (Object[]) field;
            for (Object row : rowElements) {
                messages.add(getNestedDynamicMessage(nestedFieldDescriptors, (Row) row));
            }
        }
        return builder.setField(fieldDescriptor, messages);
    }

    /**
     * Converts a post-processor JSON array into an array of message {@code Row}s.
     *
     * @param field the {@code JSONArray} of nested message values, or {@code null}
     * @return an array of rows, one per nested message
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        ArrayList<Row> rows = new ArrayList<>();
        if (field != null) {
            Object[] inputFields = ((JSONArray) field).toArray();
            for (Object inputField : inputFields) {
                rows.add(RowFactory.createRow((Map<String, Object>) inputField, fieldDescriptor.getMessageType()));
            }
        }
        return rows.toArray();
    }

    /**
     * Converts the repeated messages read from a protobuf message into an array of rows.
     *
     * @param field the list of nested {@code DynamicMessage}s read from the parent, or {@code null}
     * @return an array of rows, one per nested message
     */
    @Override
    public Object transformFromProto(Object field) {
        ArrayList<Row> rows = new ArrayList<>();
        if (field != null) {
            List<DynamicMessage> protos = (List<DynamicMessage>) field;
            protos.forEach(proto -> rows.add(RowFactory.createRow(proto)));
        }
        return rows.toArray();
    }

    /**
     * Converts the repeated protobuf messages into rows using the descriptor cache.
     *
     * @param field the list of nested {@code DynamicMessage}s read from the parent, or {@code null}
     * @param cache the field descriptor cache used to resolve nested field indices
     * @return an array of rows, one per nested message
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        ArrayList<Row> rows = new ArrayList<>();
        if (field != null) {
            List<DynamicMessage> protos = (List<DynamicMessage>) field;
            protos.forEach(proto -> rows.add(RowFactory.createRow(proto, cache)));
        }
        return rows.toArray();
    }

    /**
     * Reads the repeated message field from a Parquet {@code SimpleGroup} into an array of rows.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return an array of rows, one per nested message, empty when the field is absent
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        ArrayList<Row> rowList = new ArrayList<>();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
            for (int i = 0; i < repetitionCount; i++) {
                SimpleGroup nestedGroup = (SimpleGroup) simpleGroup.getGroup(fieldName, i);
                rowList.add(RowFactory.createRow(fieldMessageDescriptor, nestedGroup));
            }
        }
        return rowList.toArray(new Row[]{});
    }

    /**
     * Serializes the repeated message rows into a string of JSON objects.
     *
     * <p>The JSON serialization schema is created lazily on first use.
     *
     * @param field the array of message rows to serialize
     * @return a string representation of the serialized JSON objects
     */
    @Override
    public Object transformToJson(Object field) {
        if (jsonRowSerializationSchema == null) {
            jsonRowSerializationSchema = createJsonRowSchema();
        }
        return Arrays.toString(Arrays.stream((Row[]) field)
                .map(row -> new String(jsonRowSerializationSchema.serialize(row)))
                .toArray(String[]::new));
    }

    /**
     * Returns the Flink {@code TypeInformation} used to represent this repeated message field.
     *
     * @return an object-array type whose element is the nested message's row type
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.OBJECT_ARRAY(TypeInformationFactory.getRowType(fieldDescriptor.getMessageType()));
    }

    /**
     * Builds a single nested {@code DynamicMessage} from a Flink {@code Row}.
     *
     * @param nestedFieldDescriptors the descriptors of the nested message's fields
     * @param row                    the row holding the nested message's field values
     * @return the assembled nested message
     */
    private DynamicMessage getNestedDynamicMessage(List<FieldDescriptor> nestedFieldDescriptors, Row row) {
        Builder elementBuilder = DynamicMessage.newBuilder(fieldDescriptor.getMessageType());
        handleNestedField(elementBuilder, nestedFieldDescriptors, row);
        return elementBuilder.build();
    }

    /**
     * Populates the element builder from a row by converting each present nested field.
     *
     * @param elementBuilder         the builder for the nested message being assembled
     * @param nestedFieldDescriptors the descriptors of the nested message's fields
     * @param row                    the row holding the nested message's field values
     */
    private void handleNestedField(Builder elementBuilder, List<FieldDescriptor> nestedFieldDescriptors, Row row) {
        for (FieldDescriptor nestedFieldDescriptor : nestedFieldDescriptors) {
            int index = nestedFieldDescriptor.getIndex();

            if (index < row.getArity()) {
                TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(nestedFieldDescriptor);
                typeHandler.transformToProtoBuilder(elementBuilder, row.getField(index));
            }
        }
    }

    /**
     * Builds the JSON row serialization schema for the nested message type.
     *
     * @return a schema configured with the nested message's row type information
     */
    private JsonRowSerializationSchema createJsonRowSchema() {
        return JsonRowSerializationSchema
                .builder()
                .withTypeInfo(TypeInformationFactory.getRowType(fieldDescriptor.getMessageType()))
                .build();
    }
}
