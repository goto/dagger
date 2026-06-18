package com.gotocompany.dagger.core.metrics.reporters.statsd.manager;

import com.gotocompany.dagger.common.metrics.aspects.Aspects;
import com.gotocompany.dagger.core.metrics.reporters.statsd.measurement.Counter;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.StatsDTag;
import com.gotocompany.depot.metrics.StatsDReporter;

import java.util.ArrayList;

/**
 * StatsD-backed {@link Counter} that emits count deltas for an aspect under a fixed set of tags.
 *
 * <p>After the tags have been supplied via {@link #register(StatsDTag[])}, the increment/decrement
 * methods publish positive or negative deltas for an aspect's metric through the shared depot
 * {@link StatsDReporter}. It is used throughout Dagger to count events such as processed records,
 * errors, and retries.
 */
public class DaggerCounterManager implements MeasurementManager, Counter {
    /** Shared depot reporter used to publish count deltas. */
    private final StatsDReporter statsDReporter;
    /** Tags, in StatsD {@code key=value} form, attached to every delta; populated by {@link #register(StatsDTag[])}. */
    private String[] formattedTags;

    /**
     * Creates a counter manager backed by the reporter produced by the given supplier.
     *
     * @param statsDReporterSupplier serializable supplier of the shared depot StatsD reporter
     */
    public DaggerCounterManager(SerializedStatsDReporterSupplier statsDReporterSupplier) {
        this.statsDReporter = statsDReporterSupplier.buildStatsDReporter();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Converts each {@link StatsDTag} into its formatted {@code key=value} representation and
     * caches the resulting array for use on every subsequent delta.
     *
     * @param tags the tags to attach to all counter updates
     */
    @Override
    public void register(StatsDTag[] tags) {
        ArrayList<String> tagList = new ArrayList<>();
        for (StatsDTag measurementTag : tags) {
            tagList.add(measurementTag.getFormattedTag());
        }
        this.formattedTags = tagList.toArray(new String[0]);
    }

    /**
     * Increments the aspect's counter by one.
     *
     * @param aspect the metric aspect to increment
     */
    @Override
    public void increment(Aspects aspect) {
        increment(aspect, 1L);
    }

    /**
     * Increments the aspect's counter by the given amount.
     *
     * @param aspect        the metric aspect whose {@code getValue()} supplies the StatsD metric name
     * @param positiveCount the (positive) delta to add to the counter
     */
    @Override
    public void increment(Aspects aspect, long positiveCount) {
        statsDReporter.captureCount(aspect.getValue(), positiveCount, formattedTags);
    }

    /**
     * Decrements the aspect's counter by one.
     *
     * @param aspect the metric aspect to decrement
     */
    @Override
    public void decrement(Aspects aspect) {
        decrement(aspect, -1L);
    }

    /**
     * Records the given delta against the aspect's counter.
     *
     * <p>The value is forwarded to the reporter unchanged, so callers must pass a negative number to
     * actually decrease the count.
     *
     * @param aspect        the metric aspect whose {@code getValue()} supplies the StatsD metric name
     * @param negativeCount the delta to apply; expected to be negative for a true decrement
     */
    @Override
    public void decrement(Aspects aspect, long negativeCount) {
        statsDReporter.captureCount(aspect.getValue(), negativeCount, formattedTags);
    }
}
