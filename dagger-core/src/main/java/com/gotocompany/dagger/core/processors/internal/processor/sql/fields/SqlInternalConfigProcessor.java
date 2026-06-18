package com.gotocompany.dagger.core.processors.internal.processor.sql.fields;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;
import com.gotocompany.dagger.core.processors.internal.processor.InternalConfigProcessor;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlConfigTypePathParser;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlInternalFieldConfig;

import java.io.Serializable;

/**
 * The Sql internal config processor.
 */
public class SqlInternalConfigProcessor implements InternalConfigProcessor, Serializable {

    /** The configuration {@code type} value that selects this SQL processor. */
    public static final String SQL_CONFIG_HANDLER_TYPE = "sql";

    /** Resolves logical column names to their input/output row indices. */
    private ColumnNameManager columnNameManager;
    /** Extracts the configured input data (single field or whole input row) for the mapping. */
    private SqlConfigTypePathParser sqlPathParser;
    /** The internal source configuration supplying the input value and output field. */
    private InternalSourceConfig internalSourceConfig;

    /**
     * Instantiates a new Sql internal config processor.
     *
     * @param columnNameManager    the column name manager
     * @param sqlPathParser        the sql path parser
     * @param internalSourceConfig the internal source config
     */
    public SqlInternalConfigProcessor(ColumnNameManager columnNameManager, SqlConfigTypePathParser sqlPathParser, InternalSourceConfig internalSourceConfig) {
        this.columnNameManager = columnNameManager;
        this.sqlPathParser = sqlPathParser;
        this.internalSourceConfig = internalSourceConfig;
    }

    /**
     * Indicates whether this processor handles the supplied internal config type.
     *
     * @param type the configured internal source type
     * @return {@code true} when {@code type} equals {@link #SQL_CONFIG_HANDLER_TYPE}, {@code false} otherwise
     */
    @Override
    public boolean canProcess(String type) {
        return SQL_CONFIG_HANDLER_TYPE.equals(type);
    }

    /**
     * Resolves and applies the appropriate SQL field mapping to the record.
     *
     * <p>A {@link SqlInternalFieldFactory} chooses between a select-all import and a single-field
     * import based on the configuration, and the selected {@link SqlInternalFieldConfig} populates
     * the output row.
     *
     * @param rowManager the row manager wrapping the record whose output row is populated
     */
    @Override
    public void process(RowManager rowManager) {
        SqlInternalFieldConfig sqlInternalFieldConfig =
                new SqlInternalFieldFactory(columnNameManager, sqlPathParser, internalSourceConfig).getSqlInternalFieldConfig();
        sqlInternalFieldConfig.processInputColumns(rowManager);
    }
}
