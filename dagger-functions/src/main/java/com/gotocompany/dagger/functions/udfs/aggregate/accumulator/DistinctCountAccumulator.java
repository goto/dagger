package com.gotocompany.dagger.functions.udfs.aggregate.accumulator;

import org.apache.flink.table.annotation.DataTypeHint;

import java.io.Serializable;
import java.util.HashSet;

/**
 * The accumulator for DistinctCount udf.
 */
public class DistinctCountAccumulator implements Serializable {

    /**
     * Backing set holding the distinct values observed by the owning aggregation.
     */
    private @DataTypeHint("RAW") HashSet<String> distinctItems = new HashSet<>();

    /**
     * Count size of the distinct items.
     *
     * @return the size of distinct items
     */
    public int count() {
        return distinctItems.size();
    }

    /**
     * Add item.
     *
     * @param item the item
     */
    public void add(String item) {
        distinctItems.add(item);
    }

    /**
     * Returns the backing set of distinct values.
     *
     * <p>Primarily intended for state access during {@code merge} and for serialization.
     *
     * @return the mutable set of distinct items
     */
    public HashSet<String> getDistinctItems() {
        return distinctItems;
    }

    /**
     * Replaces the backing set of distinct values.
     *
     * @param distinctItems the set of distinct items to use as the accumulator state
     */
    public void setDistinctItems(HashSet<String> distinctItems) {
        this.distinctItems = distinctItems;
    }
}
