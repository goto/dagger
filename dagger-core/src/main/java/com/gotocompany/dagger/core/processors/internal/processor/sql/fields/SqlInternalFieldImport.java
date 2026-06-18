package com.gotocompany.dagger.core.processors.internal.processor.sql.fields;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlInternalFieldConfig;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlConfigTypePathParser;

/**
 * The Sql internal field import.
 */
public class SqlInternalFieldImport implements SqlInternalFieldConfig {

    /** Resolves logical column names to their input/output row indices. */
    private ColumnNameManager columnNameManager;
    /** Extracts the configured input data (single field or whole input row) for the mapping. */
    private SqlConfigTypePathParser sqlPathParser;
    /** The internal source configuration supplying the input value and output field. */
    private InternalSourceConfig internalSourceConfig;

    /**
     * Instantiates a new Sql internal field import.
     *
     * @param columnNameManager    the column name manager
     * @param sqlPathParser        the sql path parser
     * @param internalSourceConfig the internal source config
     */
    public SqlInternalFieldImport(ColumnNameManager columnNameManager, SqlConfigTypePathParser sqlPathParser, InternalSourceConfig internalSourceConfig) {
        this.columnNameManager = columnNameManager;
        this.sqlPathParser = sqlPathParser;
        this.internalSourceConfig = internalSourceConfig;
    }

    /**
     * Resolves the configured input value and writes it into the mapped output column.
     *
     * <p>When the configured output field cannot be resolved to a column index the record is left
     * unchanged.
     *
     * @param rowManager the row manager wrapping the record whose output row is populated
     */
    @Override
    public void processInputColumns(RowManager rowManager) {
        int outputFieldIndex = columnNameManager.getOutputIndex(internalSourceConfig.getOutputField());
        if (outputFieldIndex != -1) {
            Object inputData = sqlPathParser.getData(rowManager);
            rowManager.setInOutput(outputFieldIndex, inputData);
        }
    }
}
