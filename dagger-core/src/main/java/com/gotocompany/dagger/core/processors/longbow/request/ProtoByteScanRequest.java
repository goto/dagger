package com.gotocompany.dagger.core.processors.longbow.request;

import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.storage.ScanRequest;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Request scan the proto byte.
 */
public class ProtoByteScanRequest implements ScanRequest {
    /**
     * Default BigTable column family, in bytes, scanned for the serialized record.
     */
    private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT);
    /**
     * Default BigTable column qualifier, in bytes, holding the serialized proto value.
     */
    private static final byte[] QUALIFIER_NAME = Bytes.toBytes(Constants.LONGBOW_QUALIFIER_DEFAULT);
    /**
     * Inclusive start row key of the scan range.
     */
    private byte[] startRow;
    /**
     * Inclusive stop row key of the scan range.
     */
    private byte[] stopRow;
    /**
     * Identifier of the BigTable table to scan.
     */
    private String tableId;


    /**
     * Instantiates a new Proto byte scan request.
     *
     * @param startRow the start row
     * @param stopRow  the stop row
     * @param tableId  the table id
     */
    public ProtoByteScanRequest(byte[] startRow, byte[] stopRow, String tableId) {
        this.startRow = startRow;
        this.stopRow = stopRow;
        this.tableId = tableId;
    }

    /**
     * Builds the BigTable {@link Scan} for the configured range in Longbow-plus (proto byte) form.
     *
     * <p>The scan covers the start-to-stop row range and reads only the default column family and
     * qualifier that hold the serialized Protobuf value.
     *
     * @return the assembled {@link Scan} request
     */
    @Override
    public Scan get() {
        Scan scan = setScanRange(startRow, stopRow);
        scan.addColumn(COLUMN_FAMILY_NAME, QUALIFIER_NAME);
        return scan;
    }

    /**
     * {@inheritDoc}
     *
     * @return the identifier of the table to scan
     */
    @Override
    public String getTableId() {
        return tableId;
    }
}
