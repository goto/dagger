package com.gotocompany.dagger.core.utils;

import com.google.protobuf.Descriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;

/**
 * Utility class that contains helper methods to get {@link Descriptors} {@link  Descriptors.FieldDescriptor}.
 */
public class DescriptorsUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorsUtil.class.getName());

    /**
     * Gets FieldDescriptor .
     *
     * @param descriptor the descriptor
     * @param columnName the columnName
     * @return the fieldDescriptor
     */
    public static Descriptors.FieldDescriptor getFieldDescriptor(Descriptors.Descriptor descriptor, String columnName) {
        if (descriptor == null || columnName == null) {
            return null;
        }
        String[] nestedFields = columnName.split("\\.");
        if (nestedFields.length == 1) {
            return descriptor.findFieldByName(columnName);
        } else {
            return getNestedFieldDescriptor(descriptor, nestedFields);
        }
    }

    /**
     * Gets FieldDescriptor .
     *
     * @param parentDescriptor  the descriptor
     * @param nestedColumnNames the array of columnNames
     * @return the fieldDescriptor
     */
    public static Descriptors.FieldDescriptor getNestedFieldDescriptor(Descriptors.Descriptor parentDescriptor, String[] nestedColumnNames) {
        if (parentDescriptor == null || nestedColumnNames == null || nestedColumnNames.length == 0) {
            return null;
        }
        String childColumnName = nestedColumnNames[0];
        if (nestedColumnNames.length == 1) {
            return parentDescriptor.findFieldByName(childColumnName);
        }
        Descriptors.FieldDescriptor childFieldDescriptor = parentDescriptor.findFieldByName(childColumnName);
        if (childFieldDescriptor == null || childFieldDescriptor.getJavaType() != MESSAGE) {
            LOGGER.info(String.format("Either the Field Descriptor for the field '%s' is missing in the proto '%s', or the Field Descriptor is not of the MESSAGE type.", childColumnName, parentDescriptor.getFullName()));
            return null;
        }
        Descriptors.Descriptor childDescriptor = childFieldDescriptor.getMessageType();
        return getNestedFieldDescriptor(childDescriptor, Arrays.copyOfRange(nestedColumnNames, 1, nestedColumnNames.length));
    }
}
