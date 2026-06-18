package com.gotocompany.dagger.core.processors.longbow.outputRow;

import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;

import org.apache.flink.types.Row;

import java.util.HashMap;
import java.util.Map;

/**
 * The Reader output longbow data.
 */
public class ReaderOutputLongbowData implements ReaderOutputRow {
    /**
     * Schema used to resolve column names and indices when assembling the output row.
     */
    private LongbowSchema longbowSchema;

    /**
     * Instantiates a new Reader output longbow data.
     *
     * @param longbowSchema the longbow schema
     */
    public ReaderOutputLongbowData(LongbowSchema longbowSchema) {
        this.longbowSchema = longbowSchema;
    }

    /**
     * Merges the BigTable scan result with the input row into a Longbow output row.
     *
     * <p>Non Longbow-data columns are copied from {@code input}, then every entry of
     * {@code scanResult} overrides or adds to that map. Each value is finally written into a new row
     * at the index assigned to its column by the {@link LongbowSchema}.
     *
     * @param scanResult the parsed Longbow data keyed by output column name
     * @param input      the input row that triggered the lookup
     * @return a new row populated with the merged input and scanned Longbow data
     */
    @Override
    public Row get(Map<String, Object> scanResult, Row input) {
        HashMap<String, Object> columnMap = new HashMap<>();
        longbowSchema.getColumnNames(c -> !isLongbowData(c))
                .forEach(name -> columnMap.put(name, longbowSchema.getValue(input, name)));
        scanResult.forEach(columnMap::put);
        int arity = input.getArity();
        Row output = new Row(arity);
        columnMap.forEach((name, data) -> {
            output.setField(longbowSchema.getIndex(name), data);
        });
        return output;
    }

    /**
     * Determines whether the given schema column holds Longbow data.
     *
     * @param c a schema entry mapping a column name to its row index
     * @return {@code true} if the column name contains the Longbow data key marker, otherwise {@code false}
     */
    private boolean isLongbowData(Map.Entry<String, Integer> c) {
        return c.getKey().contains(Constants.LONGBOW_DATA_KEY);
    }
}
