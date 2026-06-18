package com.gotocompany.dagger.core.processors.internal.processor.function;
import com.gotocompany.dagger.core.processors.common.RowManager;

/**
 * Strategy for computing the value of a {@code function}-typed internal post-processor mapping.
 *
 * <p>Implementations recognise a specific function name (such as {@code CURRENT_TIMESTAMP} or
 * {@code JSON_PAYLOAD}) and derive the value written into the output row from the record exposed
 * by the supplied {@link RowManager}.
 */
public interface FunctionProcessor {
    /**
     * Check if function can be processed.
     *
     * @param functionName the function name
     * @return the boolean
     */
    boolean canProcess(String functionName);

    /**
     * Process.
     *
     * @param rowManager the row manager
     */
    Object getResult(RowManager rowManager);
}
