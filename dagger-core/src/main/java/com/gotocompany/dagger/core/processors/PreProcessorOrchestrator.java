package com.gotocompany.dagger.core.processors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.gotocompany.dagger.core.processors.common.ValidRecordsDecorator;
import com.jayway.jsonpath.InvalidJsonException;
import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.DaggerContext;
import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryTypes;
import com.gotocompany.dagger.core.processors.telemetry.processor.MetricsTelemetryExporter;
import com.gotocompany.dagger.core.processors.transformers.TransformProcessor;
import com.gotocompany.dagger.core.processors.types.Preprocessor;
import com.gotocompany.dagger.core.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * The Preprocessor orchestrator.
 */
public class PreProcessorOrchestrator implements Preprocessor {

    /**
     * The exporter that publishes preprocessor telemetry to the metrics subscriber.
     */
    private final MetricsTelemetryExporter metricsTelemetryExporter;
    /**
     * The parsed preprocessor configuration, or {@code null} when preprocessing is disabled.
     */
    private final PreProcessorConfig processorConfig;
    /**
     * The name of the table whose transformers this orchestrator applies.
     */
    private final String tableName;
    /**
     * The Dagger context exposing the job {@link Configuration} and shared runtime wiring.
     */
    private final DaggerContext daggerContext;
    /**
     * Shared Gson instance configured to map snake_case JSON keys onto the config fields.
     */
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    /**
     * Instantiates a new Preprocessor orchestrator.
     *
     * @param daggerContext            the daggerContext
     * @param metricsTelemetryExporter the metrics telemetry exporter
     * @param tableName                the table name
     */
    public PreProcessorOrchestrator(DaggerContext daggerContext, MetricsTelemetryExporter metricsTelemetryExporter, String tableName) {
        this.daggerContext = daggerContext;
        this.processorConfig = parseConfig(daggerContext.getConfiguration());
        this.metricsTelemetryExporter = metricsTelemetryExporter;
        this.tableName = tableName;
    }

    /**
     * Parse config preprocessor config.
     *
     * @param configuration the configuration
     * @return the preprocessor config
     */
    public PreProcessorConfig parseConfig(Configuration configuration) {
        if (!configuration.getBoolean(Constants.PROCESSOR_PREPROCESSOR_ENABLE_KEY, Constants.PROCESSOR_PREPROCESSOR_ENABLE_DEFAULT)) {
            return null;
        }
        String configJson = configuration.getString(Constants.PROCESSOR_PREPROCESSOR_CONFIG_KEY, "");
        PreProcessorConfig config;
        try {
            config = GSON.fromJson(configJson, PreProcessorConfig.class);
        } catch (JsonSyntaxException exception) {
            throw new InvalidJsonException("Invalid JSON Given for " + Constants.PROCESSOR_PREPROCESSOR_CONFIG_KEY);
        }
        return config;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Runs each enabled preprocessor over the stream in turn and then wraps the result in a
     * {@link ValidRecordsDecorator} so that only valid records are forwarded downstream.
     *
     * @param streamInfo the incoming stream together with its column names
     * @return the resulting stream after all preprocessors and the valid-records filter have run
     */
    @Override
    public StreamInfo process(StreamInfo streamInfo) {
        for (Preprocessor processor : getProcessors()) {
            streamInfo = processor.process(streamInfo);
        }
        return new StreamInfo(
                new ValidRecordsDecorator(tableName, streamInfo.getColumnNames(), daggerContext.getConfiguration())
                        .decorate(streamInfo.getDataStream()),
                streamInfo.getColumnNames());
    }

    /**
     * Gets processors.
     *
     * @return the processors
     */
    protected List<Preprocessor> getProcessors() {
        List<Preprocessor> preprocessors = new ArrayList<>();
        if (canProcess(processorConfig)) {
            processorConfig
                    .getTableTransformers()
                    .stream()
                    .filter(x -> x.getTableName().equals(this.tableName))
                    .forEach(elem -> {
                        TransformProcessor processor = new TransformProcessor(
                                elem.getTableName(),
                                TelemetryTypes.PRE_PROCESSOR_TYPE,
                                elem.getTransformers(),
                                daggerContext);
                        processor.notifySubscriber(metricsTelemetryExporter);
                        preprocessors.add(processor);
                    });
        }
        return preprocessors;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Determines whether this orchestrator has any preprocessing work to perform.
     *
     * @param config the preprocessor configuration to evaluate
     * @return {@code true} when {@code config} is non-null and not empty, {@code false} otherwise
     */
    @Override
    public boolean canProcess(PreProcessorConfig config) {
        return config != null && !config.isEmpty();
    }

}
