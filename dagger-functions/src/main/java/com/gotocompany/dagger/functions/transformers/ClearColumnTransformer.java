package com.gotocompany.dagger.functions.transformers;


import com.gotocompany.dagger.common.core.DaggerContext;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.core.Transformer;

import java.util.Arrays;
import java.util.Map;

/**
 * Allows to clear the specified column of data produced by the dagger.
 */
public class ClearColumnTransformer implements MapFunction<Row, Row>, Transformer {
    /**
     * Transformation-argument key whose value names the column to be cleared.
     */
    private static final String TARGET_KEY_COLUMN_NAME = "targetColumnName";
    /**
     * Name of the column whose value is replaced with an empty string by this transformer.
     */
    private final String targetColumnName;
    /**
     * Ordered names of the columns in the incoming {@link Row}, used to resolve the target column index.
     */
    private String[] columnNames;

    /**
     * Instantiates a new Clear column transformer.
     *
     * @param transformationArguments the transformation arguments
     * @param columnNames             the column names
     * @param daggerContext           the daggerContext
     */
    public ClearColumnTransformer(Map<String, String> transformationArguments, String[] columnNames, DaggerContext daggerContext) {
        this.columnNames = columnNames;
        this.targetColumnName = transformationArguments.get(TARGET_KEY_COLUMN_NAME);
    }

    /**
     * Copies the incoming row and blanks out the configured target column.
     *
     * <p>Every field is copied from {@code inputRow} into a new {@link Row} of the same arity, and the
     * field at the resolved target-column index is overwritten with an empty string.
     *
     * @param inputRow the row to transform
     * @return a new row identical to {@code inputRow} except that the target column is set to an empty string
     * @throws IllegalArgumentException if the configured target column is not present in the column names
     */
    @Override
    public Row map(Row inputRow) throws IllegalArgumentException {
        int targetFieldIndex = Arrays.asList(columnNames).indexOf(targetColumnName);
        if (targetFieldIndex == -1) {
            throw new IllegalArgumentException("Target Column is not defined OR doesn't exists");
        }
        Row outputRow = new Row(inputRow.getArity());
        for (int i = 0; i < inputRow.getArity(); i++) {
            outputRow.setField(i, inputRow.getField(i));
        }
        outputRow.setField(targetFieldIndex, "");
        return outputRow;
    }

    /**
     * Wires this map function into the streaming pipeline.
     *
     * <p>Applies this transformer as a {@link MapFunction} over the input data stream and returns a new
     * {@link StreamInfo} that preserves the original column names.
     *
     * @param inputStreamInfo the incoming stream and its column metadata
     * @return a {@link StreamInfo} wrapping the mapped data stream with the original column names
     */
    @Override
    public StreamInfo transform(StreamInfo inputStreamInfo) {
        DataStream<Row> inputStream = inputStreamInfo.getDataStream();
        SingleOutputStreamOperator<Row> outputStream = inputStream.map(this);
        return new StreamInfo(outputStream, inputStreamInfo.getColumnNames());
    }

}

