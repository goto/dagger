package com.gotocompany.dagger.functions.transformers;

import com.gotocompany.dagger.common.core.DaggerContext;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.core.Transformer;

import java.util.Arrays;
import java.util.Map;

/**
 * Allows to deduplicate data produced by the dagger.
 */
public class DeDuplicationTransformer extends RichFilterFunction<Row> implements Transformer {
    /**
     * Name of the keyed Flink state used to remember keys that have already been emitted.
     */
    private static final String DE_DUP_STATE = "DE_DUP_STATE";
    /**
     * Index, within the configured column names, of the column whose value identifies a record.
     */
    private final int keyIndex;
    /**
     * Time-to-live in seconds after which a remembered key expires and a matching record is allowed again.
     */
    private final Integer ttlInSeconds;
    /**
     * Keyed state mapping a previously seen key to a marker, used to detect and drop duplicate records.
     */
    private MapState<String, Integer> mapState;

    /**
     * Instantiates a new De duplication transformer.
     *
     * @param transformationArguments the transformation arguments
     * @param columnNames             the column names
     * @param daggerContext           the daggerContext
     */
    public DeDuplicationTransformer(Map<String, Object> transformationArguments, String[] columnNames, DaggerContext daggerContext) {
        keyIndex = Arrays.asList(columnNames).indexOf(String.valueOf(transformationArguments.get("key_column")));
        ttlInSeconds = Integer.valueOf(String.valueOf(transformationArguments.get("ttl_in_seconds")));
    }

    /**
     * Wires this de-duplication filter into the streaming pipeline.
     *
     * <p>Keys the input stream by the configured key column and applies this {@link RichFilterFunction}
     * so that only the first record seen for each key (within the configured TTL) is forwarded.
     *
     * @param inputStreamInfo the incoming stream and its column metadata
     * @return a {@link StreamInfo} wrapping the de-duplicated data stream with the original column names
     */
    @Override
    public StreamInfo transform(StreamInfo inputStreamInfo) {
        DataStream<Row> inputStream = inputStreamInfo.getDataStream();
        SingleOutputStreamOperator<Row> outputStream = inputStream
                .keyBy((KeySelector<Row, Object>) value -> value.getField(keyIndex))
                .filter(this);
        return new StreamInfo(outputStream, inputStreamInfo.getColumnNames());
    }

    /**
     * Initialises the keyed de-duplication state when the operator starts.
     *
     * <p>Builds a {@link MapStateDescriptor} for the de-duplication state, configures a time-to-live so
     * that remembered keys expire after the configured number of seconds, and obtains the backing
     * {@link MapState} from the runtime context.
     *
     * @param internalFlinkConfig the Flink configuration supplied by the runtime
     * @throws Exception if the superclass initialisation or state acquisition fails
     */
    @Override
    public void open(org.apache.flink.configuration.Configuration internalFlinkConfig) throws Exception {
        super.open(internalFlinkConfig);
        MapStateDescriptor<String, Integer> deDupState = new MapStateDescriptor<>(DE_DUP_STATE, String.class, Integer.class);
        StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.seconds(ttlInSeconds))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .cleanupFullSnapshot()
                .build();
        deDupState.enableTimeToLive(ttlConfig);
        mapState = getRuntimeContext().getMapState(deDupState);
    }

    /**
     * Decides whether a record should pass through based on whether its key was seen before.
     *
     * <p>Reads the key from the configured key column; if the key is absent from the state it is recorded
     * and the record is kept, otherwise the record is treated as a duplicate and dropped.
     *
     * @param value the record being evaluated
     * @return {@code true} if the key has not been seen before and the record should be kept,
     *         {@code false} if it is a duplicate
     * @throws Exception if accessing the keyed state fails
     */
    @Override
    public boolean filter(Row value) throws Exception {
        String key = (String) value.getField(keyIndex);
        boolean keyAlreadyPresent = mapState.contains(key);
        if (!keyAlreadyPresent) {
            mapState.put(key, 1);
        }
        return !keyAlreadyPresent;
    }
}
