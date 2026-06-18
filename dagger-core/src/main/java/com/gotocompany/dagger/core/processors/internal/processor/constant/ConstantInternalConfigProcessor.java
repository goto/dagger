package com.gotocompany.dagger.core.processors.internal.processor.constant;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;
import com.gotocompany.dagger.core.processors.internal.processor.InternalConfigProcessor;

import java.io.Serializable;

/**
 * The Constant internal config processor.
 */
public class ConstantInternalConfigProcessor implements InternalConfigProcessor, Serializable {
    /** The configuration {@code type} value that selects this constant processor. */
    public static final String CONSTANT_CONFIG_HANDLER_TYPE = "constant";
    /** Resolves the configured output column name to its index in the output row. */
    private ColumnNameManager columnNameManager;
    /** The internal source configuration supplying the output field and constant value. */
    private InternalSourceConfig internalSourceConfig;

    /**
     * Instantiates a new Constant internal config processor.
     *
     * @param columnNameManager    the column name manager
     * @param internalSourceConfig the internal source config
     */
    public ConstantInternalConfigProcessor(ColumnNameManager columnNameManager, InternalSourceConfig internalSourceConfig) {
        this.columnNameManager = columnNameManager;
        this.internalSourceConfig = internalSourceConfig;
    }

    /**
     * Indicates whether this processor handles the supplied internal config type.
     *
     * @param type the configured internal source type
     * @return {@code true} when {@code type} equals {@link #CONSTANT_CONFIG_HANDLER_TYPE}, {@code false} otherwise
     */
    @Override
    public boolean canProcess(String type) {
        return CONSTANT_CONFIG_HANDLER_TYPE.equals(type);
    }

    /**
     * Writes the configured constant value into the resolved output column of the record.
     *
     * <p>When the configured output field cannot be resolved to a column index the record is left
     * unchanged.
     *
     * @param rowManager the row manager wrapping the record whose output row is updated
     */
    @Override
    public void process(RowManager rowManager) {
        int outputFieldIndex = columnNameManager.getOutputIndex(internalSourceConfig.getOutputField());
        if (outputFieldIndex != -1) {
            rowManager.setInOutput(outputFieldIndex, internalSourceConfig.getValue());
        }
    }
}
