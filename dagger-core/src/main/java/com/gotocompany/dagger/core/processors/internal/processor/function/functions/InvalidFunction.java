package com.gotocompany.dagger.core.processors.internal.processor.function.functions;

import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.internal.processor.function.FunctionProcessor;

import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;

import java.io.Serializable;
/**
 * Fallback {@link FunctionProcessor} used when a configured function name is not recognised.
 *
 * <p>It never matches a function name and always fails fast on evaluation, surfacing the offending
 * configuration through an {@link InvalidConfigurationException}.
 */
public class InvalidFunction implements FunctionProcessor, Serializable {
    /** The internal source configuration whose unsupported function value triggered this fallback. */
    private InternalSourceConfig internalSourceConfig;

    /**
     * Instantiates a new Invalid internal function processor.
     *
     * @param internalSourceConfig the internal source config
     */
    public InvalidFunction(InternalSourceConfig internalSourceConfig) {
        this.internalSourceConfig = internalSourceConfig;
    }

    /**
     * Always reports that no function can be processed.
     *
     * @param functionName the configured function value (ignored)
     * @return {@code false} always
     */
    @Override
    public boolean canProcess(String functionName) {
        return false;
    }

    /**
     * Always fails because the configured function is unsupported.
     *
     * @param rowManager the row manager wrapping the current record (unused)
     * @return never returns normally
     * @throws InvalidConfigurationException always, naming the unsupported function
     */
    public Object getResult(RowManager rowManager) {
        String functionName = "";
        if (internalSourceConfig != null) {
            functionName = internalSourceConfig.getValue();
        }
        throw new InvalidConfigurationException(String.format("The function \"%s\" is not supported in custom configuration", functionName));
    }
}
