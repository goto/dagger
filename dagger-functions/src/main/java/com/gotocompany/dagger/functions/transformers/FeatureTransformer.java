package com.gotocompany.dagger.functions.transformers;

import com.gotocompany.dagger.common.core.DaggerContext;
import com.gotocompany.dagger.functions.udfs.aggregate.feast.FeatureUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.core.Transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Converts to feast Features from post processors.
 */
public class FeatureTransformer implements MapFunction<Row, Row>, Transformer {
    /**
     * Number of fields in each generated feast feature row, namely the key, value and value type.
     */
    private static final int FEATURE_ROW_LENGTH = 3;
    /**
     * Transformation-argument key whose value names the column holding the feature key.
     */
    private static final String KEY_COLUMN_NAME = "keyColumnName";
    /**
     * Transformation-argument key whose value names the column holding the feature value.
     */
    private static final String VALUE_COLUMN_NAME = "valueColumnName";
    /**
     * Name of the column that supplies the feature key.
     */
    private final String keyColumn;
    /**
     * Name of the column that supplies the feature value and that receives the generated feature rows.
     */
    private final String valueColumn;
    /**
     * Ordered names of the columns in the incoming {@link Row}, used to resolve column indices.
     */
    private String[] columnNames;

    /**
     * Instantiates a new Feature transformer.
     *
     * @param transformationArguments the transformation arguments
     * @param columnNames             the column names
     * @param daggerContext           the daggerContext
     */
    public FeatureTransformer(Map<String, String> transformationArguments, String[] columnNames, DaggerContext daggerContext) {
        this.columnNames = columnNames;
        this.keyColumn = transformationArguments.get(KEY_COLUMN_NAME);
        this.valueColumn = transformationArguments.get(VALUE_COLUMN_NAME);
    }

    /**
     * Builds feast feature rows from the configured key and value columns of the incoming row.
     *
     * <p>Resolves the key and value column indices, delegates to {@code FeatureUtils} to populate the
     * feature rows from the key and value, copies all fields of {@code inputRow} into a new {@link Row}
     * and replaces the value column with the generated array of feature rows.
     *
     * @param inputRow the row to transform
     * @return a new row whose value column holds the generated feast feature rows
     * @throws IllegalArgumentException if the configured key or value column does not exist
     * @throws Exception if populating the feature rows fails
     */
    @Override
    public Row map(Row inputRow) throws Exception {
        int featureKeyIndex = Arrays.asList(columnNames).indexOf(keyColumn);
        int featureValueIndex = Arrays.asList(columnNames).indexOf(valueColumn);
        if (featureKeyIndex == -1 || featureValueIndex == -1) {
            throw new IllegalArgumentException("FeatureKey OR FeatureValue is not defined OR doesn't exists");
        }
        ArrayList<Row> featureRows = new ArrayList<>();
        String featureKey = String.valueOf(inputRow.getField(featureKeyIndex));
        Object featureValue = inputRow.getField(featureValueIndex);

        FeatureUtils.populateFeatures(featureRows, featureKey, featureValue, FEATURE_ROW_LENGTH);

        Row outputRow = new Row(inputRow.getArity());
        for (int index = 0; index < inputRow.getArity(); index++) {
            outputRow.setField(index, inputRow.getField(index));
        }
        outputRow.setField(featureValueIndex, featureRows.toArray(new Row[0]));
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
