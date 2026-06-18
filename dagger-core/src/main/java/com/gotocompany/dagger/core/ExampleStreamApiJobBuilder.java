package com.gotocompany.dagger.core;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.DaggerContext;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.watermark.LastColumnWatermark;
import com.gotocompany.dagger.common.watermark.StreamWatermarkAssigner;
import com.gotocompany.dagger.common.watermark.WatermarkStrategyDefinition;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.dagger.core.processors.PreProcessorFactory;
import com.gotocompany.dagger.core.processors.telemetry.processor.MetricsTelemetryExporter;
import com.gotocompany.dagger.core.processors.types.Preprocessor;
import com.gotocompany.dagger.core.sink.SinkOrchestrator;
import com.gotocompany.dagger.core.sink.influx.InfluxSinkOverrides;
import com.gotocompany.dagger.core.source.StreamsFactory;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example {@link JobBuilder} that demonstrates assembling a Dagger job with the raw Flink DataStream
 * API instead of SQL.
 *
 * <p>This is a reference/template implementation (not used in production) showing how to register
 * sources with pre-processors, keep references to specific input streams by name, and apply native
 * Flink stream operators before sinking the result. The data-processing body is intentionally minimal
 * and contains commented-out snippets illustrating common patterns such as keying and aggregation.
 */
public class ExampleStreamApiJobBuilder implements JobBuilder {

//    static final String KEY_PATH = "meta.customer.id";

    /** Name of the first demo input stream this example keeps a reference to. */
    private final String inputStreamName1 = "data_streams_0";
    /** Name of the second demo input stream this example keeps a reference to. */
    private final String inputStreamName2 = "data_streams_1";
    /** Registered input streams keyed by stream name, populated during source registration. */
    private final Map<String, StreamInfo> dataStreams = new HashMap<>();

    /** Shared context bundling configuration and the Flink execution environment. */
    private final DaggerContext daggerContext;
    /** Dagger job configuration resolved from the program arguments. */
    private final Configuration configuration;
    /** Flink streaming execution environment the job is built on and submitted to. */
    private final StreamExecutionEnvironment executionEnvironment;
    /** Provides Protobuf/stencil schema descriptors to sources, processors, and sinks. */
    private StencilClientOrchestrator stencilClientOrchestrator;
    /** StatsD reporter used to emit Dagger metrics. */
    private DaggerStatsDReporter daggerStatsDReporter;
    /** Collects and publishes job telemetry to subscribers as processors and sinks are added. */
    private final MetricsTelemetryExporter telemetryExporter = new MetricsTelemetryExporter();

    /**
     * Creates the example job builder bound to the given Dagger context.
     *
     * @param daggerContext the shared context providing configuration and the Flink execution
     *                      environment
     */
    public ExampleStreamApiJobBuilder(DaggerContext daggerContext) {
        this.daggerContext = daggerContext;
        this.configuration = daggerContext.getConfiguration();
        this.executionEnvironment = daggerContext.getExecutionEnvironment();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds the {@link StencilClientOrchestrator} and {@link DaggerStatsDReporter} and applies a
     * minimal set of Flink runtime settings (max parallelism, exactly-once checkpointing, and global
     * job parameters) onto the execution environment.
     *
     * @return this builder, for fluent chaining
     */
    @Override
    public JobBuilder registerConfigs() {
        stencilClientOrchestrator = new StencilClientOrchestrator(configuration);
        org.apache.flink.configuration.Configuration flinkConfiguration = (org.apache.flink.configuration.Configuration) this.executionEnvironment.getConfiguration();
        daggerStatsDReporter = DaggerStatsDReporter.Provider.provide(flinkConfiguration, configuration);

        executionEnvironment.setMaxParallelism(configuration.getInteger(Constants.FLINK_PARALLELISM_MAX_KEY, Constants.FLINK_PARALLELISM_MAX_DEFAULT));
        executionEnvironment.getCheckpointConfig().setTolerableCheckpointFailureNumber(Integer.MAX_VALUE);
        executionEnvironment.enableCheckpointing(configuration.getLong(Constants.FLINK_CHECKPOINT_INTERVAL_MS_KEY, Constants.FLINK_CHECKPOINT_INTERVAL_MS_DEFAULT));
        executionEnvironment.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        // goes on...
        executionEnvironment.getConfig().setGlobalJobParameters(configuration.getParam());
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Registers each configured source as a watermark-assigned {@code DataStream} of {@code Row}
     * records, runs it through the configured pre-processors, and stores the streams named
     * {@code data_streams_0} and {@code data_streams_1} for later use by the example output stage.
     *
     * @return this builder, for fluent chaining
     */
    @Override
    public JobBuilder registerSourceWithPreProcessors() {
        long watermarkDelay = configuration.getLong(Constants.FLINK_WATERMARK_DELAY_MS_KEY, Constants.FLINK_WATERMARK_DELAY_MS_DEFAULT);
        Boolean enablePerPartitionWatermark = configuration.getBoolean(Constants.FLINK_WATERMARK_PER_PARTITION_ENABLE_KEY, Constants.FLINK_WATERMARK_PER_PARTITION_ENABLE_DEFAULT);

        StreamsFactory.getStreams(configuration, stencilClientOrchestrator, daggerStatsDReporter)
                .forEach(stream -> {
                    String tableName = stream.getStreamName();

                    WatermarkStrategyDefinition watermarkStrategyDefinition = new LastColumnWatermark();

                    DataStream<Row> dataStream = stream.registerSource(executionEnvironment, watermarkStrategyDefinition.getWatermarkStrategy(watermarkDelay));
                    StreamWatermarkAssigner streamWatermarkAssigner = new StreamWatermarkAssigner(new LastColumnWatermark());

                    DataStream<Row> dataStream1 = streamWatermarkAssigner
                            .assignTimeStampAndWatermark(dataStream, watermarkDelay, enablePerPartitionWatermark);


                    // just some legacy objects to adopt preprocessors
                    TableSchema tableSchema = TableSchema.fromTypeInfo(dataStream.getType());
                    StreamInfo streamInfo = new StreamInfo(dataStream1, tableSchema.getFieldNames());
                    streamInfo = addPreProcessor(streamInfo, tableName);

                    if (tableName.equals(inputStreamName1)) {
                        dataStreams.put(inputStreamName1, streamInfo);
                    }
                    if (tableName.equals(inputStreamName2)) {
                        dataStreams.put(inputStreamName2, streamInfo);
                    }
                });
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This example registers no user-defined functions and simply returns the builder unchanged.
     *
     * @return this builder, for fluent chaining
     * @throws IOException declared to satisfy the interface; never thrown by this implementation
     */
    @Override
    public JobBuilder registerFunctions() throws IOException {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Takes the registered {@code data_streams_0} input, applies a trivial keyed aggregation using
     * the Flink DataStream API as a placeholder for real processing logic, and routes the result to
     * the configured sink. Richer keying/aggregation patterns are shown in the commented-out snippets.
     *
     * @return this builder, for fluent chaining
     * @throws NullPointerException if the expected input stream was not registered
     */
    @Override
    public JobBuilder registerOutputStream() {
        // NOTE - GET THE DATASTREAM REFERENCE
        StreamInfo streamInfo = dataStreams.get(inputStreamName1);
        Preconditions.checkNotNull(streamInfo, "Expected page log stream to be registered with name %s", inputStreamName1);

        DataStream<Row> inputStream = streamInfo.getDataStream();

        SinkOrchestrator sinkOrchestrator = new SinkOrchestrator(telemetryExporter);
        sinkOrchestrator.addSubscriber(telemetryExporter);

        SingleOutputStreamOperator<Row> outputStream =
                inputStream

                        // NOTE - USE THE FLINK STREAM APIS HERE AND SINK THE OUTPUT

//                        .keyBy(
//                                new KeySelector<Row, Integer>() {
//                                    private KeyExtractor keyExtractor;
//
//                                    @Override
//                                    public Integer getKey(Row row) {
//                                        if (keyExtractor == null) {
//                                            keyExtractor = new KeyExtractor(row, KEY_PATH);
//                                        }
//                                        int userId = keyExtractor.extract(row);
//                                        return userId % DAU_PARALLELISM;
//                                    }
//                                })
//                        .process(new ShardedDistinctUserCounter())
//                        .keyBy(r -> 0) // move all the output to one operator to calculate aggregation of all
//                        .process(new UserCounterAggregator());
                        .keyBy(r -> 0)
                        .max("someField");

        outputStream.sinkTo(sinkOrchestrator.getSink(configuration, new String[]{"uniq_users"}, stencilClientOrchestrator, daggerStatsDReporter, InfluxSinkOverrides.none()));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Submits the assembled pipeline to the Flink execution environment under the configured job
     * name.
     *
     * @throws Exception if the Flink job fails to submit or execute
     */
    @Override
    public void execute() throws Exception {
        executionEnvironment.execute(configuration.getString(Constants.FLINK_JOB_ID_KEY, Constants.FLINK_JOB_ID_DEFAULT));
    }

    /**
     * Applies all configured pre-processors for a source stream in sequence.
     *
     * @param streamInfo the stream to pre-process
     * @param tableName  the name of the source/table whose pre-processors should be applied
     * @return the stream after every pre-processor has been applied
     */
    private StreamInfo addPreProcessor(StreamInfo streamInfo, String tableName) {
        List<Preprocessor> preProcessors = PreProcessorFactory.getPreProcessors(daggerContext, tableName, telemetryExporter);
        for (Preprocessor preprocessor : preProcessors) {
            streamInfo = preprocessor.process(streamInfo);
        }
        return streamInfo;
    }
}
