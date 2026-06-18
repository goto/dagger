package com.gotocompany.dagger.functions.udfs.aggregate.accumulator;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The accumulator for Percentile udf.
 */
public class PercentileAccumulator implements Serializable {

    /**
     * Backing list of sample values over which the percentile is computed.
     */
    private List<Double> dValueList = new ArrayList<>();

    /**
     * The requested percentile (for example {@code 95.0}) to evaluate over the samples.
     */
    private double percentile;

    /**
     * Add percentile.
     *
     * @param percentileValue   the percentileValue
     * @param dValue            the double value
     */
    public void add(double percentileValue, double dValue) {
        percentile = percentileValue;
        dValueList.add(dValue);
    }

    /**
     * Gets percentile value.
     *
     * @return the percentile value
     */
    public double getPercentileValue() {
        return new Percentile(this.percentile).
                evaluate(dValueList.stream().sorted().mapToDouble(Double::doubleValue).toArray(), 0, dValueList.size());
    }

    /**
     * Returns the backing list of sample values.
     *
     * <p>Primarily intended for state access during {@code merge} and for serialization.
     *
     * @return the mutable list of sample values
     */
    public List<Double> getdValueList() {
        return dValueList;
    }

    /**
     * Replaces the backing list of sample values.
     *
     * @param dValueList the list of sample values to use as the accumulator state
     */
    public void setdValueList(List<Double> dValueList) {
        this.dValueList = dValueList;
    }

    /**
     * Returns the requested percentile to be evaluated over the samples.
     *
     * @return the percentile value
     */
    public double getPercentile() {
        return percentile;
    }

    /**
     * Sets the requested percentile to be evaluated over the samples.
     *
     * @param percentile the percentile value to evaluate
     */
    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }
}

