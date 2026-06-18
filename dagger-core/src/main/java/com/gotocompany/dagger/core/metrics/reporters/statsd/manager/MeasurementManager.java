package com.gotocompany.dagger.core.metrics.reporters.statsd.manager;

import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.StatsDTag;

import java.io.Serializable;

/**
 * Common contract for StatsD measurement managers that publish a particular kind of metric.
 *
 * <p>Implementations such as {@link DaggerCounterManager}, {@link DaggerGaugeManager}, and
 * {@link DaggerHistogramManager} wrap the shared depot StatsD reporter together with a fixed set of
 * tags. The tags are supplied once via {@link #register(StatsDTag[])}; afterwards every metric the
 * manager emits is decorated with them. Extending {@link Serializable} allows managers to be
 * embedded in Flink operators and shipped with the job graph.
 */
public interface MeasurementManager extends Serializable {
    /**
     * Registers the tags that will be attached to every metric this manager subsequently emits.
     *
     * <p>Typically called once during operator setup with the component- and instance-specific tags;
     * implementations convert them into StatsD {@code key=value} strings and retain them for reuse.
     *
     * @param tags the tags to attach to all measurements; must not be {@code null}
     */
    void register(StatsDTag[] tags);
}
