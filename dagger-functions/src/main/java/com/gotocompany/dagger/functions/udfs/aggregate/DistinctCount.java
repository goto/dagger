package com.gotocompany.dagger.functions.udfs.aggregate;

import com.gotocompany.dagger.common.udfs.AggregateUdf;
import com.gotocompany.dagger.functions.udfs.aggregate.accumulator.DistinctCountAccumulator;

/**
 * User-defined aggregate function to get Distinct count.
 */
public class DistinctCount extends AggregateUdf<Integer, DistinctCountAccumulator> {

    /**
     * Creates a fresh, empty {@link DistinctCountAccumulator} for a new aggregation group.
     *
     * <p>Flink invokes this once per aggregation key to obtain the mutable state used to
     * track the set of distinct values seen for that group.
     *
     * @return a new, empty {@link DistinctCountAccumulator} instance
     */
    @Override
    public DistinctCountAccumulator createAccumulator() {
        return new DistinctCountAccumulator();
    }

    /**
     * Returns the number of distinct values accumulated so far.
     *
     * <p>Flink calls this to compute the final aggregation output from the accumulator state.
     *
     * @param distinctCountAccumulator the accumulator holding the distinct values
     * @return the count of distinct items recorded for the current group
     */
    @Override
    public Integer getValue(DistinctCountAccumulator distinctCountAccumulator) {
        return distinctCountAccumulator.count();
    }

    /**
     * returns distinct count of a field in input stream.
     *
     * @param distinctCountAccumulator the distinct count accumulator
     * @param item                     fieldName
     * @author prakhar.m
     */
    public void accumulate(DistinctCountAccumulator distinctCountAccumulator, String item) {
        if (item == null) {
            return;
        }
        distinctCountAccumulator.add(item);
    }

    /**
     * Merges the distinct values from other accumulators into the target accumulator.
     *
     * <p>Each {@link DistinctCountAccumulator} in {@code it} contributes its recorded items,
     * which are added to {@code distinctCountAccumulator}; duplicates are naturally removed
     * because the underlying storage is a set.
     *
     * @param distinctCountAccumulator the accumulator that receives the merged distinct values
     * @param it                       the other accumulators whose distinct values are merged in
     */
    public void merge(DistinctCountAccumulator distinctCountAccumulator, Iterable<DistinctCountAccumulator> it) {
        for (DistinctCountAccumulator distinctCountAccumulatorInstance : it) {
            distinctCountAccumulator.getDistinctItems().addAll(distinctCountAccumulatorInstance.getDistinctItems());
        }
    }
}
