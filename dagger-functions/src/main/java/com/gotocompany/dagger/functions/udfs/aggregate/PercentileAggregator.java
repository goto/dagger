package com.gotocompany.dagger.functions.udfs.aggregate;


import com.gotocompany.dagger.common.udfs.AggregateUdf;
import com.gotocompany.dagger.functions.udfs.aggregate.accumulator.PercentileAccumulator;
import org.apache.flink.table.annotation.DataTypeHint;

import java.math.BigDecimal;


/**
 * User-defined aggregate function to get Percentile.
 *
 * @author lavkesh.lahngir
 * @team lens @go-jek.com
 */
public class PercentileAggregator extends AggregateUdf<Double, PercentileAccumulator> {

    /**
     * Creates a fresh, empty {@link PercentileAccumulator} for a new aggregation group.
     *
     * <p>Flink invokes this once per aggregation key to obtain the mutable state used to
     * collect the sample values and the requested percentile.
     *
     * @return a new, empty {@link PercentileAccumulator} instance
     */
    @Override
    public PercentileAccumulator createAccumulator() {
        return new PercentileAccumulator();
    }

    /**
     * Returns the computed percentile over all values accumulated so far.
     *
     * <p>Flink calls this to produce the final aggregation output from the accumulator state.
     *
     * @param acc the accumulator holding the sample values and the requested percentile
     * @return the percentile value computed from the accumulated samples
     */
    @Override
    public Double getValue(PercentileAccumulator acc) {
        return acc.getPercentileValue();
    }

    /**
     * Accumulate.
     *
     * @param acc        the acc
     * @param percentile the percentile
     * @param dValue     the d value
     */
    public void accumulate(PercentileAccumulator acc, @DataTypeHint("DECIMAL(30, 3)") BigDecimal percentile, @DataTypeHint("DECIMAL(30, 3)") BigDecimal dValue) {
        acc.add(percentile.doubleValue(), dValue.doubleValue());
    }

    /**
     * Merges the sample values from other accumulators into the target accumulator.
     *
     * <p>The double values held by each {@link PercentileAccumulator} in {@code otherAccumulators}
     * are appended to {@code percentileAccumulator}, and the requested percentile is carried over so
     * the combined accumulator can compute the percentile across all partial samples.
     *
     * @param percentileAccumulator the accumulator that receives the merged sample values
     * @param otherAccumulators     the other accumulators whose sample values are merged in
     */
    public void merge(PercentileAccumulator percentileAccumulator, Iterable<PercentileAccumulator> otherAccumulators) {
        for (PercentileAccumulator accumulatorInstance : otherAccumulators) {
            percentileAccumulator.getdValueList().addAll(accumulatorInstance.getdValueList());
            percentileAccumulator.setPercentile(accumulatorInstance.getPercentile());
        }
    }
}

