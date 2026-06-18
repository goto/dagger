package com.gotocompany.dagger.core.processors.longbow.range;

import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;

import org.apache.flink.types.Row;

/**
 * Absolute range on Longbow.
 */
public class LongbowAbsoluteRange implements LongbowRange {
    /**
     * The Longbow schema used to compute absolute row keys from the input row.
     */
    private LongbowSchema longbowSchema;

    /**
     * Instantiates a new Longbow absolute range.
     *
     * @param longbowSchema the longbow schema
     */
    public LongbowAbsoluteRange(LongbowSchema longbowSchema) {
        this.longbowSchema = longbowSchema;
    }

    /**
     * Computes the upper-bound BigTable row key for the scan from the absolute latest timestamp.
     *
     * @param input the input row carrying the Longbow key and latest-timestamp field
     * @return the upper-bound row key as a byte array
     */
    @Override
    public byte[] getUpperBound(Row input) {
        return longbowSchema.getAbsoluteKey(input, (long) longbowSchema.getValue(input, Constants.LONGBOW_LATEST_KEY));
    }

    /**
     * Computes the lower-bound BigTable row key for the scan from the absolute earliest timestamp.
     *
     * @param input the input row carrying the Longbow key and earliest-timestamp field
     * @return the lower-bound row key as a byte array
     */
    @Override
    public byte[] getLowerBound(Row input) {
        return longbowSchema.getAbsoluteKey(input, (long) longbowSchema.getValue(input, Constants.LONGBOW_EARLIEST_KEY));
    }

    /**
     * Returns the fields that must not be present when an absolute range is used.
     *
     * @return an array containing the Longbow duration key, which is invalid for absolute ranges
     */
    @Override
    public String[] getInvalidFields() {
        return new String[]{Constants.LONGBOW_DURATION_KEY};
    }
}
