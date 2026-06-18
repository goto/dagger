package com.gotocompany.dagger.core.processors.longbow.data;

import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Longbow table data.
 */
public class LongbowTableData implements LongbowData {

    /**
     * The BigTable column family, in bytes, that Longbow data is stored under.
     */
    private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT);
    /**
     * The Longbow schema used to resolve which data columns to read from the scan results.
     */
    private LongbowSchema longbowSchema;

    /**
     * Instantiates a new Longbow table data.
     *
     * @param longbowSchema the longbow schema
     */
    public LongbowTableData(LongbowSchema longbowSchema) {
        this.longbowSchema = longbowSchema;
    }

    /**
     * Groups the scanned BigTable rows by Longbow data column name.
     *
     * <p>For each schema column that holds Longbow data an entry is produced; the value is the list
     * of cell values across the scan results, or an empty list when the scan returned nothing.
     *
     * @param scanResult the BigTable scan results to read from
     * @return a map from each Longbow data column name to its list of string values
     */
    @Override
    public Map<String, List<String>> parse(List<Result> scanResult) {
        Map<String, List<String>> longbowData = new HashMap<>();
        List<String> longbowDataColumnNames = longbowSchema.getColumnNames(c -> c.getKey().contains(Constants.LONGBOW_DATA_KEY));
        if (scanResult.isEmpty()) {
            longbowDataColumnNames.forEach(name -> longbowData.put(name, new ArrayList<>()));
        } else {
            longbowDataColumnNames.forEach(name -> longbowData.put(name, getData(scanResult, name)));
        }
        return longbowData;
    }

    /**
     * Reads the values of a single column across all scan results.
     *
     * @param resultScan the BigTable scan results to read from
     * @param name       the column qualifier to extract
     * @return the list of string values for the given column, one per result
     */
    private List<String> getData(List<Result> resultScan, String name) {
        return resultScan
                .stream()
                .map(result -> Bytes.toString(result.getValue(COLUMN_FAMILY_NAME, Bytes.toBytes(name))))
                .collect(Collectors.toList());
    }
}
