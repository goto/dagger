package com.gotocompany.dagger.functions.udfs.aggregate;

import com.gotocompany.dagger.common.udfs.AggregateUdf;
import com.gotocompany.dagger.functions.udfs.aggregate.accumulator.ArrayAccumulator;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.annotation.InputGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * User-defined aggregate function to get the arraList of the objects.
 */
@FunctionHint(output = @DataTypeHint(value = "RAW", bridgedTo = ArrayList.class))
public class CollectArray extends AggregateUdf<List<Object>, ArrayAccumulator> {

    /**
     * Creates a fresh, empty {@link ArrayAccumulator} for a new aggregation group.
     *
     * <p>Flink invokes this method once per aggregation key to obtain the mutable state
     * into which input objects are folded by {@code accumulate}.
     *
     * @return a new, empty {@link ArrayAccumulator} instance
     */
    public ArrayAccumulator createAccumulator() {
        return new ArrayAccumulator();
    }

    /**
     * Return an arrayList of the objects passed.
     *
     * @param arrayAccumulator the array accumulator
     * @param obj              the obj
     * @return arrayOfObjects arrayList of all the passes objects
     * @author Rasyid
     * @team DE
     */
    public void accumulate(ArrayAccumulator arrayAccumulator, @DataTypeHint(inputGroup = InputGroup.ANY) Object obj) {
        arrayAccumulator.add(obj);
    }

    /**
     * Returns the aggregated result by emitting every object collected so far.
     *
     * <p>Flink calls this to compute the final output value of the aggregation from the
     * supplied accumulator state.
     *
     * @param arrayAccumulator the accumulator holding the collected objects
     * @return the list of all objects accumulated for the current group, in insertion order
     */
    public List<Object> getValue(ArrayAccumulator arrayAccumulator) {
        return arrayAccumulator.emit();
    }

    /**
     * Merges the objects collected by other accumulators into the target accumulator.
     *
     * <p>Flink uses this when partial aggregates computed in parallel (for example across
     * session windows or split groups) must be combined; every object from each accumulator
     * in {@code it} is appended to {@code arrayAccumulator}.
     *
     * @param arrayAccumulator the accumulator that receives the merged objects
     * @param it               the other accumulators whose collected objects are merged in
     */
    public void merge(ArrayAccumulator arrayAccumulator, Iterable<ArrayAccumulator> it) {
        for (ArrayAccumulator accumulatorInstance : it) {
            arrayAccumulator.getArrayList().addAll(accumulatorInstance.getArrayList());
        }
    }
}
