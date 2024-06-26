package com.gotocompany.dagger.core.processors.internal.processor.function;
import com.gotocompany.dagger.core.processors.common.RowManager;

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
