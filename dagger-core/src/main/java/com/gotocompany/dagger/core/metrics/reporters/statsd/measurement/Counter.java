package com.gotocompany.dagger.core.metrics.reporters.statsd.measurement;

import com.gotocompany.dagger.common.metrics.aspects.Aspects;

import java.io.Serializable;

/**
 * Abstraction for emitting counter metrics for a Dagger {@link Aspects} aspect.
 *
 * <p>Counters accumulate deltas over time and are typically used to count events such as records
 * processed, errors, or retries. Implementations may increase or decrease the count by one or by an
 * explicit amount. The interface extends {@link Serializable} so it can travel with the Flink job
 * graph.
 *
 * @see com.gotocompany.dagger.core.metrics.reporters.statsd.manager.DaggerCounterManager
 */
public interface Counter extends Serializable {
    /**
     * Increases the aspect's counter by one.
     *
     * @param aspect the metric aspect to increment
     */
    void increment(Aspects aspect);

    /**
     * Increases the aspect's counter by the given amount.
     *
     * @param aspect the metric aspect to increment
     * @param num    the delta to add to the counter
     */
    void increment(Aspects aspect, long num);

    /**
     * Decreases the aspect's counter by one.
     *
     * @param aspect the metric aspect to decrement
     */
    void decrement(Aspects aspect);

    /**
     * Decreases the aspect's counter by the given amount.
     *
     * @param aspect the metric aspect to decrement
     * @param num    the delta to apply to the counter
     */
    void decrement(Aspects aspect, long num);
}
