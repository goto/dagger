package com.gotocompany.dagger.core.processors.longbow.model.adapters;

import com.alicloud.openservices.tablestore.model.Row;
import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;

public class TablestoreRowToScanResultAdapter implements ScanResultAdapter<Row> {

    private final String columnFamilyName;

    public TablestoreRowToScanResultAdapter(String columnFamilyName) {
        this.columnFamilyName = columnFamilyName;
    }

    @Override
    public ScanResult adapt(Row row) {
        ScanResult scanResult = new ScanResult(row.getPrimaryKey().getPrimaryKeyColumn(0).getNameRawData());
        row.getColumnsMap().forEach((columnName, timestampToValueMap) -> scanResult.addData(columnFamilyName.getBytes(), columnName.getBytes(), timestampToValueMap.get(0).asBinary()));
        return scanResult;
    }

}
