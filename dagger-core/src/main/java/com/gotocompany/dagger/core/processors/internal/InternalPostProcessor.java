package com.gotocompany.dagger.core.processors.internal;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.PostProcessorConfig;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.internal.processor.InternalConfigHandlerFactory;
import com.gotocompany.dagger.core.processors.internal.processor.InternalConfigProcessor;
import com.gotocompany.dagger.core.processors.internal.processor.sql.SqlConfigTypePathParser;
import com.gotocompany.dagger.core.processors.types.PostProcessor;
import com.gotocompany.dagger.core.processors.types.StreamDecorator;
import com.gotocompany.dagger.core.processors.types.Validator;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.core.StreamInfo;

/**
 * The Internal post processor.
 */
public class InternalPostProcessor implements PostProcessor {

    /** The full post processor configuration; supplies the internal source mappings and output columns. */
    private PostProcessorConfig postProcessorConfig;
    /** Schema and runtime context (stencil client, configuration) shared with the produced decorators. */
    private SchemaConfig schemaConfig;

    /**
     * Instantiates a new Internal post processor.
     *
     * @param postProcessorConfig the post processor config
     * @param schemaConfig        the schema config
     */
    public InternalPostProcessor(PostProcessorConfig postProcessorConfig, SchemaConfig schemaConfig) {
        this.postProcessorConfig = postProcessorConfig;
        this.schemaConfig = schemaConfig;
    }

    /**
     * Indicates whether this post processor is applicable to the supplied configuration.
     *
     * @param config the post processor configuration to inspect
     * @return {@code true} when the configuration declares an internal source, {@code false} otherwise
     */
    @Override
    public boolean canProcess(PostProcessorConfig config) {
        return config.hasInternalSource();
    }

    /**
     * Enriches the incoming stream by applying every configured internal source mapping in turn.
     *
     * <p>A {@link ColumnNameManager} is built from the stream's input columns and the configured
     * output columns. Each {@link InternalSourceConfig} then contributes an {@link InternalDecorator}
     * that is chained onto the stream, and the resulting {@link StreamInfo} carries the managed
     * output column names.
     *
     * @param streamInfo the upstream stream together with its current column names
     * @return a new {@link StreamInfo} wrapping the decorated stream and its output column names
     */
    @Override
    public StreamInfo process(StreamInfo streamInfo) {
        DataStream<Row> resultStream = streamInfo.getDataStream();
        ColumnNameManager columnNameManager = new ColumnNameManager(streamInfo.getColumnNames(), postProcessorConfig.getOutputColumnNames());

        for (InternalSourceConfig internalSourceConfig : postProcessorConfig.getInternalSource()) {
            resultStream = enrichStream(resultStream, internalSourceConfig, getInternalDecorator(internalSourceConfig, columnNameManager));
        }
        return new StreamInfo(resultStream, columnNameManager.getOutputColumnNames());
    }

    /**
     * Validates a single internal config and appends its decorator to the stream.
     *
     * @param resultStream the stream to enrich
     * @param configs      the validator (an {@link InternalSourceConfig}) whose fields are checked first
     * @param decorator    the decorator that appends the internal mapping onto the stream
     * @return the decorated stream
     */
    private DataStream<Row> enrichStream(DataStream<Row> resultStream, Validator configs, StreamDecorator decorator) {
        configs.validateFields();
        return decorator.decorate(resultStream);
    }

    /**
     * Gets internal decorator.
     *
     * @param internalSourceConfig the internal source config
     * @param columnNameManager    the column name manager
     * @return the internal decorator
     */
    protected StreamDecorator getInternalDecorator(InternalSourceConfig internalSourceConfig, ColumnNameManager columnNameManager) {
        SqlConfigTypePathParser sqlPathParser = new SqlConfigTypePathParser(internalSourceConfig, columnNameManager);
        InternalConfigProcessor processor = InternalConfigHandlerFactory
                .getProcessor(internalSourceConfig, columnNameManager, sqlPathParser, schemaConfig);
        return new InternalDecorator(internalSourceConfig, processor, columnNameManager);
    }
}
