package com.gotocompany.dagger.core.processors.common;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.types.MapDecorator;

import org.apache.flink.types.Row;

/**
 * The Initialization decorator.
 */
public class InitializationDecorator implements MapDecorator {

    /**
     * Manager that supplies the output column count used to size the combined output row.
     */
    private ColumnNameManager columnNameManager;

    /**
     * Instantiates a new Initialization decorator.
     *
     * @param columnNameManager the column name manager
     */
    public InitializationDecorator(ColumnNameManager columnNameManager) {
        this.columnNameManager = columnNameManager;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This decorator is always applied explicitly to the stream and therefore does not opt into
     * the generic decoration chain.
     *
     * @return {@code false} always
     */
    @Override
    public Boolean canDecorate() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the input {@link Row} into a combined record sized to hold both the input and the
     * configured number of output columns, ready for the downstream post processors to populate.
     *
     * @param input the original input row
     * @return the initialized combined input/output row
     */
    @Override
    public Row map(Row input) {
        RowManager rowManager = new RowManager(input, columnNameManager.getOutputSize());
        return rowManager.getAll();
    }


}
