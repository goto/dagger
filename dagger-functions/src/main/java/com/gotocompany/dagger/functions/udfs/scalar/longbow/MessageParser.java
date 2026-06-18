package com.gotocompany.dagger.functions.udfs.scalar.longbow;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.gotocompany.dagger.functions.exceptions.LongbowException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Message parser.
 */
public class MessageParser implements Serializable {

    /**
     * The converter used to transform protobuf messages into Flink {@code Row} values.
     */
    private ProtoToRow protoToRow;

    /**
     * Instantiates a new Message parser.
     */
    public MessageParser() {
        this.protoToRow = new ProtoToRow();
    }

    /**
     * Read object.
     *
     * @param dynamicMessage the dynamic message
     * @param keys           the keys
     * @return the object
     */
    public Object read(DynamicMessage dynamicMessage, List<String> keys) {
        Descriptors.Descriptor parentDescriptor = dynamicMessage.getDescriptorForType();
        String key = keys.get(0);
        Descriptors.FieldDescriptor fieldByName = getFieldByName(key, parentDescriptor);
        if (keys.size() == 1) {
            Object resultField = dynamicMessage.getField(fieldByName);
            return parseSingleRow(fieldByName, resultField);
        } else {
            ArrayList<String> remainingKeys = new ArrayList<>(keys.subList(1, keys.size()));
            return read((DynamicMessage) dynamicMessage.getField(fieldByName), remainingKeys);
        }
    }

    /**
     * Converts a single resolved protobuf field value into its Flink-compatible representation.
     *
     * <p>Message fields are converted to {@code Row} values, enum fields to their string form (single
     * or repeated), and repeated string fields to string arrays; all other values are returned as-is.
     *
     * @param fieldByName the descriptor of the field being parsed
     * @param resultField the raw field value extracted from the message
     * @return the converted value suitable for use in a Flink {@code Row}
     */
    private Object parseSingleRow(Descriptors.FieldDescriptor fieldByName, Object resultField) {
        if (fieldByName.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            return protoToRow.getRow((DynamicMessage) resultField);
        } else if (fieldByName.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
            if (fieldByName.isRepeated()) {
                return protoToRow.getStringRow(((List) resultField));
            } else {
                return resultField.toString();
            }
        } else if (fieldByName.isRepeated()) {
            if (fieldByName.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
                return protoToRow.getRowForStringList((List<String>) resultField);
            }
        }
        return resultField;
    }

    /**
     * Resolves the protobuf field descriptor for the given key within the supplied parent descriptor.
     *
     * @param key              the field name to look up
     * @param parentDescriptor the descriptor of the message containing the field
     * @return the matching field descriptor
     * @throws LongbowException if no field with the given key exists in the parent message
     */
    private Descriptors.FieldDescriptor getFieldByName(String key, Descriptors.Descriptor parentDescriptor) {
        Descriptors.FieldDescriptor fieldByName = parentDescriptor.findFieldByName(key);
        if (fieldByName == null) {
            throw new LongbowException("Key : " + key + " does not exist in Message " + parentDescriptor.getFullName());
        }
        return fieldByName;
    }
}
