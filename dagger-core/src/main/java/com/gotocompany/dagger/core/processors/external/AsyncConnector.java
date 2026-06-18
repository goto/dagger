package com.gotocompany.dagger.core.processors.external;

import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import com.gotocompany.dagger.core.metrics.aspects.ExternalSourceAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporterFactory;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryPublisher;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryTypes;
import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.DescriptorManager;
import com.gotocompany.dagger.core.processors.common.EndpointHandler;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.types.SourceConfig;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.types.Row;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.common.exceptions.DescriptorNotFoundException;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.singleton;

/**
 * The Async connector.
 */
public abstract class AsyncConnector extends RichAsyncFunction<Row, Row> implements TelemetryPublisher {
    /**
     * The identifier of the external source type (for example {@code ES}, {@code HTTP}, {@code GRPC}, or {@code PG}),
     * used when registering metrics and telemetry for this connector.
     */
    private final String sourceType;
    /**
     * The source-specific configuration describing the endpoint, request pattern, and behaviour of this connector.
     */
    private final SourceConfig sourceConfig;
    /**
     * The metric configuration controlling telemetry, the metric id, and the shutdown period for this connector.
     */
    private final ExternalMetricConfig externalMetricConfig;
    /**
     * The schema configuration providing column metadata, input/output proto classes, and the stencil orchestrator.
     */
    private final SchemaConfig schemaConfig;
    /**
     * Reporter used to surface fatal and non-fatal errors raised while making external calls.
     */
    private ErrorReporter errorReporter;
    /**
     * Manager used to register and emit meter-style metrics for the external source aspects.
     */
    private MeterStatsManager meterStatsManager;
    /**
     * Resolver used to look up protobuf {@link Descriptors.Descriptor}s for request and response messages.
     */
    private DescriptorManager descriptorManager;
    /**
     * Telemetry collected for this connector, keyed by telemetry type, with each key mapping to a list of values.
     */
    private Map<String, List<String>> metrics = new HashMap<>();
    /**
     * The protobuf descriptor of the output message that enriched values are written into.
     */
    private Descriptors.Descriptor outputDescriptor;
    /**
     * Helper that resolves endpoint/request variable values from the incoming {@link Row}.
     */
    private EndpointHandler endpointHandler;

    /**
     * Instantiates a new Async connector.
     *
     * @param sourceType           the source type
     * @param sourceConfig         the source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     */
    public AsyncConnector(String sourceType, SourceConfig sourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig) {
        this.sourceType = sourceType;
        this.sourceConfig = sourceConfig;
        this.externalMetricConfig = externalMetricConfig;
        this.schemaConfig = schemaConfig;
    }

    /**
     * Gets error reporter.
     *
     * @return the error reporter
     */
    protected ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    /**
     * Gets meter stats manager.
     *
     * @return the meter stats manager
     */
    protected MeterStatsManager getMeterStatsManager() {
        return meterStatsManager;
    }

    /**
     * Gets endpoint handler.
     *
     * @return the endpoint handler
     */
    protected EndpointHandler getEndpointHandler() {
        return endpointHandler;
    }

    /**
     * Gets column name manager.
     *
     * @return the column name manager
     */
    public ColumnNameManager getColumnNameManager() {
        return schemaConfig.getColumnNameManager();
    }

    /**
     * Sets error reporter.
     *
     * @param errorReporter the error reporter
     */
    public void setErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    /**
     * Sets meter stats manager.
     *
     * @param meterStatsManager the meter stats manager
     */
    public void setMeterStatsManager(MeterStatsManager meterStatsManager) {
        this.meterStatsManager = meterStatsManager;
    }

    /**
     * Sets descriptor manager.
     *
     * @param descriptorManager the descriptor manager
     */
    public void setDescriptorManager(DescriptorManager descriptorManager) {
        this.descriptorManager = descriptorManager;
    }

    /**
     * Gets descriptor manager.
     *
     * @return the descriptor manager
     */
    public DescriptorManager getDescriptorManager() {
        return descriptorManager;
    }

    /**
     * Initialize the descriptor manager.
     *
     * @param config the config
     * @return the descriptor manager
     */
    protected DescriptorManager initDescriptorManager(SchemaConfig config) {
        return new DescriptorManager(config.getStencilClientOrchestrator());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Initializes the connector by lazily creating the {@link DescriptorManager}, the external client
     * (via {@link #createClient()}), the {@link ErrorReporter}, the {@link MeterStatsManager}, and the
     * {@link EndpointHandler}, and then registers the external source metrics under the configured source
     * type and metric id.
     *
     * @param configuration the Flink runtime configuration supplied during operator initialization
     * @throws Exception if the parent initialization or client creation fails
     */
    @Override
    public void open(Configuration configuration) throws Exception {
        super.open(configuration);

        if (descriptorManager == null) {
            descriptorManager = initDescriptorManager(schemaConfig);
        }

        createClient();

        if (errorReporter == null) {
            errorReporter = ErrorReporterFactory
                    .getErrorReporter(getRuntimeContext().getMetricGroup(), externalMetricConfig.isTelemetryEnabled(), externalMetricConfig.getShutDownPeriod());
        }
        if (meterStatsManager == null) {
            meterStatsManager = new MeterStatsManager(getRuntimeContext().getMetricGroup(), true);
        }
        if (endpointHandler == null) {
            endpointHandler = new EndpointHandler(meterStatsManager, errorReporter,
                    schemaConfig.getInputProtoClasses(), schemaConfig.getColumnNameManager(), descriptorManager);
        }

        String groupKey = TelemetryTypes.SOURCE_METRIC_ID.getValue();
        String groupValue = sourceType + "." + externalMetricConfig.getMetricId();
        meterStatsManager.register(groupKey, groupValue, ExternalSourceAspects.values());
    }

    /**
     * Create client.
     */
    protected abstract void createClient();

    /**
     * Process async.
     *
     * @param input        the input
     * @param resultFuture the result future
     * @throws Exception the exception
     */
    protected abstract void process(Row input, ResultFuture<Row> resultFuture) throws Exception;

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #process(Row, ResultFuture)} and records a successful external call. Pattern or
     * variable configuration problems are translated into an {@code InvalidConfigurationException} that is
     * reported and propagated through the result future.
     *
     * @param input        the incoming row to be enriched by the external lookup
     * @param resultFuture the future used to emit the enriched row or an error
     * @throws Exception if processing fails in an unrecoverable way
     */
    @Override
    public void asyncInvoke(Row input, ResultFuture<Row> resultFuture) throws Exception {

        try {
            process(input, resultFuture);
            meterStatsManager.markEvent(ExternalSourceAspects.TOTAL_EXTERNAL_CALLS);
        } catch (UnknownFormatConversionException e) {
            meterStatsManager.markEvent(ExternalSourceAspects.INVALID_CONFIGURATION);
            Exception invalidConfigurationException = new InvalidConfigurationException(String.format("pattern config '%s' is invalid", sourceConfig.getPattern()));
            reportAndThrowError(resultFuture, invalidConfigurationException);
        } catch (IllegalFormatException e) {
            meterStatsManager.markEvent(ExternalSourceAspects.INVALID_CONFIGURATION);
            Exception invalidConfigurationException = new InvalidConfigurationException(String.format("pattern config '%s' is incompatible with the variable config '%s'", sourceConfig.getPattern(), sourceConfig.getVariables()));
            reportAndThrowError(resultFuture, invalidConfigurationException);
        } catch (InvalidConfigurationException e) {
            meterStatsManager.markEvent(ExternalSourceAspects.INVALID_CONFIGURATION);
            reportAndThrowError(resultFuture, e);
        }
    }

    /**
     * Report and throw error.
     *
     * @param resultFuture the result future
     * @param exception    the exception
     */
    protected void reportAndThrowError(ResultFuture<Row> resultFuture, Exception exception) {
        errorReporter.reportFatalException(exception);
        resultFuture.completeExceptionally(exception);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Marks a timeout metric and either reports and throws the error (when the source is configured to fail
     * on errors) or reports it as non-fatal, finally completing the future with the original unmodified input row.
     *
     * @param input        the row whose external call timed out
     * @param resultFuture the future used to emit the fallback row or an error
     */
    @Override
    public void timeout(Row input, ResultFuture<Row> resultFuture) {
        meterStatsManager.markEvent(ExternalSourceAspects.TIMEOUTS);
        Exception timeoutException = new TimeoutException("Timeout in external source call!");
        if (sourceConfig.isFailOnErrors()) {
            reportAndThrowError(resultFuture, timeoutException);
        } else {
            errorReporter.reportNonFatalException(timeoutException);
        }
        resultFuture.complete(singleton(input));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the parent implementation to release any resources held by the rich async function.
     *
     * @throws Exception if the parent cleanup fails
     */
    @Override
    public void close() throws Exception {

        super.close();
    }

    /**
     * {@inheritDoc}
     *
     * @return the telemetry collected for this connector, keyed by telemetry type
     */
    @Override
    public Map<String, List<String>> getTelemetry() {
        return metrics;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Records the post-processor type telemetry entry for this connector's source type before telemetry
     * subscribers are notified.
     */
    @Override
    public void preProcessBeforeNotifyingSubscriber() {
        addMetric(TelemetryTypes.POST_PROCESSOR_TYPE.getValue(), sourceType);
    }

    /**
     * Appends a telemetry value under the given key, creating the backing list on first use.
     *
     * @param key   the telemetry key to record the value under
     * @param value the telemetry value to add
     */
    private void addMetric(String key, String value) {
        metrics.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    /**
     * Gets output descriptor.
     *
     * @param resultFuture the result future
     * @return the output descriptor
     */
    protected Descriptors.Descriptor getOutputDescriptor(ResultFuture<Row> resultFuture) {
        String descriptorClassName = sourceConfig.getType() != null ? sourceConfig.getType() : schemaConfig.getOutputProtoClassName();
        if (StringUtils.isNotEmpty(descriptorClassName)) {
            try {
                outputDescriptor = descriptorManager.getDescriptor(descriptorClassName);
            } catch (DescriptorNotFoundException descriptorNotFound) {
                reportAndThrowError(resultFuture, descriptorNotFound);
            }
        }
        return outputDescriptor;
    }


}
