package com.gotocompany.dagger.core.processors.longbow.request;

import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;
import com.gotocompany.dagger.core.processors.longbow.storage.ScanRequest;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Map;

/**
 * Request scan the table.
 */
public class TableScanRequest implements ScanRequest {
    /**
     * Default BigTable column family, in bytes, scanned for Longbow data columns.
     */
    private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT);
    /**
     * Inclusive start row key of the scan range.
     */
    private byte[] startRow;
    /**
     * Inclusive stop row key of the scan range.
     */
    private byte[] stopRow;
    /**
     * Schema describing which Longbow data columns to read.
     */
    private LongbowSchema longbowSchema;
    /**
     * Identifier of the BigTable table to scan.
     */
    private String tableId;

    /**
     * Instantiates a new Table scan request.
     *
     * @param startRow      the start row
     * @param stopRow       the stop row
     * @param longbowSchema the longbow schema
     * @param tableId       the table id
     */
    public TableScanRequest(byte[] startRow, byte[] stopRow, LongbowSchema longbowSchema, String tableId) {
        this.startRow = startRow;
        this.stopRow = stopRow;
        this.longbowSchema = longbowSchema;
        this.tableId = tableId;
    }

    /**
     * Builds the BigTable {@link Scan} for the configured range in table (column-per-field) form.
     *
     * <p>The scan covers the start-to-stop row range and adds every Longbow data column from the
     * schema under the default column family.
     *
     * @return the assembled {@link Scan} request
     */
    @Override
    public Scan get() {
        Scan scan = setScanRange(startRow, stopRow);
        longbowSchema
                .getColumnNames(this::isLongbowData)
                .forEach(column -> scan.addColumn(COLUMN_FAMILY_NAME, Bytes.toBytes(column)));

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
