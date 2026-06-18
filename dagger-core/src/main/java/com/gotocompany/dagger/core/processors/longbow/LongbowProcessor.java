package com.gotocompany.dagger.core.processors.longbow;

import com.gotocompany.dagger.core.processors.PostProcessorConfig;
import com.gotocompany.dagger.core.processors.longbow.columnmodifier.ColumnModifier;
import com.gotocompany.dagger.core.processors.types.PostProcessor;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StreamInfo;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * The Longbow processor.
 */
public class LongbowProcessor implements PostProcessor {

    /**
     * The async processor used to attach each rich async function as an ordered-wait operator.
     */
    private AsyncProcessor asyncProcessor;
    /**
     * The Dagger configuration providing the Longbow async timeout and thread capacity.
     */
    private Configuration configuration;
    /**
     * The ordered list of Longbow rich async functions (writer and/or reader) applied to the stream.
     */
    private ArrayList<RichAsyncFunction<Row, Row>> longbowRichFunctions;
    /**
     * The column modifier that adjusts the output column names for the chosen Longbow type.
     */
    private ColumnModifier modifier;

    /**
     * Instantiates a new Longbow processor.
     *
     * @param asyncProcessor       the async processor
     * @param configuration        the configuration
     * @param longbowRichFunctions the longbow rich functions
     * @param modifier             the modifier
     */
    public LongbowProcessor(AsyncProcessor asyncProcessor, Configuration configuration, ArrayList<RichAsyncFunction<Row, Row>> longbowRichFunctions, ColumnModifier modifier) {
        this.asyncProcessor = asyncProcessor;
        this.configuration = configuration;
        this.longbowRichFunctions = longbowRichFunctions;
        this.modifier = modifier;
    }

    /**
     * Applies the configured Longbow rich async functions to the incoming stream in order.
     *
     * <p>Each function is wrapped in an ordered-wait async operator using the Longbow async timeout
     * and thread capacity from the configuration. The resulting stream is returned together with the
     * column names produced by the {@link ColumnModifier}.
     *
     * @param streamInfo the incoming stream and its column names
     * @return a new {@link StreamInfo} wrapping the Longbow-processed stream and modified column names
     */
    @Override
    public StreamInfo process(StreamInfo streamInfo) {
        DataStream<Row> inputStream = streamInfo.getDataStream();
        long longbowAsyncTimeout = configuration.getLong(Constants.PROCESSOR_LONGBOW_ASYNC_TIMEOUT_KEY, Constants.PROCESSOR_LONGBOW_ASYNC_TIMEOUT_DEFAULT);
        Integer longbowThreadCapacity = configuration.getInteger(Constants.PROCESSOR_LONGBOW_THREAD_CAPACITY_KEY, Constants.PROCESSOR_LONGBOW_THREAD_CAPACITY_DEFAULT);
        DataStream<Row> outputStream = inputStream;
        for (RichAsyncFunction<Row, Row> longbowRichFunction : longbowRichFunctions) {
            outputStream = asyncProcessor.orderedWait(outputStream, longbowRichFunction, longbowAsyncTimeout, TimeUnit.MILLISECONDS, longbowThreadCapacity);
        }
        return new StreamInfo(outputStream, modifier.modifyColumnNames(streamInfo.getColumnNames()));
    }

    /**
     * Indicates whether this post processor can handle the given configuration.
     *
     * <p>The Longbow processor is always constructed explicitly by the {@code LongbowFactory} rather
     * than selected from configuration, so this always returns {@code false}.
     *
     * @param postProcessorConfig the post processor configuration to test
     * @return {@code false}, as this processor is never chosen via configuration matching
     */
    @Override
    public boolean canProcess(PostProcessorConfig postProcessorConfig) {
        return false;
    }
}
