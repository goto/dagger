package com.gotocompany.dagger.core.metrics.reporters.statsd.measurement;

import com.gotocompany.dagger.common.metrics.aspects.Aspects;

import java.io.Serializable;

/**
 * Abstraction for emitting histogram (distribution) metrics for a Dagger {@link Aspects} aspect.
 *
 * <p>Implementations record individual samples that the metrics backend aggregates into a
 * distribution (percentiles, min/max, and so on). The interface extends {@link Serializable} so it
 * can be carried inside Flink operators as part of the job graph.
 *
 * @see com.gotocompany.dagger.core.metrics.reporters.statsd.manager.DaggerHistogramManager
 */
public interface Histogram extends Serializable {
    /**
     * Records a single sample for the given aspect.
     *
     * @param aspect the metric aspect identifying which histogram to update
     * @param value  the sample value to add to the distribution
     */
    void recordValue(Aspects aspect, long value);
}
