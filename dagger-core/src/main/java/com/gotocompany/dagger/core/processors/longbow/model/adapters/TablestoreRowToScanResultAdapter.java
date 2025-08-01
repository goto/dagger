package com.gotocompany.dagger.core.processors.longbow.model.adapters;

import com.alicloud.openservices.tablestore.model.Row;
import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;

public class TablestoreRowToScanResultAdapter implements ScanResultAdapter<Row> {

    private static final int PRIMARY_COLUMN_INDEX = 0;

    private final String columnFamilyName;

    public TablestoreRowToScanResultAdapter(String columnFamilyName) {
        this.columnFamilyName = columnFamilyName;
    }

    @Override
    public ScanResult adapt(Row row) {
        ScanResult scanResult = new ScanResult(row.getPrimaryKey().getPrimaryKeyColumn(PRIMARY_COLUMN_INDEX).getNameRawData());
        row.getColumnsMap()
                .forEach((columnName, timestampToValueMap) ->
                        scanResult.addData(columnFamilyName.getBytes(), columnName.getBytes(), timestampToValueMap.firstEntry().getValue().asBinary()));
        return scanResult;
    }

}
