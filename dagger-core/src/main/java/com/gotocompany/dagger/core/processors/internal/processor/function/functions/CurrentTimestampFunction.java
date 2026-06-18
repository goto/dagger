package com.gotocompany.dagger.core.processors.internal.processor.function.functions;

import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.internal.processor.function.FunctionProcessor;

import java.sql.Timestamp;
import java.io.Serializable;
import java.time.Clock;

/**
 * Internal post-processor function that yields the current wall-clock time.
 *
 * <p>Selected when an internal source of type {@code function} has the value
 * {@code CURRENT_TIMESTAMP}. The injected {@link Clock} makes the produced value deterministic
 * in tests.
 */
public class CurrentTimestampFunction implements FunctionProcessor, Serializable {
    /** The configured function value that selects this function. */
    public static final String CURRENT_TIMESTAMP_FUNCTION_KEY = "CURRENT_TIMESTAMP";

    /** Clock used to read the current time; injected so the result can be controlled in tests. */
    private Clock clock;

    /**
     * Instantiates a new current timestamp function.
     *
     * @param clock the clock supplying the current time
     */
    public CurrentTimestampFunction(Clock clock) {
        this.clock = clock;
    }
    /**
     * Indicates whether this function handles the supplied function name.
     *
     * @param functionName the configured function value to match
     * @return {@code true} when {@code functionName} equals {@link #CURRENT_TIMESTAMP_FUNCTION_KEY}, {@code false} otherwise
     */
    @Override
    public boolean canProcess(String functionName) {
        return CURRENT_TIMESTAMP_FUNCTION_KEY.equals(functionName);
    }

    /**
     * Gets current time.
     *
     * @return the current time
     */
    @Override
    public Object getResult(RowManager rowManager) {
        return new Timestamp(clock.millis());
    }
}
