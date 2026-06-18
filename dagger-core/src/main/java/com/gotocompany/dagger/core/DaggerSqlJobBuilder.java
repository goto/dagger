package com.gotocompany.dagger.core;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.DaggerContext;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.udfs.UdfFactory;
import com.gotocompany.dagger.common.watermark.LastColumnWatermark;
import com.gotocompany.dagger.common.watermark.NoWatermark;
import com.gotocompany.dagger.common.watermark.StreamWatermarkAssigner;
import com.gotocompany.dagger.common.watermark.WatermarkStrategyDefinition;
import com.gotocompany.dagger.core.exception.UDFFactoryClassNotDefinedException;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.dagger.core.processors.PostProcessorFactory;
import com.gotocompany.dagger.core.processors.PreProcessorFactory;
import com.gotocompany.dagger.core.processors.types.PostProcessor;
import com.gotocompany.dagger.core.processors.types.Preprocessor;
import com.gotocompany.dagger.core.sink.SinkOrchestrator;
import com.gotocompany.dagger.core.sink.influx.InfluxSinkOverrides;
import com.gotocompany.dagger.core.source.StreamsFactory;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.telemetry.processor.MetricsTelemetryExporter;
import com.gotocompany.dagger.functions.udfs.python.PythonUdfConfig;
import com.gotocompany.dagger.functions.udfs.python.PythonUdfManager;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.ApiExpression;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;

import static com.gotocompany.dagger.functions.common.Constants.PYTHON_UDF_ENABLE_DEFAULT;
import static com.gotocompany.dagger.functions.common.Constants.PYTHON_UDF_ENABLE_KEY;
import static org.apache.flink.table.api.Expressions.$;

/**
 * Default {@link JobBuilder} implementation that assembles and runs a Dagger SQL job on Flink.
 *
 * <p>The builder is driven by {@link KafkaProtoSQLProcessor} and wires together every stage of a
 * Dagger pipeline: it configures the Flink execution and table environments, registers source
 * streams (with their watermark strategies and pre-processors), registers user-defined functions,
 * runs the configured SQL query, applies post-processors and finally attaches the sink. Each
 * {@code register*} step returns {@code this} so the registration stages can be chained fluently.
 */
public class DaggerSqlJobBuilder implements JobBuilder {

    /**
     * Dagger configuration that supplies all Flink, source, SQL and processor settings.
     */
    private final Configuration configuration;
    /**
     * Flink streaming execution environment configured and used to run the job.
     */
    private final StreamExecutionEnvironment executionEnvironment;
    /**
     * Flink table environment used to register sources and functions and to run the SQL query.
     */
    private final StreamTableEnvironment tableEnvironment;
    /**
     * Exporter that publishes pipeline metrics gathered from sources, processors and sinks.
     */
    private final MetricsTelemetryExporter telemetryExporter = new MetricsTelemetryExporter();
    /**
     * Orchestrates the Stencil client used to resolve Protobuf descriptors at runtime.
     */
    private StencilClientOrchestrator stencilClientOrchestrator;
    /**
     * Reporter that emits StatsD metrics for the running job.
     */
    private DaggerStatsDReporter daggerStatsDReporter;

    /**
     * Context holding the configuration together with the Flink execution and table environments.
     */
    private final DaggerContext daggerContext;

    /**
     * Instantiates dagger sql job-builder.
     *
     * @param daggerContext the daggerContext in form of param
     */
    public DaggerSqlJobBuilder(DaggerContext daggerContext) {
        this.daggerContext = daggerContext;
        this.configuration = daggerContext.getConfiguration();
        this.executionEnvironment = daggerContext.getExecutionEnvironment();
        this.tableEnvironment = daggerContext.getTableEnvironment();
    }

    /**
     * Register configs stream manager.
     *
     * @return the stream manager
     */
    @Override
    public JobBuilder registerConfigs() {
        stencilClientOrchestrator = new StencilClientOrchestrator(configuration);
        org.apache.flink.configuration.Configuration flinkConfiguration = (org.apache.flink.configuration.Configuration) this.executionEnvironment.getConfiguration();
        daggerStatsDReporter = DaggerStatsDReporter.Provider.provide(flinkConfiguration, configuration);

        executionEnvironment.setMaxParallelism(configuration.getInteger(Constants.FLINK_PARALLELISM_MAX_KEY, Constants.FLINK_PARALLELISM_MAX_DEFAULT));
        executionEnvironment.setParallelism(configuration.getInteger(Constants.FLINK_PARALLELISM_KEY, Constants.FLINK_PARALLELISM_DEFAULT));
        executionEnvironment.getConfig().setAutoWatermarkInterval(configuration.getInteger(Constants.FLINK_WATERMARK_INTERVAL_MS_KEY, Constants.FLINK_WATERMARK_INTERVAL_MS_DEFAULT));
        executionEnvironment.getCheckpointConfig().setTolerableCheckpointFailureNumber(Integer.MAX_VALUE);
        executionEnvironment.enableCheckpointing(configuration.getLong(Constants.FLINK_CHECKPOINT_INTERVAL_MS_KEY, Constants.FLINK_CHECKPOINT_INTERVAL_MS_DEFAULT));
        executionEnvironment.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        executionEnvironment.getCheckpointConfig().setCheckpointTimeout(configuration.getLong(Constants.FLINK_CHECKPOINT_TIMEOUT_MS_KEY, Constants.FLINK_CHECKPOINT_TIMEOUT_MS_DEFAULT));
        executionEnvironment.getCheckpointConfig().setMinPauseBetweenCheckpoints(configuration.getLong(Constants.FLINK_CHECKPOINT_MIN_PAUSE_MS_KEY, Constants.FLINK_CHECKPOINT_MIN_PAUSE_MS_DEFAULT));
        executionEnvironment.getCheckpointConfig().setMaxConcurrentCheckpoints(configuration.getInteger(Constants.FLINK_CHECKPOINT_MAX_CONCURRENT_KEY, Constants.FLINK_CHECKPOINT_MAX_CONCURRENT_DEFAULT));
        executionEnvironment.getConfig().setGlobalJobParameters(configuration.getParam());

        tableEnvironment.getConfig().setIdleStateRetention(Duration.ofMinutes(configuration.getInteger(Constants.FLINK_RETENTION_IDLE_STATE_MINUTE_KEY, Constants.FLINK_RETENTION_IDLE_STATE_MINUTE_DEFAULT)));
        return this;
    }

    /**
     * Register source with pre processors stream manager.
     *
     * @return the stream manager
     */
    @Override
    public JobBuilder registerSourceWithPreProcessors() {
        long watermarkDelay = configuration.getLong(Constants.FLINK_WATERMARK_DELAY_MS_KEY, Constants.FLINK_WATERMARK_DELAY_MS_DEFAULT);
        Boolean enablePerPartitionWatermark = configuration.getBoolean(Constants.FLINK_WATERMARK_PER_PARTITION_ENABLE_KEY, Constants.FLINK_WATERMARK_PER_PARTITION_ENABLE_DEFAULT);
        StreamsFactory.getStreams(configuration, stencilClientOrchestrator, daggerStatsDReporter)
                .forEach(stream -> {
                    String tableName = stream.getStreamName();
                    WatermarkStrategyDefinition watermarkStrategyDefinition = getSourceWatermarkDefinition(enablePerPartitionWatermark);
                    DataStream<Row> dataStream = stream.registerSource(executionEnvironment, watermarkStrategyDefinition.getWatermarkStrategy(watermarkDelay));
                    StreamWatermarkAssigner streamWatermarkAssigner = new StreamWatermarkAssigner(new LastColumnWatermark());

                    DataStream<Row> dataStreamWithWaterMark = streamWatermarkAssigner
                            .assignTimeStampAndWatermark(dataStream, watermarkDelay, enablePerPartitionWatermark);

                    TableSchema tableSchema = TableSchema.fromTypeInfo(dataStream.getType());
                    StreamInfo streamInfo = new StreamInfo(dataStreamWithWaterMark, tableSchema.getFieldNames());
                    streamInfo = addPreProcessor(streamInfo, tableName);

                    Table table = tableEnvironment.fromDataStream(streamInfo.getDataStream(), getApiExpressions(streamInfo));
                    tableEnvironment.createTemporaryView(tableName, table);
                });
        return this;
    }

    /**
     * Selects the watermark strategy definition to apply when registering a source stream.
     *
     * @param enablePerPartitionWatermark whether per-partition watermarks are enabled; when
     *                                    {@code true} a {@link LastColumnWatermark} is used,
     *                                    otherwise a {@link NoWatermark} is returned
     * @return the watermark strategy definition matching the requested behaviour
     */
    private WatermarkStrategyDefinition getSourceWatermarkDefinition(Boolean enablePerPartitionWatermark) {
        return enablePerPartitionWatermark ? new LastColumnWatermark() : new NoWatermark();
    }

    /**
     * Builds the Flink Table API expressions used to convert a source data stream into a table.
     *
     * <p>Every column is projected by its name and the final column is bound to the configured
     * row-time attribute so that event-time based SQL operations work as expected.
     *
     * @param streamInfo the stream metadata holding the ordered column names of the source
     * @return an array of {@code ApiExpression}, one per column, with the last element marked as the
     *         row-time attribute; an empty array when the stream has no columns
     */
    private ApiExpression[] getApiExpressions(StreamInfo streamInfo) {
        String rowTimeAttributeName = configuration.getString(Constants.FLINK_ROWTIME_ATTRIBUTE_NAME_KEY, Constants.FLINK_ROWTIME_ATTRIBUTE_NAME_DEFAULT);
        String[] columnNames = streamInfo.getColumnNames();
        ApiExpression[] expressions = new ApiExpression[columnNames.length];
        if (columnNames.length == 0) {
            return expressions;
        }
        for (int i = 0; i < columnNames.length - 1; i++) {
            ApiExpression expression = $(columnNames[i]);
            expressions[i] = expression;
        }
        expressions[columnNames.length - 1] = $(rowTimeAttributeName).rowtime();
        return expressions;
    }


    /**
     * Register functions stream manager.
     *
     * @return the stream manager
     */
    @Override
    public JobBuilder registerFunctions() throws IOException {
        if (configuration.getBoolean(PYTHON_UDF_ENABLE_KEY, PYTHON_UDF_ENABLE_DEFAULT)) {
            PythonUdfConfig pythonUdfConfig = PythonUdfConfig.parse(configuration);
            PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, configuration);
            pythonUdfManager.registerPythonFunctions();
        }

        String[] functionFactoryClasses = configuration
                .getString(Constants.FUNCTION_FACTORY_CLASSES_KEY, Constants.FUNCTION_FACTORY_CLASSES_DEFAULT)
                .split(",");

        for (String className : functionFactoryClasses) {
            try {
                UdfFactory udfFactory = getUdfFactory(className);
                udfFactory.registerFunctions();
            } catch (ReflectiveOperationException e) {
                throw new UDFFactoryClassNotDefinedException(e.getMessage());
            }
        }
        return this;
    }

    /**
     * Reflectively instantiates a {@link UdfFactory} from its fully-qualified class name.
     *
     * <p>The factory class is expected to expose a constructor that accepts a
     * {@code StreamTableEnvironment} and a {@link Configuration}, which receive the job's table
     * environment and configuration respectively.
     *
     * @param udfFactoryClassName the fully-qualified name of the UDF factory class to load
     * @return the instantiated UDF factory, ready to register its functions
     * @throws ClassNotFoundException    if the named class cannot be found on the classpath
     * @throws NoSuchMethodException     if the class has no constructor with the expected signature
     * @throws IllegalAccessException    if the constructor is not accessible
     * @throws InvocationTargetException if the underlying constructor throws an exception
     * @throws InstantiationException    if the class cannot be instantiated
     */
    private UdfFactory getUdfFactory(String udfFactoryClassName) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> udfFactoryClass = Class.forName(udfFactoryClassName);
        Constructor<?> udfFactoryClassConstructor = udfFactoryClass.getConstructor(StreamTableEnvironment.class, Configuration.class);
        return (UdfFactory) udfFactoryClassConstructor.newInstance(tableEnvironment, configuration);
    }

    /**
     * Register output stream manager.
     *
     * @return the stream manager
     */
    @Override
    public JobBuilder registerOutputStream() {
        Table table = tableEnvironment.sqlQuery(configuration.getString(Constants.FLINK_SQL_QUERY_KEY, Constants.FLINK_SQL_QUERY_DEFAULT));
        StreamInfo streamInfo = createStreamInfo(table);
        streamInfo = addPostProcessor(streamInfo);
        addSink(streamInfo);
        return this;
    }

    /**
     * Execute dagger job.
     *
     * @throws Exception the exception
     */
    @Override
    public void execute() throws Exception {
        executionEnvironment.execute(configuration.getString(Constants.FLINK_JOB_ID_KEY, Constants.FLINK_JOB_ID_DEFAULT));
    }

    /**
     * Create stream info.
     *
     * @param table the table
     * @return the stream info
     */
    protected StreamInfo createStreamInfo(Table table) {
        DataStream<Row> stream = tableEnvironment
                .toRetractStream(table, Row.class)
                .filter(value -> value.f0)
                .map(value -> value.f1);
        return new StreamInfo(stream, table.getSchema().getFieldNames());
    }

    /**
     * Applies the configured post-processors to the given stream in declaration order.
     *
     * <p>Post-processors enrich or transform the output {@code Row} records (for example through
     * HTTP, GRPC, Elasticsearch, Postgres, longbow or transformer lookups) before the data is
     * handed off to the sink.
     *
     * @param streamInfo the stream produced by the SQL query
     * @return the stream after every post-processor has been applied
     */
    private StreamInfo addPostProcessor(StreamInfo streamInfo) {
        List<PostProcessor> postProcessors = PostProcessorFactory.getPostProcessors(daggerContext, stencilClientOrchestrator, streamInfo.getColumnNames(), telemetryExporter);
        for (PostProcessor postProcessor : postProcessors) {
            streamInfo = postProcessor.process(streamInfo);
        }
        return streamInfo;
    }

    /**
     * Applies the configured pre-processors for a source table to the given stream in order.
     *
     * @param streamInfo the stream registered from the source
     * @param tableName  the name of the source table whose pre-processors should be applied
     * @return the stream after every pre-processor has been applied
     */
    private StreamInfo addPreProcessor(StreamInfo streamInfo, String tableName) {
        List<Preprocessor> preProcessors = PreProcessorFactory.getPreProcessors(daggerContext, tableName, telemetryExporter);
        for (Preprocessor preprocessor : preProcessors) {
            streamInfo = preprocessor.process(streamInfo);
        }
        return streamInfo;
    }

    /**
     * Builds the configured sink and attaches it to the stream as the job's terminal stage.
     *
     * <p>A {@code SinkOrchestrator} resolves the sink implementation from the configuration and the
     * telemetry exporter is subscribed so that sink metrics are reported.
     *
     * @param streamInfo the fully processed stream to be written to the sink
     */
    private void addSink(StreamInfo streamInfo) {
        SinkOrchestrator sinkOrchestrator = new SinkOrchestrator(telemetryExporter);
        sinkOrchestrator.addSubscriber(telemetryExporter);
        streamInfo.getDataStream().sinkTo(sinkOrchestrator.getSink(configuration, streamInfo.getColumnNames(), stencilClientOrchestrator, daggerStatsDReporter, InfluxSinkOverrides.none()));
    }
}
