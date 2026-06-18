package com.gotocompany.dagger.core.processors.internal;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.internal.processor.InternalConfigProcessor;
import com.gotocompany.dagger.core.processors.types.MapDecorator;

import org.apache.flink.types.Row;

/**
 * The decorator for Internal post processor.
 */
public class InternalDecorator implements MapDecorator {

    /**
     * The fixed index, within the wrapped Dagger record, of the output {@link Row} that the
     * internal post processor reads from and writes its resolved values back into.
     */
    public static final int OUTPUT_ROW_INDEX = 1;

    /** Configuration describing the single internal source/output mapping handled by this decorator. */
    private InternalSourceConfig internalSourceConfig;
    /** Strategy that resolves the configured value and writes it into the output row. */
    private InternalConfigProcessor internalConfigProcessor;
    /** Resolves logical column names to their input/output {@link Row} indices. */
    private ColumnNameManager columnNameManager;

    /**
     * Instantiates a new Internal decorator.
     *
     * @param internalSourceConfig    the internal source config
     * @param internalConfigProcessor the internal config processor
     * @param columnNameManager       the column name manager
     */
    public InternalDecorator(InternalSourceConfig internalSourceConfig, InternalConfigProcessor internalConfigProcessor, ColumnNameManager columnNameManager) {
        this.internalSourceConfig = internalSourceConfig;
        this.internalConfigProcessor = internalConfigProcessor;
        this.columnNameManager = columnNameManager;
    }

    /**
     * Determines whether this decorator should be applied to the stream.
     *
     * <p>Decoration is skipped when no {@link InternalSourceConfig} was supplied, allowing the
     * surrounding post processor to ignore unused internal mappings.
     *
     * @return {@code true} when an internal source config is present, {@code false} otherwise
     */
    @Override
    public Boolean canDecorate() {
        return internalSourceConfig != null;
    }

    /**
     * Applies the configured internal mapping to a single record and returns the enriched row.
     *
     * <p>When the existing output row (at index {@link #OUTPUT_ROW_INDEX}) no longer matches the
     * managed output arity it is replaced with a fresh {@link Row} sized to the configured output
     * columns. The mapping is then delegated to the {@link InternalConfigProcessor} through a
     * {@link RowManager}, which populates the output row in place.
     *
     * @param input the incoming record holding both the input and output rows
     * @return the record whose output row has been populated by the internal config processor
     */
    @Override
    public Row map(Row input) {
        Row outputRow = (Row) input.getField(OUTPUT_ROW_INDEX);
        if (outputColumnSizeIsDifferent(outputRow)) {
            input.setField(OUTPUT_ROW_INDEX, new Row(columnNameManager.getOutputSize()));
        }
        RowManager rowManager = new RowManager(input);
        internalConfigProcessor.process(rowManager);
        return rowManager.getAll();
    }

    /**
     * Checks whether an existing output row needs to be resized to match the managed columns.
     *
     * @param outputRow the current output row extracted from the record, possibly {@code null}
     * @return {@code true} when {@code outputRow} is non-null and its arity differs from the
     *         configured output size, {@code false} otherwise
     */
    private boolean outputColumnSizeIsDifferent(Row outputRow) {
        return outputRow != null && outputRow.getArity() != columnNameManager.getOutputSize();
    }
}
