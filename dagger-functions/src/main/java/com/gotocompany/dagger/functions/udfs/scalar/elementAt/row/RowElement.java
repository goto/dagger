package com.gotocompany.dagger.functions.udfs.scalar.elementAt.row;

import com.gotocompany.dagger.functions.udfs.scalar.elementAt.descriptor.CustomDescriptor;
import org.apache.flink.types.Row;

import java.util.Optional;

import static com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * The Row element.
 */
class RowElement extends Element {

    /**
     * Instantiates a new Row element.
     *
     * @param parent          the parent
     * @param row             the row
     * @param fieldDescriptor the field descriptor
     */
    RowElement(Element parent, Row row, FieldDescriptor fieldDescriptor) {
        super(parent, row, fieldDescriptor);
    }

    /**
     * Creates the next element in the path chain for the given child field of this message element.
     *
     * @param pathElement the name of the child field to descend into
     * @return an {@link Optional} containing the next element, or empty when the field is absent
     */
    public Optional<Element> createNext(String pathElement) {
        Optional<Element> childElement = initialize(this, null, new CustomDescriptor(getFieldDescriptor().getMessageType()), pathElement);
        return childElement;
    }
}
