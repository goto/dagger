package com.gotocompany.dagger.core.processors.longbow.request;

import com.gotocompany.dagger.core.processors.longbow.storage.ScanRequest;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;
import com.gotocompany.dagger.core.processors.longbow.range.LongbowRange;

import org.apache.flink.types.Row;

import java.io.Serializable;

/**
 * The factory class for scan request.
 */
public class ScanRequestFactory implements Serializable {
    /**
     * Schema used to decide between the table and proto-byte scan representations.
     */
    private LongbowSchema longbowSchema;
    /**
     * Identifier of the BigTable table the created scan requests target.
     */
    private String tableId;

    /**
     * Instantiates a new Scan request factory.
     *
     * @param longbowSchema the longbow schema
     * @param tableId       the table id
     */
    public ScanRequestFactory(LongbowSchema longbowSchema, String tableId) {
        this.longbowSchema = longbowSchema;
        this.tableId = tableId;
    }

    /**
     * Create scan request.
     *
     * @param input        the input
     * @param longbowRange the longbow range
     * @return the scan request
     */
    public ScanRequest create(Row input, LongbowRange longbowRange) {
        if (!longbowSchema.isLongbowPlus()) {
            return new TableScanRequest(longbowRange.getUpperBound(input), longbowRange.getLowerBound(input), longbowSchema, tableId);
        } else {
            return new ProtoByteScanRequest(longbowRange.getUpperBound(input), longbowRange.getLowerBound(input), parseTableName(input));
        }
    }

    /**
     * Resolves the BigTable table name carried by the input row for Longbow-plus scans.
     *
     * @param input the input row holding the synchronizer-provided table id
     * @return the table name read from the {@code Constants.SYNCHRONIZER_BIGTABLE_TABLE_ID_KEY} column
     */
    private String parseTableName(Row input) {
        return (String) longbowSchema.getValue(input, Constants.SYNCHRONIZER_BIGTABLE_TABLE_ID_KEY);
    }
}
