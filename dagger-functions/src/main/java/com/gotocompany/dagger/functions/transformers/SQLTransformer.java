package com.gotocompany.dagger.functions.transformers;

import com.gotocompany.dagger.common.core.DaggerContext;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.core.Transformer;
import com.gotocompany.dagger.common.watermark.RowtimeFieldWatermark;
import com.gotocompany.dagger.common.watermark.StreamWatermarkAssigner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;


/**
 * Enables to apply a SQL transformation on top of streaming data in post processors.
 */
public class SQLTransformer implements Serializable, Transformer {
    /**
     * Ordered names of the columns in the incoming {@link Row}, used to build the table schema.
     */
    private final String[] columnNames;
    /**
     * SQL query applied to the registered table to produce the transformed stream.
     */
    private final String sqlQuery;
    /**
     * Name under which the input stream is registered as a table for the SQL query.
     */
    private final String tableName;
    /**
     * Allowed lateness, in milliseconds, used when assigning watermarks for the rowtime attribute.
     */
    private final long allowedLatenessInMs;
    /**
     * Name of the column treated as the event-time (rowtime) attribute.
     */
    private static final String ROWTIME = "rowtime";
    /**
     * Dagger context providing access to the Flink table environment.
     */
    private final DaggerContext daggerContext;

    /**
     * Instantiates a new Sql transformer.
     *
     * @param transformationArguments the transformation arguments
     * @param columnNames             the column names
     * @param daggerContext           the daggerContext
     */
    public SQLTransformer(Map<String, String> transformationArguments, String[] columnNames, DaggerContext daggerContext) {
        this.columnNames = columnNames;
        this.sqlQuery = transformationArguments.get("sqlQuery");
        this.tableName = transformationArguments.getOrDefault("tableName", "data_stream");
        this.allowedLatenessInMs = Long.parseLong(transformationArguments.getOrDefault("allowedLatenessInMs", "0"));
        this.daggerContext = daggerContext;
    }

    /**
     * Applies the configured SQL query to the input stream and returns the resulting stream.
     *
     * <p>Builds the table schema from the column names, registers the input stream as a table (assigning
     * a rowtime time attribute and watermarks when a rowtime column is present), runs the SQL query and
     * converts the resulting retract stream back into an append-only stream of {@link Row} records.
     *
     * @param inputStreamInfo the incoming stream and its column metadata
     * @return a {@link StreamInfo} wrapping the query-result stream together with the query output column names
     * @throws IllegalArgumentException if no SQL query was provided in the transformation arguments
     */
    @Override
    public StreamInfo transform(StreamInfo inputStreamInfo) {
        DataStream<Row> inputStream = inputStreamInfo.getDataStream();
        if (sqlQuery == null) {
            throw new IllegalArgumentException("SQL Query must pe provided in Transformation Arguments");
        }
        String schema = String.join(",", columnNames);
        if (Arrays.asList(columnNames).contains(ROWTIME)) {
            schema = schema.replace(ROWTIME, ROWTIME + ".rowtime");
            inputStream = assignTimeAttribute(inputStream);
        }
        StreamTableEnvironment streamTableEnvironment = daggerContext.getTableEnvironment();
        streamTableEnvironment.registerDataStream(tableName, inputStream, schema);

        Table table = streamTableEnvironment.sqlQuery(sqlQuery);
        SingleOutputStreamOperator<Row> outputStream = streamTableEnvironment
                .toRetractStream(table, Row.class)
                .filter(value -> value.f0)
                .map(value -> value.f1);
        return new StreamInfo(outputStream, table.getSchema().getFieldNames());
    }

    /**
     * Assigns timestamps and watermarks to the stream based on the rowtime field.
     *
     * <p>Uses a {@link StreamWatermarkAssigner} backed by a {@link RowtimeFieldWatermark} over the column
     * names, applying the configured allowed lateness.
     *
     * @param inputStream the stream to which timestamps and watermarks are assigned
     * @return the input stream with timestamps and watermarks assigned
     */
    private DataStream<Row> assignTimeAttribute(DataStream<Row> inputStream) {
        StreamWatermarkAssigner streamWatermarkAssigner = new StreamWatermarkAssigner(new RowtimeFieldWatermark(columnNames));
        return streamWatermarkAssigner.assignTimeStampAndWatermark(inputStream, allowedLatenessInMs);
    }
}
