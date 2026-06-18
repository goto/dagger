package com.gotocompany.dagger.core.processors.common;

import com.gotocompany.dagger.core.exception.InputOutputMappingException;

import org.apache.flink.types.Row;

import java.util.Objects;

/**
 * A class that responsible for managing input and output Row.
 */
public class RowManager {
    /**
     * Index of the input child row within the parent input/output row.
     */
    public static final int INPUT_ROW_INDEX = 0;
    /**
     * Index of the output child row within the parent input/output row.
     */
    public static final int OUTPUT_ROW_INDEX = 1;
    /**
     * The parent row holding the input row and output row as its two children.
     */
    private Row parentRow;

    /**
     * Instantiates a new Row manager.
     *
     * @param row the row
     */
    public RowManager(Row row) {
        this.parentRow = row;
    }

    /**
     * Instantiates a new Row manager with specified input row and output row size.
     *
     * @param inputRow      the input row
     * @param outputRowSize the output row size
     */
    public RowManager(Row inputRow, int outputRowSize) {
        Row inputOutputRow = new Row(2);
        Row outputRow = new Row(outputRowSize);
        inputOutputRow.setField(INPUT_ROW_INDEX, inputRow);
        inputOutputRow.setField(OUTPUT_ROW_INDEX, outputRow);
        this.parentRow = inputOutputRow;
    }

    /**
     * Set value in output row.
     *
     * @param fieldIndex the field index
     * @param value      the value
     */
    public void setInOutput(int fieldIndex, Object value) {
        getChildRow(OUTPUT_ROW_INDEX).setField(fieldIndex, value);
    }

    /**
     * Get value from input row.
     *
     * @param fieldIndex the field index
     * @return the from input
     */
    public Object getFromInput(int fieldIndex) {
        return getChildRow(INPUT_ROW_INDEX).getField(fieldIndex);
    }

    /**
     * Returns the input or output child row stored within the parent row.
     *
     * @param index the child index, either {@link #INPUT_ROW_INDEX} or {@link #OUTPUT_ROW_INDEX}
     * @return the child row at the given index
     * @throws InputOutputMappingException if the parent row does not have the expected arity of two
     */
    private Row getChildRow(int index) {
        if (parentRow.getArity() != 2) {
            throw new InputOutputMappingException("InputOutputRow does not contain output. Something went wrong. Row Arity: " + parentRow.getArity());
        }
        return (Row) parentRow.getField(index);
    }

    /**
     * Gets all row value from parent row.
     *
     * @return the all
     */
    public Row getAll() {
        return parentRow;
    }

    /**
     * Gets input data.
     *
     * @return the input data
     */
    public Row getInputData() {
        return getChildRow(INPUT_ROW_INDEX);
    }

    /**
     * Gets output data.
     *
     * @return the output data
     */
    public Row getOutputData() {
        return getChildRow(OUTPUT_ROW_INDEX);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Two row managers are equal when they wrap equal parent rows.
     *
     * @param o the object to compare with
     * @return {@code true} if {@code o} is a row manager wrapping an equal parent row,
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RowManager that = (RowManager) o;
        return Objects.equals(parentRow, that.parentRow);
    }

    /**
     * {@inheritDoc}
     *
     * @return a hash code derived from the wrapped parent row
     */
    @Override
    public int hashCode() {
        return Objects.hash(parentRow);
    }
}
