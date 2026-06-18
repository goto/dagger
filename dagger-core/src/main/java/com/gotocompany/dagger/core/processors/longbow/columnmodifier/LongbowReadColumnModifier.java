package com.gotocompany.dagger.core.processors.longbow.columnmodifier;

import com.gotocompany.dagger.core.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Longbow read column modifier.
 */
public class LongbowReadColumnModifier implements ColumnModifier {

    /**
     * Appends the Longbow Protobuf data column to the input column names.
     *
     * <p>Used by the Longbow+ read flow so that the serialized Protobuf payload scanned from BigTable
     * is exposed as an additional output column.
     *
     * @param inputColumnNames the incoming column names
     * @return the column names with the Longbow proto data column appended
     */
    @Override
    public String[] modifyColumnNames(String[] inputColumnNames) {
        ArrayList<String> inputColumnList = new ArrayList<>(Arrays.asList(inputColumnNames));
        inputColumnList.add(inputColumnList.size(), Constants.LONGBOW_PROTO_DATA_KEY);

        return inputColumnList.toArray(new String[0]);
    }
}
