package com.gotocompany.dagger.core.processors.longbow;

import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.types.Row;

import com.google.gson.Gson;
import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.core.processors.longbow.columnmodifier.LongbowReadColumnModifier;
import com.gotocompany.dagger.core.processors.longbow.columnmodifier.LongbowWriteColumnModifier;
import com.gotocompany.dagger.core.processors.longbow.columnmodifier.NoOpColumnModifier;
import com.gotocompany.dagger.core.processors.longbow.data.LongbowProtoData;
import com.gotocompany.dagger.core.processors.longbow.data.LongbowTableData;
import com.gotocompany.dagger.core.processors.longbow.outputRow.OutputIdentity;
import com.gotocompany.dagger.core.processors.longbow.outputRow.OutputSynchronizer;
import com.gotocompany.dagger.core.processors.longbow.outputRow.ReaderOutputLongbowData;
import com.gotocompany.dagger.core.processors.longbow.outputRow.ReaderOutputProtoData;
import com.gotocompany.dagger.core.processors.longbow.processor.LongbowReader;
import com.gotocompany.dagger.core.processors.longbow.processor.LongbowWriter;
import com.gotocompany.dagger.core.processors.longbow.range.LongbowRange;
import com.gotocompany.dagger.core.processors.longbow.range.LongbowRangeFactory;
import com.gotocompany.dagger.core.processors.longbow.request.PutRequestFactory;
import com.gotocompany.dagger.core.processors.longbow.request.ScanRequestFactory;
import com.gotocompany.dagger.core.processors.longbow.validator.LongbowType;
import com.gotocompany.dagger.core.processors.longbow.validator.LongbowValidator;
import com.gotocompany.dagger.core.processors.telemetry.processor.MetricsTelemetryExporter;
import com.gotocompany.dagger.core.processors.types.PostProcessor;

import java.util.ArrayList;
import java.util.Map;

import static com.gotocompany.dagger.common.core.Constants.INPUT_STREAMS;
import static com.gotocompany.dagger.common.core.Constants.STREAM_INPUT_SCHEMA_PROTO_CLASS;
import static com.gotocompany.dagger.core.utils.Constants.DAGGER_NAME_DEFAULT;
import static com.gotocompany.dagger.core.utils.Constants.DAGGER_NAME_KEY;
import static com.gotocompany.dagger.core.utils.Constants.PROCESSOR_LONGBOW_GCP_TABLE_ID_KEY;

/**
 * The factory class for Longbow.
 */
public class LongbowFactory {
    /**
     * The Longbow schema describing the column layout and Longbow type of the input stream.
     */
    private LongbowSchema longbowSchema;
    /**
     * The Dagger configuration used to resolve Longbow, BigTable and stream settings.
     */
    private Configuration configuration;
    /**
     * The async processor used to wrap the rich async functions into ordered-wait operators.
     */
    private AsyncProcessor asyncProcessor;
    /**
     * The orchestrator that supplies the Stencil client for resolving Protobuf descriptors.
     */
    private StencilClientOrchestrator stencilClientOrchestrator;
    /**
     * The exporter that the reader and writer notify so their metrics are published.
     */
    private MetricsTelemetryExporter metricsTelemetryExporter;
    /**
     * The Longbow column names derived from the schema, used to build readers and writers.
     */
    private String[] columnNames;
    /**
     * Shared {@code Gson} instance used to parse the input streams configuration JSON.
     */
    private static final Gson GSON = new Gson();

    /**
     * Instantiates a new Longbow factory.
     *
     * @param longbowSchema             the longbow schema
     * @param configuration                    the configuration
     * @param stencilClientOrchestrator the stencil client orchestrator
     * @param metricsTelemetryExporter  the metrics telemetry exporter
     */
    public LongbowFactory(LongbowSchema longbowSchema, Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, MetricsTelemetryExporter metricsTelemetryExporter) {
        this.longbowSchema = longbowSchema;
        this.configuration = configuration;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.metricsTelemetryExporter = metricsTelemetryExporter;
        this.columnNames = longbowSchema.getColumnNames().toArray(new String[0]);
        this.asyncProcessor = new AsyncProcessor();
    }

    /**
     * Instantiates a new Longbow factory.
     *
     * @param longbowSchema             the longbow schema
     * @param configuration             the configuration
     * @param stencilClientOrchestrator the stencil client orchestrator
     * @param metricsTelemetryExporter  the metrics telemetry exporter
     * @param asyncProcessor            the async processor
     */
    public LongbowFactory(LongbowSchema longbowSchema, Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, MetricsTelemetryExporter metricsTelemetryExporter, AsyncProcessor asyncProcessor) {
        this(longbowSchema, configuration, stencilClientOrchestrator, metricsTelemetryExporter);
        this.longbowSchema = longbowSchema;
        this.configuration = configuration;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.metricsTelemetryExporter = metricsTelemetryExporter;
        this.columnNames = longbowSchema.getColumnNames().toArray(new String[0]);
        this.asyncProcessor = asyncProcessor;
    }

    /**
     * Gets longbow processor.
     *
     * @return the longbow processor
     */
    public PostProcessor getLongbowProcessor() {
        LongbowReader longbowReader;
        LongbowWriter longbowWriter;
        LongbowValidator longbowValidator = new LongbowValidator(columnNames);
        LongbowType longbowType = longbowSchema.getType();

        ArrayList<RichAsyncFunction<Row, Row>> longbowRichFunctions = new ArrayList<>();
        longbowValidator.validateLongbow(longbowType);
        switch (longbowType) {
            case LongbowWrite:
                longbowWriter = longbowWriterPlus();
                longbowRichFunctions.add(longbowWriter);
                longbowWriter.notifySubscriber(metricsTelemetryExporter);
                return new LongbowProcessor(asyncProcessor, configuration, longbowRichFunctions, new LongbowWriteColumnModifier());
            case LongbowRead:
                longbowReader = longbowReaderPlus();
                longbowRichFunctions.add(longbowReader);
                longbowReader.notifySubscriber(metricsTelemetryExporter);
                return new LongbowProcessor(asyncProcessor, configuration, longbowRichFunctions, new LongbowReadColumnModifier());
            default:
                longbowWriter = longbowWriter();
                longbowReader = longbowReader();
                longbowRichFunctions.add(longbowWriter);
                longbowRichFunctions.add(longbowReader);
                longbowWriter.notifySubscriber(metricsTelemetryExporter);
                longbowReader.notifySubscriber(metricsTelemetryExporter);
                return new LongbowProcessor(asyncProcessor, configuration, longbowRichFunctions, new NoOpColumnModifier());
        }
    }

    /**
     * Builds a {@link LongbowReader} for the Longbow+ read flow, where the scanned BigTable rows
     * carry serialized Protobuf payloads.
     *
     * <p>The reader is wired with the range resolved from the schema, a {@code ScanRequestFactory}
     * targeting the configured BigTable table, a {@link LongbowProtoData} parser and a
     * {@code ReaderOutputProtoData} output mapper.
     *
     * @return the configured Longbow reader for the Protobuf-backed read flow
     */
    private LongbowReader longbowReaderPlus() {
        LongbowRange longbowRange = LongbowRangeFactory.getLongbowRange(longbowSchema);
        ScanRequestFactory scanRequestFactory = new ScanRequestFactory(longbowSchema, getTableId(configuration));
        ReaderOutputProtoData readerOutputRow = new ReaderOutputProtoData(longbowSchema);
        LongbowProtoData longbowTableData = new LongbowProtoData();
        return new LongbowReader(configuration, longbowSchema, longbowRange, longbowTableData, scanRequestFactory, readerOutputRow);
    }

    /**
     * Builds a {@link LongbowReader} for the standard Longbow read flow, where scanned BigTable rows
     * are mapped back into individual schema columns.
     *
     * <p>The reader is wired with the range resolved from the schema, a {@code ScanRequestFactory}
     * targeting the configured table, a {@link LongbowTableData} parser and a
     * {@code ReaderOutputLongbowData} output mapper.
     *
     * @return the configured Longbow reader for the column-based read flow
     */
    private LongbowReader longbowReader() {
        LongbowRange longbowRange = LongbowRangeFactory.getLongbowRange(longbowSchema);
        ScanRequestFactory scanRequestFactory = new ScanRequestFactory(longbowSchema, getTableId(configuration));
        ReaderOutputLongbowData readerOutputRow = new ReaderOutputLongbowData(longbowSchema);
        LongbowTableData longbowTableData = new LongbowTableData(longbowSchema);
        return new LongbowReader(configuration, longbowSchema, longbowRange, longbowTableData, scanRequestFactory, readerOutputRow);
    }

    /**
     * Builds a {@link LongbowWriter} for the Longbow+ write flow, serializing the input row into a
     * Protobuf payload before persisting it to BigTable.
     *
     * <p>A {@link ProtoSerializer} is created from the configured input Protobuf class, and the
     * writer is given a {@code PutRequestFactory} plus an {@code OutputSynchronizer} that records the
     * synchronization metadata for the downstream reader.
     *
     * @return the configured Longbow writer for the Protobuf-backed write flow
     */
    private LongbowWriter longbowWriterPlus() {
        ProtoSerializer protoSerializer = new ProtoSerializer(null, getMessageProtoClassName(configuration), columnNames, stencilClientOrchestrator);
        String tableId = getTableId(configuration);
        PutRequestFactory putRequestFactory = new PutRequestFactory(longbowSchema, protoSerializer, tableId);
        OutputSynchronizer outputSynchronizer = new OutputSynchronizer(longbowSchema, tableId, getMessageProtoClassName(configuration));
        return new LongbowWriter(configuration, longbowSchema, putRequestFactory, tableId, outputSynchronizer);
    }

    /**
     * Builds a {@link LongbowWriter} for the standard Longbow write flow, persisting the row columns
     * directly to BigTable.
     *
     * <p>The writer uses a {@code PutRequestFactory} without a serializer and an
     * {@code OutputIdentity} that passes the input row through unchanged.
     *
     * @return the configured Longbow writer for the column-based write flow
     */
    private LongbowWriter longbowWriter() {
        String tableId = getTableId(configuration);
        PutRequestFactory putRequestFactory = new PutRequestFactory(longbowSchema, null, tableId);
        OutputIdentity outputIdentity = new OutputIdentity();
        return new LongbowWriter(configuration, longbowSchema, putRequestFactory, tableId, outputIdentity);
    }

    /**
     * Resolves the BigTable table id to use for Longbow operations.
     *
     * <p>The explicit Longbow GCP table id is preferred; when it is absent the Dagger job name (or
     * its default) is used instead.
     *
     * @param config the configuration to read the table id and Dagger name from
     * @return the resolved BigTable table id
     */
    private String getTableId(Configuration config) {
        return config
                .getString(PROCESSOR_LONGBOW_GCP_TABLE_ID_KEY, config.getString(DAGGER_NAME_KEY, DAGGER_NAME_DEFAULT));
    }

    /**
     * Extracts the input Protobuf message class name from the first configured input stream.
     *
     * @param config the configuration holding the input streams JSON
     * @return the fully-qualified Protobuf class name of the first input stream
     */
    private String getMessageProtoClassName(Configuration config) {
        String jsonArrayString = config.getString(INPUT_STREAMS, "");
        Map[] streamsConfig = GSON.fromJson(jsonArrayString, Map[].class);
        return (String) streamsConfig[0].get(STREAM_INPUT_SCHEMA_PROTO_CLASS);
    }
}
