package com.gotocompany.dagger.core.processors.internal.processor.function;

import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;
import com.gotocompany.dagger.core.processors.internal.processor.function.functions.CurrentTimestampFunction;
import com.gotocompany.dagger.core.processors.internal.processor.function.functions.JsonPayloadFunction;
import com.gotocompany.dagger.core.processors.internal.processor.function.functions.InvalidFunction;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

/**
 * The factory class for internal function post processors.
 */
public class FunctionProcessorFactory {
    /**
     * Prevents instantiation of this static factory.
     *
     * @throws IllegalStateException always, since the class only exposes static helpers
     */
    private FunctionProcessorFactory() {
        throw new IllegalStateException("Factory class");
    }

    /**
     * Builds the ordered list of candidate function processors.
     *
     * <p>Functions are evaluated in declaration order; the system-default-zone {@link Clock} backs
     * the {@link CurrentTimestampFunction}.
     *
     * @param internalSourceConfig the internal source configuration being handled
     * @param schemaConfig         the schema/runtime context passed to functions that need it
     * @return the candidate function processors to try, in priority order
     */
    private static List<FunctionProcessor> getFunctions(InternalSourceConfig internalSourceConfig, SchemaConfig schemaConfig) {
        Clock clock = Clock.systemDefaultZone();
        return Arrays.asList(new CurrentTimestampFunction(clock),
                new JsonPayloadFunction(internalSourceConfig, schemaConfig));
    }

    /**
     * Gets function for post-processing.
     *
     * @param internalSourceConfig the internal source config
     * @param schemaConfig         the schema config
     *
     * @return the function processor
     */
    public static FunctionProcessor getFunctionProcessor(InternalSourceConfig internalSourceConfig, SchemaConfig schemaConfig) {
        return getFunctions(internalSourceConfig, schemaConfig)
                .stream()
                .filter(functionProcessor -> functionProcessor.canProcess(internalSourceConfig.getValue()))
                .findFirst()
                .orElse(new InvalidFunction(internalSourceConfig));
    }
}
