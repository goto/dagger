package com.gotocompany.dagger.core.processors.longbow.outputRow;

import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;

import org.apache.flink.types.Row;

import java.util.HashMap;
import java.util.Map;

/**
 * The Reader output proto data.
 */
public class ReaderOutputProtoData implements ReaderOutputRow {
    /**
     * Schema used to resolve column names and indices when assembling the output row.
     */
    private LongbowSchema longbowSchema;

    /**
     * Instantiates a new Reader output proto data.
     *
     * @param longbowSchema the longbow schema
     */
    public ReaderOutputProtoData(LongbowSchema longbowSchema) {
        this.longbowSchema = longbowSchema;
    }

    /**
     * Builds a Longbow output row that carries the scanned Protobuf bytes column.
     *
     * <p>All non proto-data columns are copied from {@code input} into their schema-assigned indices,
     * and the serialized Protobuf payload retrieved from {@code scanResult} under
     * {@code Constants.LONGBOW_PROTO_DATA_KEY} is appended as the final field.
     *
     * @param scanResult the parsed scan result containing the Longbow proto-data payload
     * @param input      the input row that triggered the lookup
     * @return a new row with the input columns followed by the scanned proto-data value
     */
    @Override
    public Row get(Map<String, Object> scanResult, Row input) {
        HashMap<String, Object> columnMap = new HashMap<>();
        longbowSchema.getColumnNames(c -> !isLongbowProtoData(c))
                .forEach(name -> columnMap.put(name, longbowSchema.getValue(input, name)));
        int arity = columnMap.size() + 1;
        Row output = new Row(arity);
        columnMap.forEach((name, data) -> {
            output.setField(longbowSchema.getIndex(name), data);
        });
        output.setField(arity - 1, scanResult.get(Constants.LONGBOW_PROTO_DATA_KEY));
        return output;
    }

    /**
     * Determines whether the given schema column holds the Longbow proto-data payload.
     *
     * @param c a schema entry mapping a column name to its row index
     * @return {@code true} if the column name contains the Longbow proto-data key marker, otherwise {@code false}
     */
    private boolean isLongbowProtoData(Map.Entry<String, Integer> c) {
        return c.getKey().contains(Constants.LONGBOW_PROTO_DATA_KEY);
    }
}
