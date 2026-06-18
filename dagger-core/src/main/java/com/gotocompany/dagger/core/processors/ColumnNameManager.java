package com.gotocompany.dagger.core.processors;

import com.gotocompany.dagger.core.utils.Constants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Manage input and output column names.
 */
public class ColumnNameManager implements Serializable {
    /**
     * The fixed, ordered list of input column names provided at construction time.
     */
    private final List<String> inputColumnNames;
    /**
     * The ordered list of output column names, with the select-all sentinel expanded to the inputs.
     */
    private List<String> outputColumnNames;

    /**
     * Instantiates a new Column name manager.
     *
     * @param inputColumnNames  the input column names
     * @param outputColumnNames the output column names
     */
    public ColumnNameManager(String[] inputColumnNames, List<String> outputColumnNames) {
        this.inputColumnNames = Arrays.asList(inputColumnNames);
        this.outputColumnNames = setOutputColumnNames(outputColumnNames);
    }

    /**
     * Gets input columns index.
     *
     * @param inputColumnName the input column name
     * @return the input index
     */
    public Integer getInputIndex(String inputColumnName) {
        return inputColumnNames.indexOf(inputColumnName);
    }

    /**
     * Gets output columns index.
     *
     * @param outputColumnName the output column name
     * @return the output index
     */
    public Integer getOutputIndex(String outputColumnName) {
        return outputColumnNames.indexOf(outputColumnName);
    }

    /**
     * Gets output size.
     *
     * @return the output columns size
     */
    public int getOutputSize() {
        return outputColumnNames.size();
    }

    /**
     * Get output column names string [ ].
     *
     * @return the output column names
     */
    public String[] getOutputColumnNames() {
        return outputColumnNames.toArray(new String[0]);
    }

    /**
     * Get input column names string [ ].
     *
     * @return the input column names
     */
    public String[] getInputColumnNames() {
        return inputColumnNames.toArray(new String[0]);
    }

    /**
     * Resolves the effective output column names for this manager.
     *
     * <p>When the configured names request selecting all input columns (via the
     * {@code SELECT *} sentinel value), that sentinel is removed and replaced by the full set of
     * input column names so downstream consumers always observe concrete column names.
     *
     * @param names the configured output column names; may contain the select-all sentinel value
     * @return the resolved list of output column names
     */
    private List<String> setOutputColumnNames(List<String> names) {
        if (selectAllFromInputColumns(names)) {
            names.remove(Constants.SQL_PATH_SELECT_ALL_CONFIG_VALUE);
            names.addAll(inputColumnNames);
        }
        return names;
    }

    /**
     * Checks whether the configured output columns request selecting all input columns.
     *
     * @param names the configured output column names; may be {@code null}
     * @return {@code true} if {@code names} is non-null and contains the select-all sentinel value,
     *         {@code false} otherwise
     */
    private boolean selectAllFromInputColumns(List<String> names) {
        return names != null && names.contains(Constants.SQL_PATH_SELECT_ALL_CONFIG_VALUE);
    }
}
