package com.gotocompany.dagger.core.processors.transformers;

import com.gotocompany.dagger.common.core.DaggerContext;
import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.core.Transformer;
import com.gotocompany.dagger.core.exception.TransformClassNotDefinedException;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryPublisher;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryTypes;
import com.gotocompany.dagger.core.processors.PostProcessorConfig;
import com.gotocompany.dagger.core.processors.PreProcessorConfig;
import com.gotocompany.dagger.core.processors.types.PostProcessor;
import com.gotocompany.dagger.core.processors.types.Preprocessor;
import com.gotocompany.dagger.core.utils.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Transformer processor.
 */
public class TransformProcessor implements Preprocessor, PostProcessor, TelemetryPublisher {
    /**
     * The ordered list of {@link TransformConfig} entries this processor applies to the stream.
     */
    protected final List<TransformConfig> transformConfigs;


    /**
     * Gets table name.
     *
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * The name of the input table this processor is bound to, or {@code "NULL"} when unscoped.
     */
    protected final String tableName;
    /**
     * The telemetry metrics gathered by this processor, keyed by metric type.
     */
    private final Map<String, List<String>> metrics = new HashMap<>();
    /**
     * The telemetry classification (pre-processor or post-processor) of this processor.
     */
    protected final TelemetryTypes type;
    /**
     * The Dagger context providing access to job configuration and the Flink runtime.
     */
    private final DaggerContext daggerContext;

    /**
     * Instantiates a new Transform processor.
     *
     * @param transformConfigs the transform configs
     * @param daggerContext    the daggerContext
     */
    public TransformProcessor(List<TransformConfig> transformConfigs, DaggerContext daggerContext) {
        this("NULL", TelemetryTypes.POST_PROCESSOR_TYPE, transformConfigs, daggerContext);
    }

    /**
     * Instantiates a new Transform processor with specified table name and telemetry types.
     *
     * @param tableName     the table name
     * @param type          the type
     * @param daggerContext the configuration
     */
    public TransformProcessor(String tableName, TelemetryTypes type, List<TransformConfig> transformConfigs, DaggerContext daggerContext) {
        this.transformConfigs = transformConfigs == null ? new ArrayList<>() : transformConfigs;
        this.tableName = tableName;
        this.type = type;
        this.daggerContext = daggerContext;
        TransformerUtils.populateDefaultArguments(this);
    }

    /**
     * Applies every configured transformer to the given stream in order and returns the result.
     *
     * <p>For each {@link TransformConfig} the declared {@link Transformer} implementation is loaded
     * reflectively and its {@code transform} method is invoked, chaining the output of one
     * transformer into the input of the next.
     *
     * @param streamInfo the stream metadata and data stream to transform
     * @return the resulting {@link StreamInfo} after all transformers have been applied
     * @throws TransformClassNotDefinedException if a configured transformer class cannot be loaded
     *                                           or instantiated
     */
    @Override
    public StreamInfo process(StreamInfo streamInfo) {
        for (TransformConfig transformConfig : transformConfigs) {
            String className = transformConfig.getTransformationClass();
            try {
                Transformer function = getTransformMethod(transformConfig, className, streamInfo.getColumnNames());
                streamInfo = function.transform(streamInfo);
            } catch (ReflectiveOperationException e) {
                throw new TransformClassNotDefinedException(e.getMessage());
            }
        }
        return streamInfo;
    }

    /**
     * Determines whether this processor should run for the given pre-processor configuration.
     *
     * @param processorConfig the pre-processor configuration to inspect
     * @return {@code true} if the configuration declares a table transformer whose table name
     *         matches this processor's table name, {@code false} otherwise
     */
    @Override
    public boolean canProcess(PreProcessorConfig processorConfig) {
        return processorConfig.getTableTransformers().stream().anyMatch(x -> x.tableName.equals(this.tableName));
    }

    /**
     * Determines whether this processor should run for the given post-processor configuration.
     *
     * @param processorConfig the post-processor configuration to inspect
     * @return {@code true} if the configuration declares any transform configs, {@code false}
     *         otherwise
     */
    @Override
    public boolean canProcess(PostProcessorConfig processorConfig) {
        return processorConfig.hasTransformConfigs();
    }

    /**
     * Registers this processor's telemetry metric before telemetry subscribers are notified.
     *
     * <p>The metric key recorded depends on the configured {@link TelemetryTypes}: post-processor
     * usage is tracked under the generic transform-processor key, while pre-processor usage is
     * tracked under a table-scoped key. Other telemetry types record nothing.
     */
    @Override
    public void preProcessBeforeNotifyingSubscriber() {
        switch (this.type) {
            case POST_PROCESSOR_TYPE:
                addMetric(type.getValue(), Constants.TRANSFORM_PROCESSOR_KEY);
                break;
            case PRE_PROCESSOR_TYPE:
                addMetric(type.getValue(), this.tableName + "_" + Constants.TRANSFORM_PROCESSOR_KEY);
                break;
            default:
                break;
        }
    }

    /**
     * Returns the telemetry metrics gathered by this processor.
     *
     * @return a map of metric type to the list of recorded metric values
     */
    @Override
    public Map<String, List<String>> getTelemetry() {
        return metrics;
    }

    /**
     * Records a single telemetry metric value under the given key, creating the list if needed.
     *
     * @param key   the metric type key to record under
     * @param value the metric value to append
     */
    private void addMetric(String key, String value) {
        metrics.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    /**
     * Gets transform method.
     *
     * @param transformConfig the transform config
     * @param className       the class name
     * @param columnNames     the column names
     * @return the transform method
     * @throws ClassNotFoundException    the class not found exception
     * @throws NoSuchMethodException     the no such method exception
     * @throws InstantiationException    the instantiation exception
     * @throws IllegalAccessException    the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     */
    protected Transformer getTransformMethod(TransformConfig transformConfig, String className, String[] columnNames) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> transformerClass = Class.forName(className);
        Constructor transformerClassConstructor = transformerClass.getConstructor(Map.class, String[].class, DaggerContext.class);
        return (Transformer) transformerClassConstructor.newInstance(transformConfig.getTransformationArguments(), columnNames, daggerContext);
    }
}
