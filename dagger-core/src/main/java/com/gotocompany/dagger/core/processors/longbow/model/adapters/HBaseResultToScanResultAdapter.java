package com.gotocompany.dagger.core.processors.longbow.model.adapters;

import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;
import org.apache.hadoop.hbase.client.Result;

import java.util.NavigableMap;

public class HBaseResultToScanResultAdapter implements ScanResultAdapter<Result> {

    @Override
    public ScanResult adapt(Result result) {
        ScanResult scanResult = new ScanResult(result.getRow());
        NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> rowMaps = result.getMap();
        rowMaps.forEach(
                (columnFamily, columnMap) -> columnMap.forEach((columnName, timestampMap) ->
                        scanResult.addData(columnFamily, columnName, timestampMap.firstEntry().getValue()))
        );
        return scanResult;
    }

}
