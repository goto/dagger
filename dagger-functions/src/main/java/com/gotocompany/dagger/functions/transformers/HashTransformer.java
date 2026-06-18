package com.gotocompany.dagger.functions.transformers;

import com.gotocompany.dagger.common.core.DaggerContext;
import com.gotocompany.dagger.functions.transformers.hash.PathReader;
import com.gotocompany.dagger.functions.transformers.hash.field.RowHasher;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.types.Row;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.core.Transformer;
import com.gotocompany.dagger.common.exceptions.DescriptorNotFoundException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Enables encryption on a set of fields as configured.
 * Using SHA-256 hashing to encrypt data.
 */
public class HashTransformer extends RichMapFunction<Row, Row> implements Serializable, Transformer {
    /**
     * Configuration key holding the proto class name of the output (sink) Kafka message.
     */
    private static final String SINK_KAFKA_PROTO_MESSAGE = "SINK_KAFKA_PROTO_MESSAGE";
    /**
     * Transformation-argument key whose value lists the field paths to be hashed.
     */
    private static final String ENCRYPTION_FIELD_KEY = "maskColumns";
    /**
     * Dot-separated field paths whose values must be masked using SHA-256 hashing.
     */
    private final List<String> fieldsToHash;
    /**
     * Dagger context providing access to the job configuration.
     */
    private final DaggerContext daggerContext;
    /**
     * Ordered names of the top-level columns in the incoming {@link Row}.
     */
    private final String[] columnNames;
    /**
     * Mapping from each configured field path to the {@code RowHasher} that masks it.
     */
    private Map<String, RowHasher> rowHasherMap;

    /**
     * Instantiates a new Hash transformer.
     *
     * @param transformationArguments the transformation arguments
     * @param columnNames             the column names
     * @param daggerContext           the daggerContext
     */
    public HashTransformer(Map<String, Object> transformationArguments, String[] columnNames, DaggerContext daggerContext) {
        this.fieldsToHash = getFieldsToHash(transformationArguments);
        this.columnNames = columnNames;
        this.daggerContext = daggerContext;
    }

    /**
     * Extracts the configured list of field paths to hash from the transformation arguments.
     *
     * @param transformationArguments the transformation arguments supplied to this transformer
     * @return the list of dot-separated field paths to be masked
     */
    private ArrayList<String> getFieldsToHash(Map<String, Object> transformationArguments) {
        return (ArrayList<String>) transformationArguments.get(ENCRYPTION_FIELD_KEY);
    }

    /**
     * Lazily builds the field-path to hasher mapping when the operator starts.
     *
     * <p>If the hasher map has not yet been created it is built from the output proto descriptor before
     * delegating to the superclass initialisation.
     *
     * @param internalFlinkConfig the Flink configuration supplied by the runtime
     * @throws Exception if building the hasher map or the superclass initialisation fails
     */
    @Override
    public void open(org.apache.flink.configuration.Configuration internalFlinkConfig) throws Exception {
        if (this.rowHasherMap == null) {
            this.rowHasherMap = createRowHasherMap();
        }
        super.open(internalFlinkConfig);
    }

    /**
     * Wires this hashing map function into the streaming pipeline.
     *
     * <p>Applies this {@link RichMapFunction} over the input data stream and returns a new
     * {@link StreamInfo} that preserves the original column names.
     *
     * @param streamInfo the incoming stream and its column metadata
     * @return a {@link StreamInfo} wrapping the mapped data stream with the original column names
     */
    @Override
    public StreamInfo transform(StreamInfo streamInfo) {
        DataStream<Row> inputStream = streamInfo.getDataStream();
        SingleOutputStreamOperator<Row> outputStream = inputStream.map(this);
        return new StreamInfo(outputStream, streamInfo.getColumnNames());
    }

    /**
     * Create row hasher map.
     *
     * @return the map
     */
    protected Map<String, RowHasher> createRowHasherMap() {
        String outputProtoClassName = daggerContext.getConfiguration().getString(SINK_KAFKA_PROTO_MESSAGE, "");
        StencilClientOrchestrator stencilClientOrchestrator = new StencilClientOrchestrator(daggerContext.getConfiguration());
        Descriptors.Descriptor outputDescriptor = stencilClientOrchestrator.getStencilClient().get(outputProtoClassName);
        if (outputDescriptor == null) {
            throw new DescriptorNotFoundException("Output Descriptor for class: " + outputProtoClassName
                    + " not found");
        }
        PathReader pathReader = new PathReader(outputDescriptor, new ArrayList<>(Arrays.asList(columnNames)));
        return pathReader.fieldMaskingPath(fieldsToHash);
    }

    /**
     * Masks every configured field of the incoming row using its SHA-256 hasher.
     *
     * <p>Creates a copy of {@code inputRow} and, for each configured field path, applies the matching
     * {@code RowHasher} to overwrite the field value with its hash.
     *
     * @param inputRow the row whose configured fields should be masked
     * @return a copy of {@code inputRow} with the configured fields replaced by their hashed values
     */
    @Override
    public Row map(Row inputRow) {
        Row outPutRow = Row.copy(inputRow);
        for (String fieldPath : rowHasherMap.keySet()) {
            rowHasherMap.get(fieldPath).maskRow(outPutRow);
        }
        return outPutRow;
    }
}
