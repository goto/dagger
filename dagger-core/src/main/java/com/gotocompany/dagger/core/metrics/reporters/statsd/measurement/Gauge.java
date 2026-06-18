package com.gotocompany.dagger.core.metrics.reporters.statsd.measurement;

import com.gotocompany.dagger.common.metrics.aspects.Aspects;

import java.io.Serializable;

/**
 * Abstraction for emitting gauge metrics (point-in-time values) for a Dagger {@link Aspects} aspect.
 *
 * <p>A gauge reports the latest value of some quantity rather than a cumulative total, for example a
 * current lag, queue depth, or watermark. The interface extends {@link Serializable} so it can be
 * embedded in Flink operators shipped with the job graph.
 *
 * @see com.gotocompany.dagger.core.metrics.reporters.statsd.manager.DaggerGaugeManager
 */
public interface Gauge extends Serializable {
    /**
     * Reports the current value of the given aspect's gauge.
     *
     * @param aspect     the metric aspect identifying which gauge to update
     * @param gaugeValue the current value to report
     */
    void markValue(Aspects aspect, int gaugeValue);
}
