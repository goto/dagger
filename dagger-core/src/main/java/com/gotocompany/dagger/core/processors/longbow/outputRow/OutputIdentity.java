package com.gotocompany.dagger.core.processors.longbow.outputRow;

import org.apache.flink.types.Row;

/**
 * The Output identity.
 */
public class OutputIdentity implements WriterOutputRow {
    /**
     * Returns the input row unchanged.
     *
     * <p>This identity implementation is selected when the Longbow writer should emit the original
     * record as-is, without appending any synchronizer metadata columns.
     *
     * @param input the row to pass through
     * @return the same {@code input} row, unmodified
     */
    @Override
    public Row get(Row input) {
        return input;
    }
}
