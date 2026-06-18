package com.gotocompany.dagger.core.processors.longbow.range;

import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;

import org.apache.flink.types.Row;

/**
 * Duration range on Longbow.
 */
public class LongbowDurationRange implements LongbowRange {
    /**
     * The Longbow schema used to compute row keys and resolve the lookback duration.
     */
    private LongbowSchema longbowSchema;

    /**
     * Instantiates a new Longbow duration range.
     *
     * @param longbowSchema the longbow schema
     */
    public LongbowDurationRange(LongbowSchema longbowSchema) {
        this.longbowSchema = longbowSchema;
    }

    /**
     * Computes the upper-bound BigTable row key for the scan, anchored at the row's event time.
     *
     * @param input the input row carrying the Longbow key and rowtime field
     * @return the upper-bound row key as a byte array
     */
    @Override
    public byte[] getUpperBound(Row input) {
        return longbowSchema.getKey(input, 0);
    }

    /**
     * Computes the lower-bound BigTable row key for the scan by subtracting the configured duration
     * from the row's event time.
     *
     * @param input the input row carrying the Longbow key, rowtime and duration fields
     * @return the lower-bound row key as a byte array
     */
    @Override
    public byte[] getLowerBound(Row input) {
        return longbowSchema.getKey(input, longbowSchema.getDurationInMillis(input));
    }

    /**
     * Returns the fields that must not be present when a duration range is used.
     *
     * @return an array containing the Longbow earliest and latest keys, which are invalid for
     *         duration ranges
     */
    @Override
    public String[] getInvalidFields() {
        return new String[]{Constants.LONGBOW_EARLIEST_KEY, Constants.LONGBOW_LATEST_KEY};
    }
}
