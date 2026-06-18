package com.gotocompany.dagger.core.processors.internal.processor.sql.fields;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlInternalFieldConfig;

/**
 * The Sql internal auto field import.
 * used to get all the input columns and values to the output of internal post processor.
 */
public class SqlInternalAutoFieldImport implements SqlInternalFieldConfig {

    /** Resolves logical column names to their input/output row indices. */
    private ColumnNameManager columnNameManager;

    /**
     * Instantiates a new Sql internal auto field import.
     *
     * @param columnNameManager the column name manager
     */
    public SqlInternalAutoFieldImport(ColumnNameManager columnNameManager) {
        this.columnNameManager = columnNameManager;
    }

    /**
     * Copies every input column straight through to the matching output column.
     *
     * <p>For each managed input column the value is read from the input row and written into the
     * output column carrying the same name, implementing the SQL select-all behaviour.
     *
     * @param rowManager the row manager wrapping the record whose output row is populated
     */
    @Override
    public void processInputColumns(RowManager rowManager) {
        for (String columnName : columnNameManager.getInputColumnNames()) {
            int inputFieldIndex = columnNameManager.getInputIndex(columnName);
            rowManager.setInOutput(columnNameManager.getOutputIndex(columnName), rowManager.getFromInput(inputFieldIndex));
        }
    }
}
