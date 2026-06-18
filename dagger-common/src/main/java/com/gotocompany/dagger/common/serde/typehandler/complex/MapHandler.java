package com.gotocompany.dagger.common.serde.typehandler.complex;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeInformationFactory;
import com.gotocompany.dagger.common.serde.typehandler.repeated.RepeatedMessageHandler;
import com.gotocompany.dagger.common.serde.typehandler.RowFactory;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.types.Row;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation.checkFieldExistsAndIsInitialized;
import static com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation.checkIsLegacySimpleGroupMap;
import static com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation.checkIsStandardSimpleGroupMap;

/**
 * The type Map proto handler.
 */
public class MapHandler implements TypeHandler {

    /**
     * The protobuf {@code FieldDescriptor} of the map field being converted.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;
    /**
     * Delegate handler that treats the map's entries as a repeated key/value message.
     */
    private TypeHandler repeatedMessageHandler;

    /**
     * Instantiates a new Map proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public MapHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
        this.repeatedMessageHandler = new RepeatedMessageHandler(fieldDescriptor);
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is a protobuf {@code map} field
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.isMapField();
    }

    /**
     * Sets the map field on the builder by encoding its entries as repeated key/value messages.
     *
     * <p>A {@code Map} input is first turned into rows of {@code (key, value)} pairs; any other
     * input is passed straight to the underlying repeated-message handler. When the handler
     * cannot apply or {@code field} is {@code null}, the builder is returned unchanged.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the map (or pre-built rows) to encode, or {@code null} to skip
     * @return the same {@code builder}, with the map entries set when provided
     */
    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }
        if (field instanceof Map) {
            Map<?, ?> mapField = (Map<?, ?>) field;
            ArrayList<Row> rows = new ArrayList<>();
            for (Entry<?, ?> entry : mapField.entrySet()) {
                rows.add(Row.of(entry.getKey(), entry.getValue()));
            }
            return repeatedMessageHandler.transformToProtoBuilder(builder, rows.toArray());
        }
        return repeatedMessageHandler.transformToProtoBuilder(builder, field);
    }

    /**
     * Converts a post-processor value into an array of key/value {@code Row} entries.
     *
     * <p>For a {@code Map} input, each entry's key and value are converted with their own
     * handlers; a {@code List} input is delegated to the repeated-message handler. Any other
     * input (including {@code null}) yields an empty array.
     *
     * @param field the map or list value emitted by an upstream post processor
     * @return an array of two-field rows, one per map entry
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        ArrayList<Row> rows = new ArrayList<>();
        if (field == null) {
            return rows.toArray();
        }
        if (field instanceof Map) {
            Map<String, ?> mapField = (Map<String, ?>) field;
            for (Entry<String, ?> entry : mapField.entrySet()) {
                Descriptors.FieldDescriptor keyDescriptor = fieldDescriptor.getMessageType().findFieldByName("key");
                Descriptors.FieldDescriptor valueDescriptor = fieldDescriptor.getMessageType().findFieldByName("value");
                TypeHandler handler = TypeHandlerFactory.getTypeHandler(keyDescriptor);
                Object key = handler.transformFromPostProcessor(entry.getKey());
                Object value = TypeHandlerFactory.getTypeHandler(valueDescriptor).transformFromPostProcessor(entry.getValue());
                rows.add(Row.of(key, value));
            }
            return rows.toArray();
        }
        if (field instanceof List) {
            return repeatedMessageHandler.transformFromPostProcessor(field);
        }
        return rows.toArray();
    }

    /**
     * Converts the map entries read from a protobuf message into key/value rows.
     *
     * @param field the repeated map-entry value read from the message
     * @return an array of two-field rows, one per map entry
     */
    @Override
    public Object transformFromProto(Object field) {
        return repeatedMessageHandler.transformFromProto(field);
    }

    /**
     * Converts the protobuf map entries into key/value rows using the descriptor cache.
     *
     * @param field the repeated map-entry value read from the message
     * @param cache the field descriptor cache used to resolve nested field indices
     * @return an array of two-field rows, one per map entry
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return repeatedMessageHandler.transformFromProtoUsingCache(field, cache);
    }

    /**
     * Reads the map field from a Parquet {@code SimpleGroup} into key/value rows.
     *
     * <p>Both the legacy and the standard ({@code key_value}-wrapped) Parquet map encodings are
     * supported; an empty array is returned when the field is missing.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return an array of two-field rows, one per map entry, or an empty array when absent
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            if (checkIsLegacySimpleGroupMap(simpleGroup, fieldName)) {
                return transformLegacyMapFromSimpleGroup(simpleGroup, fieldName);
            } else if (checkIsStandardSimpleGroupMap(simpleGroup, fieldName)) {
                return transformStandardMapFromSimpleGroup(simpleGroup, fieldName);
            }
        }
        return new Row[0];
    }

    /**
     * Deserializes a legacy-encoded Parquet map, where entries are repeated directly on the field.
     *
     * @param simpleGroup the Parquet group containing the map field
     * @param fieldName   the name of the map field to read
     * @return the deserialized key/value rows
     */
    private Row[] transformLegacyMapFromSimpleGroup(SimpleGroup simpleGroup, String fieldName) {
        ArrayList<Row> deserializedRows = new ArrayList<>();
        int repetitionCount = simpleGroup.getFieldRepetitionCount(fieldName);
        Descriptors.Descriptor keyValueDescriptor = fieldDescriptor.getMessageType();
        for (int i = 0; i < repetitionCount; i++) {
            SimpleGroup keyValuePair = (SimpleGroup) simpleGroup.getGroup(fieldName, i);
            deserializedRows.add(RowFactory.createRow(keyValueDescriptor, keyValuePair));
        }
        return deserializedRows.toArray(new Row[]{});
    }

    /**
     * Deserializes a standard-encoded Parquet map, where entries are nested under a
     * {@code key_value} group.
     *
     * @param simpleGroup the Parquet group containing the map field
     * @param fieldName   the name of the map field to read
     * @return the deserialized key/value rows
     */
    private Row[] transformStandardMapFromSimpleGroup(SimpleGroup simpleGroup, String fieldName) {
        ArrayList<Row> deserializedRows = new ArrayList<>();
        final String innerFieldName = "key_value";
        SimpleGroup nestedMapGroup = (SimpleGroup) simpleGroup.getGroup(fieldName, 0);
        int repetitionCount = nestedMapGroup.getFieldRepetitionCount(innerFieldName);
        Descriptors.Descriptor keyValueDescriptor = fieldDescriptor.getMessageType();
        for (int i = 0; i < repetitionCount; i++) {
            SimpleGroup keyValuePair = (SimpleGroup) nestedMapGroup.getGroup(innerFieldName, i);
            deserializedRows.add(RowFactory.createRow(keyValueDescriptor, keyValuePair));
        }
        return deserializedRows.toArray(new Row[]{});
    }

    /**
     * Returns {@code null}, as map fields are not serialized to JSON by this handler.
     *
     * @param field the value that would be serialized
     * @return {@code null}, always
     */
    @Override
    public Object transformToJson(Object field) {
        return null;
    }

    /**
     * Returns the Flink {@code TypeInformation} used to represent this map field.
     *
     * @return an object-array type whose element is the key/value row type
     */
    @Override
    public TypeInformation getTypeInformation() {
        return Types.OBJECT_ARRAY(TypeInformationFactory.getRowType(fieldDescriptor.getMessageType()));
    }
}
