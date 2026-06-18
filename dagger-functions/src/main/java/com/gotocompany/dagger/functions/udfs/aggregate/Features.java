package com.gotocompany.dagger.functions.udfs.aggregate;

import com.gotocompany.dagger.common.udfs.AggregateUdf;
import com.gotocompany.dagger.functions.exceptions.OddNumberOfArgumentsException;
import com.gotocompany.dagger.functions.udfs.aggregate.accumulator.FeatureAccumulator;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.annotation.InputGroup;
import org.apache.flink.types.Row;

/**
 * User-defined aggregate function to get Features.
 */
@FunctionHint(output = @DataTypeHint("RAW"))
public class Features extends AggregateUdf<Row[], FeatureAccumulator> {

    /**
     * Creates a fresh, empty {@link FeatureAccumulator} for a new aggregation group.
     *
     * <p>Flink invokes this once per aggregation key to obtain the mutable state used to
     * collect key/value feature pairs.
     *
     * @return a new, empty {@link FeatureAccumulator} instance
     */
    @Override
    public FeatureAccumulator createAccumulator() {
        return new FeatureAccumulator();
    }

    /**
     * Returns the accumulated features as an array of Feast feature {@link Row} values.
     *
     * <p>Flink calls this to produce the final aggregation output, converting every collected
     * key/value pair into its {@link Row} representation.
     *
     * @param featureAccumulator the accumulator holding the collected feature pairs
     * @return an array of {@link Row} values, one per accumulated feature
     */
    @Override
    public Row[] getValue(FeatureAccumulator featureAccumulator) {
        return featureAccumulator.getFeaturesAsRows();
    }

    /**
     * Converts the given list of objects to a FeatureRow type to store in feast(https://github.com/feast-dev/feast)
     * with key and values from every even pairs passed in args.
     *
     * @param featureAccumulator the feature accumulator
     * @param objects            the objects as arguments
     * @return features the output in FeatureRow for every even pairs
     * @author zhilingc
     * @team DS
     */
    public void accumulate(FeatureAccumulator featureAccumulator, @DataTypeHint(inputGroup = InputGroup.ANY) Object... objects) {
        if (objects.length % 2 != 0) {
            throw new OddNumberOfArgumentsException();
        }
        for (int elementIndex = 0; elementIndex < objects.length; elementIndex += 2) {
            featureAccumulator.add(String.valueOf(objects[elementIndex]), objects[elementIndex + 1]);
        }
    }

    /**
     * Merges the features collected by other accumulators into the target accumulator.
     *
     * <p>Every feature pair from each {@link FeatureAccumulator} in {@code it} is appended to
     * {@code featureAccumulator}, combining partial aggregates produced in parallel.
     *
     * @param featureAccumulator the accumulator that receives the merged features
     * @param it                 the other accumulators whose features are merged in
     */
    public void merge(FeatureAccumulator featureAccumulator, Iterable<FeatureAccumulator> it) {
        for (FeatureAccumulator accumulatorInstance : it) {
            featureAccumulator.getFeatures().addAll(accumulatorInstance.getFeatures());
        }
    }
}
