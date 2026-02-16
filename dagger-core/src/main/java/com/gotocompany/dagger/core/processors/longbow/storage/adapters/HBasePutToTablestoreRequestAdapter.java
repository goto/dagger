package com.gotocompany.dagger.core.processors.longbow.storage.adapters;

import com.alicloud.openservices.tablestore.model.ColumnValue;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyBuilder;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.PutRowRequest;
import com.alicloud.openservices.tablestore.model.RowPutChange;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Put;

@RequiredArgsConstructor
public class HBasePutToTablestoreRequestAdapter implements TablestoreRequestAdapter<Put, PutRowRequest> {

    private final String primaryKeyName;
    private final String tableId;

    @Override
    public PutRowRequest adapt(Put request) {
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(primaryKeyName, PrimaryKeyValue.fromBinary(request.getRow()));
        PrimaryKey primaryKey = primaryKeyBuilder.build();
        RowPutChange rowPutChange = new RowPutChange(tableId, primaryKey);

        request.getFamilyCellMap()
                .forEach((columnFamilyName, columns) -> columns.forEach(cell -> rowPutChange.addColumn(
                        new String(cell.getQualifierArray()),
                        ColumnValue.fromBinary(cell.getValueArray())
                )));
        return new PutRowRequest(rowPutChange);
    }

}
