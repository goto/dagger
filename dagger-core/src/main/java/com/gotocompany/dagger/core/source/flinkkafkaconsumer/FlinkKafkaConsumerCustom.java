package com.gotocompany.dagger.core.source.flinkkafkaconsumer;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporterFactory;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.runtime.tasks.ExceptionInChainedOperatorException;
import org.apache.flink.types.Row;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * A class responsible for consuming the messages in kafka.
 * {@link FlinkKafkaConsumer}.
 */
public class FlinkKafkaConsumerCustom extends FlinkKafkaConsumer<Row> {

    /**
     * Dagger configuration used to build the {@link ErrorReporter} when consumption fails.
     */
    private Configuration configuration;
    /**
     * Reporter used to publish fatal exceptions raised while consuming Kafka records.
     */
    private ErrorReporter errorReporter;

    /**
     * Instantiates a new Flink kafka consumer custom.
     *
     * @param subscriptionPattern the subscription pattern
     * @param deserializer        the deserializer
     * @param props               the props
     * @param configuration       the configuration
     */
    public FlinkKafkaConsumerCustom(Pattern subscriptionPattern, KafkaDeserializationSchema<Row> deserializer,
                                    Properties props, Configuration configuration) {
        super(subscriptionPattern, deserializer, props);
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@code runBaseConsumer} and, on any non-chained failure, reports the
     * exception as fatal through the {@link ErrorReporter} before rethrowing it. An
     * {@code ExceptionInChainedOperatorException} is rethrown unchanged so that downstream
     * operator failures are not masked.
     *
     * @param sourceContext the Flink source context that emitted records are written to
     * @throws Exception if the underlying Kafka consumer fails
     */
    @Override
    public void run(SourceContext<Row> sourceContext) throws Exception {
        try {
            runBaseConsumer(sourceContext);
        } catch (ExceptionInChainedOperatorException chainedOperatorException) {
            throw chainedOperatorException;
        } catch (Exception exception) {
            errorReporter = getErrorReporter(getRuntimeContext());
            errorReporter.reportFatalException(exception);
            throw exception;
        }
    }

    /**
     * Run base consumer.
     *
     * @param sourceContext the source context
     * @throws Exception the exception
     */
    protected void runBaseConsumer(SourceContext<Row> sourceContext) throws Exception {
        super.run(sourceContext);
    }

    /**
     * Gets error reporter.
     *
     * @param runtimeContext the runtime context
     * @return the error reporter
     */
    protected ErrorReporter getErrorReporter(RuntimeContext runtimeContext) {
        return ErrorReporterFactory.getErrorReporter(runtimeContext.getMetricGroup(), configuration);
    }
}
