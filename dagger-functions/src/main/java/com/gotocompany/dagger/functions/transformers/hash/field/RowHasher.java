package com.gotocompany.dagger.functions.transformers.hash.field;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.functions.transformers.hash.FieldHasherFactory;
import org.apache.flink.types.Row;

import java.util.Arrays;

/**
 * The Row hasher.
 */
public class RowHasher implements FieldHasher {

    /**
     * Dot-separated path segments from this row down to the nested field that must be masked.
     */
    private String[] splittedFieldPath;
    /**
     * Hasher responsible for masking the next segment of the field path within this row.
     */
    private FieldHasher child;
    /**
     * Index, within this row, of the field handled by the child hasher.
     */
    private int childIndex;

    /**
     * Instantiates a new Row hasher.
     *
     * @param splittedFieldPath the splitted field path
     */
    public RowHasher(String[] splittedFieldPath) {
        this.splittedFieldPath = splittedFieldPath;
    }

    /**
     * Instantiates a new Row hasher with specified child index.
     *
     * @param childIndex the child index
     * @param child      the child
     */
    public RowHasher(int childIndex, FieldHasher child) {
        this.child = child;
        this.childIndex = childIndex;
    }

    /**
     * Masks the nested field within the given row by delegating to the child hasher.
     *
     * <p>Replaces the field at the child index of the row with the value produced by the child hasher.
     *
     * @param elem the {@link Row} containing the field to mask
     * @return the same row with its nested field masked
     */
    @Override
    public Object maskRow(Object elem) {
        Row currentRow = (Row) elem;
        currentRow.setField(childIndex, child.maskRow(currentRow.getField(this.childIndex)));
        return currentRow;
    }

    /**
     * Determines whether this hasher can descend into the given field.
     *
     * <p>Returns {@code true} only for a multi-segment path that points to a valid, non-repeated field
     * of protobuf message type.
     *
     * @param fieldDescriptor the descriptor of the field to be masked
     * @return {@code true} if this hasher can process the field, {@code false} otherwise
     */
    @Override
    public boolean canProcess(Descriptors.FieldDescriptor fieldDescriptor) {
        return splittedFieldPath.length > 1
                && isValidNonRepeatedField(fieldDescriptor)
                && fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE;
    }

    /**
     * Lazily creates and stores the child hasher for the next path segment when not already set.
     *
     * @param fieldDescriptor the descriptor of the message field this row hasher handles
     * @return this hasher
     */
    @Override
    public FieldHasher setChild(Descriptors.FieldDescriptor fieldDescriptor) {
        if (child == null) {
            this.child = createChild(fieldDescriptor);
        }
        return this;
    }

    /**
     * Builds the child hasher for the remaining field path beneath this row.
     *
     * <p>Strips the first path segment, resolves the descriptor of the next field within the message
     * type, records its index and creates the appropriate child hasher via {@code FieldHasherFactory}.
     *
     * @param fieldDescriptor the descriptor of the message field this row hasher handles
     * @return the child hasher for the next segment of the field path
     */
    private FieldHasher createChild(Descriptors.FieldDescriptor fieldDescriptor) {
        String[] childColumnPath = Arrays.copyOfRange(splittedFieldPath, 1, splittedFieldPath.length);
        String childField = childColumnPath[0];
        Descriptors.FieldDescriptor childFieldDescriptor = fieldDescriptor.getMessageType().findFieldByName(childField);
        FieldHasher childHasher = FieldHasherFactory.createChildHasher(childColumnPath, childFieldDescriptor);
        childIndex = childFieldDescriptor.getIndex();
        return childHasher;
    }
}
