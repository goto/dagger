package com.gotocompany.dagger.core.processors;

import com.gotocompany.dagger.common.core.DaggerContext;
import com.gotocompany.dagger.core.processors.common.FetchOutputDecorator;
import com.gotocompany.dagger.core.processors.common.InitializationDecorator;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.types.PostProcessor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetrySubscriber;
import com.gotocompany.dagger.core.processors.external.ExternalMetricConfig;
import com.gotocompany.dagger.core.processors.external.ExternalPostProcessor;
import com.gotocompany.dagger.core.processors.internal.InternalPostProcessor;
import com.gotocompany.dagger.core.processors.transformers.TransformProcessor;
import com.gotocompany.dagger.core.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Parent post processor.
 */
public class ParentPostProcessor implements PostProcessor {
    /**
     * The parsed post processor configuration describing the external, internal and transform stages.
     */
    private final PostProcessorConfig postProcessorConfig;

    /**
     * The orchestrator used to obtain Stencil clients for resolving Protobuf descriptors.
     */
    private final StencilClientOrchestrator stencilClientOrchestrator;
    /**
     * The subscriber notified about telemetry emitted by the underlying post processors.
     */
    private TelemetrySubscriber telemetrySubscriber;

    /**
     * The Dagger context exposing the job {@link Configuration} and shared runtime wiring.
     */
    private final DaggerContext daggerContext;

    /**
     * Instantiates a new Parent post processor.
     *
     * @param daggerContext             the daggerContext
     * @param stencilClientOrchestrator the stencil client orchestrator
     * @param telemetrySubscriber       the telemetry subscriber
     */
    public ParentPostProcessor(DaggerContext daggerContext, StencilClientOrchestrator stencilClientOrchestrator, TelemetrySubscriber telemetrySubscriber) {
        this.daggerContext = daggerContext;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.telemetrySubscriber = telemetrySubscriber;
        this.postProcessorConfig = parsePostProcessorConfig(daggerContext.getConfiguration());
    }

    /**
     * Parses the post processor configuration from the job configuration.
     *
     * <p>Reads the raw JSON string stored under
     * {@code Constants.PROCESSOR_POSTPROCESSOR_CONFIG_KEY} (defaulting to an empty string when
     * absent) and converts it into a {@link PostProcessorConfig}.
     *
     * @param configuration the job configuration to read the raw config string from
     * @return the parsed post processor configuration
     */
    private static PostProcessorConfig parsePostProcessorConfig(Configuration configuration) {
        String postProcessorConfigString = configuration.getString(Constants.PROCESSOR_POSTPROCESSOR_CONFIG_KEY, "");
        return PostProcessorConfig.parse(postProcessorConfigString);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Applies the configured post processing pipeline to the given stream. When no post
     * processing is configured the input is returned unchanged. Otherwise the stream is first
     * initialized with an output {@link Row}, passed through the enabled external and internal
     * post processors, projected onto the configured output columns, and finally run through any
     * configured SQL transformers.
     *
     * @param streamInfo the incoming stream together with its column names
     * @return the resulting stream after all applicable post processing stages have run
     */
    @Override
    public StreamInfo process(StreamInfo streamInfo) {
        if (!canProcess(postProcessorConfig)) {
            return streamInfo;
        }
        DataStream<Row> resultStream = streamInfo.getDataStream();
        ColumnNameManager columnNameManager = new ColumnNameManager(streamInfo.getColumnNames(), postProcessorConfig.getOutputColumnNames());

        InitializationDecorator initializationDecorator = new InitializationDecorator(columnNameManager);
        resultStream = initializationDecorator.decorate(resultStream);
        streamInfo = new StreamInfo(resultStream, streamInfo.getColumnNames());
        SchemaConfig schemaConfig = new SchemaConfig(daggerContext.getConfiguration(), stencilClientOrchestrator, columnNameManager);

        List<PostProcessor> enabledPostProcessors = getEnabledPostProcessors(telemetrySubscriber, schemaConfig);
        for (PostProcessor postProcessor : enabledPostProcessors) {
            streamInfo = postProcessor.process(streamInfo);
        }

        FetchOutputDecorator fetchOutputDecorator = new FetchOutputDecorator(schemaConfig, postProcessorConfig.hasSQLTransformer());
        resultStream = fetchOutputDecorator.decorate(streamInfo.getDataStream());
        StreamInfo resultantStreamInfo = new StreamInfo(resultStream, columnNameManager.getOutputColumnNames());
        TransformProcessor transformProcessor = new TransformProcessor(postProcessorConfig.getTransformers(), daggerContext);
        if (transformProcessor.canProcess(postProcessorConfig)) {
            transformProcessor.notifySubscriber(telemetrySubscriber);
            resultantStreamInfo = transformProcessor.process(resultantStreamInfo);
        }
        return resultantStreamInfo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Determines whether this parent processor has any post processing work to perform.
     *
     * @param config the post processor configuration to evaluate
     * @return {@code true} when {@code config} is non-null and not empty, {@code false} otherwise
     */
    @Override
    public boolean canProcess(PostProcessorConfig config) {
        return config != null && !config.isEmpty();
    }

    /**
     * Builds the list of post processors that are enabled and applicable for the current config.
     *
     * <p>Returns an empty list when post processing is disabled via
     * {@code Constants.PROCESSOR_POSTPROCESSOR_ENABLE_KEY}. Otherwise it assembles the external and
     * internal post processors and keeps only those whose {@code canProcess} check passes for the
     * parsed configuration.
     *
     * @param subscriber   the telemetry subscriber forwarded to the external metric configuration
     * @param schemaConfig the schema configuration shared across the post processors
     * @return the post processors that are enabled and able to process the current configuration
     */
    private List<PostProcessor> getEnabledPostProcessors(TelemetrySubscriber subscriber, SchemaConfig schemaConfig) {
        if (!daggerContext.getConfiguration().getBoolean(Constants.PROCESSOR_POSTPROCESSOR_ENABLE_KEY, Constants.PROCESSOR_POSTPROCESSOR_ENABLE_DEFAULT)) {
            return new ArrayList<>();
        }

        ExternalMetricConfig externalMetricConfig = getExternalMetricConfig(daggerContext.getConfiguration(), subscriber);
        ArrayList<PostProcessor> processors = new ArrayList<>();
        processors.add(new ExternalPostProcessor(schemaConfig, postProcessorConfig.getExternalSource(), externalMetricConfig));
        processors.add(new InternalPostProcessor(postProcessorConfig, schemaConfig));
        return processors
                .stream()
                .filter(p -> p.canProcess(postProcessorConfig))
                .collect(Collectors.toList());
    }

    /**
     * Creates the external metric configuration used to instrument the external post processors.
     *
     * @param config     the job configuration carrying external metric settings
     * @param subscriber the telemetry subscriber that receives external telemetry
     * @return a new {@link ExternalMetricConfig} bound to the given config and subscriber
     */
    private ExternalMetricConfig getExternalMetricConfig(Configuration config, TelemetrySubscriber subscriber) {
        return new ExternalMetricConfig(config, subscriber);
    }
}
