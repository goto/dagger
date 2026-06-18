package com.gotocompany.dagger.core.processors.longbow.columnmodifier;

/**
 * The No op column modifier.
 */
public class NoOpColumnModifier implements ColumnModifier {
    /**
     * Returns the input column names unchanged.
     *
     * <p>Used by the combined Longbow process flow, which neither adds nor removes any columns.
     *
     * @param inputColumnNames the incoming column names
     * @return the same column names that were passed in
     */
    @Override
    public String[] modifyColumnNames(String[] inputColumnNames) {
        return inputColumnNames;
    }
}
