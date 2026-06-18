package com.gotocompany.dagger.common.core;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

/**
 * The class to hold the data stream and column names.
 */
public class StreamInfo {
    /**
     * The underlying Flink data stream of {@link Row} records.
     */
    private DataStream<Row> dataStream;
    /**
     * The column names describing the schema of each {@link Row} in the stream.
     */
    private String[] columnNames;

    /**
     * Instantiates a new Stream info.
     *
     * @param dataStream  the data stream
     * @param columnNames the column names
     */
    public StreamInfo(DataStream<Row> dataStream, String[] columnNames) {
        this.dataStream = dataStream;
        this.columnNames = columnNames;
    }

    /**
     * Gets data stream.
     *
     * @return the data stream
     */
    public DataStream<Row> getDataStream() {
        return dataStream;
    }

    /**
     * Get column names.
     *
     * @return list of column names
     */
    public String[] getColumnNames() {
        return columnNames;
    }
}
