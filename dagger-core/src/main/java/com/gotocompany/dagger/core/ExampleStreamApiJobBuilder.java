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

public class ExampleStreamApiJobBuilder implements JobBuilder {

//    static final String KEY_PATH = "meta.customer.id";

    private final String inputStreamName1 = "data_streams_0";
    private final String inputStreamName2 = "data_streams_1";
    private final Map<String, StreamInfo> dataStreams = new HashMap<>();

    private final DaggerContext daggerContext;
    private final Configuration configuration;
    private final StreamExecutionEnvironment executionEnvironment;
    private StencilClientOrchestrator stencilClientOrchestrator;
    private DaggerStatsDReporter daggerStatsDReporter;
    private final MetricsTelemetryExporter telemetryExporter = new MetricsTelemetryExporter();

    public ExampleStreamApiJobBuilder(DaggerContext daggerContext) {
        this.daggerContext = daggerContext;
        this.configuration = daggerContext.getConfiguration();
        this.executionEnvironment = daggerContext.getExecutionEnvironment();
    }

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

    @Override
    public JobBuilder registerFunctions() throws IOException {
        return this;
    }

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

        outputStream.sinkTo(sinkOrchestrator.getSink("", configuration, new String[]{"uniq_users"}, stencilClientOrchestrator, daggerStatsDReporter));
        return this;
    }

    @Override
    public void execute() throws Exception {
        executionEnvironment.execute(configuration.getString(Constants.FLINK_JOB_ID_KEY, Constants.FLINK_JOB_ID_DEFAULT));
    }

    private StreamInfo addPreProcessor(StreamInfo streamInfo, String tableName) {
        List<Preprocessor> preProcessors = PreProcessorFactory.getPreProcessors(daggerContext, tableName, telemetryExporter);
        for (Preprocessor preprocessor : preProcessors) {
            streamInfo = preprocessor.process(streamInfo);
        }
        return streamInfo;
    }
}
