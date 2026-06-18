package com.gotocompany.dagger.core.metrics.reporters.statsd.manager;

import com.gotocompany.dagger.common.metrics.aspects.Aspects;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.measurement.Histogram;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.StatsDTag;
import com.gotocompany.depot.metrics.StatsDReporter;

import java.util.ArrayList;

/**
 * StatsD-backed {@link Histogram} that records distributions of {@code long} samples under a fixed
 * set of tags.
 *
 * <p>Once the tags have been supplied via {@link #register(StatsDTag[])}, each call to
 * {@link #recordValue(Aspects, long)} forwards the sample to the shared depot {@link StatsDReporter}
 * through its histogram API. It is commonly used for latency and size distributions within the
 * Dagger pipeline.
 */
public class DaggerHistogramManager implements MeasurementManager, Histogram {
    /** Shared depot reporter used to publish histogram samples. */
    private final StatsDReporter statsDReporter;
    /** Tags, in StatsD {@code key=value} form, attached to every sample; populated by {@link #register(StatsDTag[])}. */
    private String[] formattedTags;

    /**
     * Creates a histogram manager backed by the reporter produced by the given supplier.
     *
     * @param statsDReporterSupplier serializable supplier of the shared depot StatsD reporter
     */
    public DaggerHistogramManager(SerializedStatsDReporterSupplier statsDReporterSupplier) {
        this.statsDReporter = statsDReporterSupplier.buildStatsDReporter();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Converts each {@link StatsDTag} into its formatted {@code key=value} representation and
     * caches the resulting array for use on every subsequent sample.
     *
     * @param tags the tags to attach to all recorded samples
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
     * Records a single histogram sample for the given aspect together with the registered tags.
     *
     * @param aspect the metric aspect whose {@code getValue()} supplies the StatsD metric name
     * @param value  the sample value to add to the distribution
     */
    @Override
    public void recordValue(Aspects aspect, long value) {
        statsDReporter.captureHistogram(aspect.getValue(), value, formattedTags);
    }
}
