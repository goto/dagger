package com.gotocompany.dagger.core.processors.internal.processor;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.internal.processor.constant.ConstantInternalConfigProcessor;
import com.gotocompany.dagger.core.processors.internal.processor.function.FunctionInternalConfigProcessor;
import com.gotocompany.dagger.core.processors.internal.processor.invalid.InvalidInternalConfigProcessor;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlConfigTypePathParser;
import com.gotocompany.dagger.core.processors.internal.processor.sql.fields.SqlInternalConfigProcessor;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;

import java.util.Arrays;
import java.util.List;

/**
 * The factory class for Internal config handler.
 */
public class InternalConfigHandlerFactory {
    /**
     * Prevents instantiation of this static factory.
     *
     * @throws IllegalStateException always, since the class only exposes static helpers
     */
    private InternalConfigHandlerFactory() {
        throw new IllegalStateException("Factory class");
    }

    /**
     * Builds the ordered list of candidate internal config processors.
     *
     * <p>The handlers are evaluated in declaration order, so SQL handling takes precedence over
     * function handling, which in turn precedes constant handling.
     *
     * @param columnNameManager    resolves logical column names to row indices
     * @param sqlPathParser        extracts input data for SQL-type mappings
     * @param internalSourceConfig the internal source configuration being handled
     * @param schemaConfig         the schema/runtime context passed to function processors
     * @return the candidate processors to try, in priority order
     */
    private static List<InternalConfigProcessor> getHandlers(ColumnNameManager columnNameManager, SqlConfigTypePathParser sqlPathParser, InternalSourceConfig internalSourceConfig, SchemaConfig schemaConfig) {
        return Arrays.asList(new SqlInternalConfigProcessor(columnNameManager, sqlPathParser, internalSourceConfig),
                new FunctionInternalConfigProcessor(columnNameManager, internalSourceConfig, schemaConfig),
                new ConstantInternalConfigProcessor(columnNameManager, internalSourceConfig));
    }

    /**
     * Gets processor.
     *
     * @param internalSourceConfig the internal source config
     * @param columnNameManager    the column name manager
     * @param sqlPathParser        the sql path parser
     * @param schemaConfig         the schema configuration
     * @return the processor
     */
    public static InternalConfigProcessor getProcessor(InternalSourceConfig internalSourceConfig, ColumnNameManager columnNameManager, SqlConfigTypePathParser sqlPathParser, SchemaConfig schemaConfig) {
        return getHandlers(columnNameManager, sqlPathParser, internalSourceConfig, schemaConfig)
                .stream()
                .filter(customConfigProcessor -> customConfigProcessor.canProcess(internalSourceConfig.getType()))
                .findFirst()
                .orElse(new InvalidInternalConfigProcessor(internalSourceConfig));
    }
}
