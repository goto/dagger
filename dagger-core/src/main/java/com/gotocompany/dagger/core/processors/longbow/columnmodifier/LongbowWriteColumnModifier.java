package com.gotocompany.dagger.core.processors.longbow.columnmodifier;

import com.gotocompany.dagger.core.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Longbow write column modifier.
 */
public class LongbowWriteColumnModifier implements ColumnModifier {

    /**
     * Appends the synchronizer metadata columns to the input column names.
     *
     * <p>Used by the Longbow write flow to expose the BigTable table id, the input class name and the
     * Longbow read key needed to synchronize with the downstream reader.
     *
     * @param inputColumnNames the incoming column names
     * @return the column names with the synchronizer columns appended
     */
    @Override
    public String[] modifyColumnNames(String[] inputColumnNames) {
        ArrayList<String> outputList = new ArrayList<>(Arrays.asList(inputColumnNames));
        outputList.add(Constants.SYNCHRONIZER_BIGTABLE_TABLE_ID_KEY);
        outputList.add(Constants.SYNCHRONIZER_INPUT_CLASSNAME_KEY);
        outputList.add(Constants.SYNCHRONIZER_LONGBOW_READ_KEY);
        return outputList.toArray(new String[0]);
    }
}
