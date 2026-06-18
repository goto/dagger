package com.gotocompany.dagger.core.metrics.reporters.statsd.manager;

import com.gotocompany.dagger.common.metrics.aspects.Aspects;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.measurement.Gauge;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.StatsDTag;
import com.gotocompany.depot.metrics.StatsDReporter;

import java.util.ArrayList;

/**
 * StatsD-backed {@link Gauge} that reports point-in-time integer values decorated with a fixed set
 * of tags.
 *
 * <p>Once the tags have been supplied via {@link #register(StatsDTag[])}, each call to
 * {@link #markValue(Aspects, int)} publishes the value under the aspect's metric name through the
 * shared depot {@link StatsDReporter}. It is used across Dagger to surface current-state signals
 * such as watermarks, consumer lag, or open-connection counts.
 */
public class DaggerGaugeManager implements MeasurementManager, Gauge {
    /** Shared depot reporter used to publish gauge values. */
    private final StatsDReporter statsDReporter;
    /** Tags, in StatsD {@code key=value} form, attached to every gauge; populated by {@link #register(StatsDTag[])}. */
    private String[] formattedTags;

    /**
     * Creates a gauge manager backed by the reporter produced by the given supplier.
     *
     * @param statsDReporterSupplier serializable supplier of the shared depot StatsD reporter
     */
    public DaggerGaugeManager(SerializedStatsDReporterSupplier statsDReporterSupplier) {
        this.statsDReporter = statsDReporterSupplier.buildStatsDReporter();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Converts each {@link StatsDTag} into its formatted {@code key=value} representation and
     * caches the resulting array for use on every subsequent gauge emission.
     *
     * @param tags the tags to attach to all gauge values
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
     * Publishes a gauge reading for the given aspect together with the registered tags.
     *
     * @param aspect     the metric aspect whose {@code getValue()} supplies the StatsD metric name
     * @param gaugeValue the current value to report
     */
    @Override
    public void markValue(Aspects aspect, int gaugeValue) {
        statsDReporter.gauge(aspect.getValue(), gaugeValue, formattedTags);
    }
}
