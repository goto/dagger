package com.gotocompany.dagger.core.processors.internal.processor.sql.fields;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlConfigTypePathParser;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlInternalFieldConfig;

/**
 * The factory class for Sql internal field processor.
 */
public class SqlInternalFieldFactory {
    /** Resolves logical column names to their input/output row indices. */
    private ColumnNameManager columnNameManager;
    /** Extracts the configured input data for single-field SQL mappings. */
    private SqlConfigTypePathParser sqlPathParser;
    /** The internal source configuration supplying the output field and select-all marker. */
    private InternalSourceConfig internalSourceConfig;

    /**
     * Instantiates a new Sql internal field factory.
     *
     * @param columnNameManager    the column name manager
     * @param sqlPathParser        the sql path parser
     * @param internalSourceConfig the internal source config
     */
    public SqlInternalFieldFactory(ColumnNameManager columnNameManager, SqlConfigTypePathParser sqlPathParser, InternalSourceConfig internalSourceConfig) {
        this.columnNameManager = columnNameManager;
        this.sqlPathParser = sqlPathParser;
        this.internalSourceConfig = internalSourceConfig;
    }

    /**
     * Gets sql internal field config.
     *
     * @return the sql internal field config
     */
    public SqlInternalFieldConfig getSqlInternalFieldConfig() {
        if (selectAllFromInputColumns()) {
            return new SqlInternalAutoFieldImport(columnNameManager);
        } else {
            return new SqlInternalFieldImport(columnNameManager, sqlPathParser, internalSourceConfig);
        }
    }

    /**
     * Determines whether the configuration requests importing all input columns.
     *
     * @return {@code true} when the configured output field is the SQL select-all marker, {@code false} otherwise
     */
    private boolean selectAllFromInputColumns() {
        return Constants.SQL_PATH_SELECT_ALL_CONFIG_VALUE.equals(internalSourceConfig.getOutputField());
    }
}
