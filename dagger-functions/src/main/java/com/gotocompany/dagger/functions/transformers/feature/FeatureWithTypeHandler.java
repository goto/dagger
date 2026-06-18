package com.gotocompany.dagger.functions.transformers.feature;

import com.gotocompany.dagger.functions.udfs.aggregate.feast.FeatureUtils;
import com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.types.Row;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Feature with type handler.
 */
public class FeatureWithTypeHandler implements Serializable {
    /**
     * Transformation-argument key whose value names the output column that receives the feature rows.
     */
    private static final String OUTPUT_COLUMN_NAME_KEY = "outputColumnName";
    /**
     * Transformation-argument key whose value lists the per-feature key, value and type definitions.
     */
    private static final String OUTPUT_COLUMN_DATA_KEY = "data";
    /**
     * Key, within a feature definition, naming the column that supplies the feature key.
     */
    private static final String KEY_COLUMN_NAME = "keyColumnName";
    /**
     * Key, within a feature definition, naming the column that supplies the feature value.
     */
    private static final String VALUE_COLUMN_NAME = "valueColumnName";
    /**
     * Key, within a feature definition, naming the value type of the feature.
     */
    private static final String TYPE = "type";
    /**
     * Number of fields in each generated feature row, namely the key, value and value type.
     */
    private static final int FEATURE_ROW_LENGTH = 3;


    /**
     * Ordered names of the columns in the incoming {@link Row}, used to resolve column indices.
     */
    private String[] inputColumns;
    /**
     * Per-feature definitions, each holding the key column, value column and value type.
     */
    private List<Tuple3<String, String, String>> featureInfoList;
    /**
     * Name of the column that receives the generated array of feature rows.
     */
    private String outputColumnName;

    /**
     * Instantiates a new Feature with type handler.
     *
     * @param transformationArguments the transformation arguments
     * @param inputColumns            the input columns
     */
    public FeatureWithTypeHandler(Map<String, Object> transformationArguments, String[] inputColumns) {
        this.inputColumns = inputColumns;
        this.outputColumnName = String.valueOf(transformationArguments.get(OUTPUT_COLUMN_NAME_KEY));
        createFeatureInfoList(transformationArguments);
    }

    /**
     * Builds the list of feature definitions from the transformation arguments.
     *
     * <p>Reads the output-column data entries and, for each, records the key column, value column and
     * value type as a {@link Tuple3} in the feature-info list.
     *
     * @param transformationArguments the transformation arguments supplied to this handler
     */
    private void createFeatureInfoList(Map<String, Object> transformationArguments) {
        featureInfoList = new ArrayList<>();
        List<Map<String, String>> outputColumnData = (List<Map<String, String>>) transformationArguments.get(OUTPUT_COLUMN_DATA_KEY);
        outputColumnData.forEach(featureInfo -> featureInfoList.add(
                new Tuple3<String, String, String>(featureInfo.get(KEY_COLUMN_NAME), featureInfo.get(VALUE_COLUMN_NAME), featureInfo.get(TYPE))
        ));
    }

    /**
     * Gets output column index.
     *
     * @return the output column index
     */
    public int getOutputColumnIndex() {
        int outputColumnIndex = Arrays.asList(inputColumns).indexOf(outputColumnName);
        if (outputColumnIndex == -1) {
            throw new IllegalArgumentException("OutputColumnName is not given or not exist");
        }
        return outputColumnIndex;
    }

    /**
     * Populate features array list.
     *
     * @param inputRow the input row
     * @return the array list
     */
    public ArrayList<Row> populateFeatures(Row inputRow) {
        ArrayList<Row> featureRows = new ArrayList<>();
        for (Tuple3<String, String, String> stringStringStringTuple3 : featureInfoList) {
            int featureKeyIndex = Arrays.asList(inputColumns).indexOf(stringStringStringTuple3.f0);
            int featureValueIndex = Arrays.asList(inputColumns).indexOf(stringStringStringTuple3.f1);
            if (featureKeyIndex == -1 || featureValueIndex == -1) {
                throw new IllegalArgumentException("FeatureKey OR FeatureValue is not defined OR doesn't exists");
            }
            String key = String.valueOf(inputRow.getField(featureKeyIndex));
            Object value = inputRow.getField(featureValueIndex);
            ValueEnum valueEnum = ValueEnum.valueOf(String.valueOf(stringStringStringTuple3.f2));
            FeatureUtils.populateFeaturesWithType(featureRows, key, value, valueEnum, FEATURE_ROW_LENGTH);
        }
        return featureRows;
    }
}
