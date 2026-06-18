package com.gotocompany.dagger.core.processors.internal.processor.function;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;
import com.gotocompany.dagger.core.processors.internal.processor.InternalConfigProcessor;

import java.io.Serializable;

/**
 * The Function internal config processor.
 */
public class FunctionInternalConfigProcessor implements InternalConfigProcessor, Serializable {
    /** The configuration {@code type} value that selects this function processor. */
    public static final String FUNCTION_CONFIG_HANDLER_TYPE = "function";

    /** Resolves the configured output column name to its index in the output row. */
    private ColumnNameManager columnNameManager;
    /** The internal source configuration supplying the output field and function name. */
    private InternalSourceConfig internalSourceConfig;
    /** The concrete function selected for this mapping, resolved from the configured value. */
    protected FunctionProcessor functionProcessor;

    /**
     * Instantiates a new Function internal config processor.
     *
     * @param columnNameManager    the column name manager
     * @param internalSourceConfig the internal source config
     * @param schemaConfig         the schema config
     */
    public FunctionInternalConfigProcessor(ColumnNameManager columnNameManager, InternalSourceConfig internalSourceConfig, SchemaConfig schemaConfig) {
        this.columnNameManager = columnNameManager;
        this.internalSourceConfig = internalSourceConfig;
        this.functionProcessor = FunctionProcessorFactory.getFunctionProcessor(internalSourceConfig, schemaConfig);
    }

    /**
     * Indicates whether this processor handles the supplied internal config type.
     *
     * @param type the configured internal source type
     * @return {@code true} when {@code type} equals {@link #FUNCTION_CONFIG_HANDLER_TYPE}, {@code false} otherwise
     */
    @Override
    public boolean canProcess(String type) {
        return FUNCTION_CONFIG_HANDLER_TYPE.equals(type);
    }

    /**
     * Evaluates the configured function and writes its result into the resolved output column.
     *
     * <p>When the configured output field cannot be resolved to a column index the record is left
     * unchanged.
     *
     * @param rowManager the row manager wrapping the record read by the function and updated in place
     */
    @Override
    public void process(RowManager rowManager) {
        int outputFieldIndex = columnNameManager.getOutputIndex(internalSourceConfig.getOutputField());
        if (outputFieldIndex != -1) {
            rowManager.setInOutput(outputFieldIndex, functionProcessor.getResult(rowManager));
        }
    }
}
