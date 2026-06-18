package com.gotocompany.dagger.common.metrics.managers;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;
import org.apache.flink.dropwizard.metrics.DropwizardHistogramWrapper;
import org.apache.flink.dropwizard.metrics.DropwizardMeterWrapper;
import org.apache.flink.metrics.Histogram;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.MetricGroup;

import com.codahale.metrics.SlidingTimeWindowReservoir;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.gotocompany.dagger.common.core.Constants.SLIDING_TIME_WINDOW;


/**
 * The Meter stats manager.
 */
public class MeterStatsManager {
    /** Histograms keyed by aspect, used to record value distributions over a sliding time window. */
    private final HashMap<Aspects, Histogram> histogramMap;
    /** Whether metric registration and updates are enabled; when {@code false} calls are no-ops. */
    private Boolean enabled;
    /** Meters keyed by aspect, used to record event rates. */
    private HashMap<Aspects, Meter> meterMap;
    /** The Flink metric group under which histograms and meters are registered. */
    private MetricGroup metricGroup;

    /**
     * Instantiates a new Meter stats manager.
     *
     * @param metricGroup the metric group
     * @param enabled     the enabled
     */
    public MeterStatsManager(MetricGroup metricGroup, Boolean enabled) {
        this.metricGroup = metricGroup;
        this.enabled = enabled;
        histogramMap = new HashMap<>();
        meterMap = new HashMap<>();
    }

    /**
     * Instantiates a new Meter stats manager.
     *
     * @param metricGroup  the metric group
     * @param enabled      the enabled
     * @param histogramMap the histogram map
     * @param meterMap     the meter map
     */
    public MeterStatsManager(MetricGroup metricGroup, Boolean enabled, HashMap histogramMap, HashMap meterMap) {
        this.metricGroup = metricGroup;
        this.enabled = enabled;
        this.histogramMap = histogramMap;
        this.meterMap = meterMap;
    }

    /**
     * Register aspects.
     *
     * @param groupName the group name
     * @param aspects   the aspects
     */
    public void register(String groupName, Aspects[] aspects) {
        if (enabled) {
            register(metricGroup.addGroup(groupName), aspects);
        }
    }

    /**
     * Creates a Dropwizard-compatible histogram backed by a sliding time window reservoir.
     *
     * @return a new {@code com.codahale.metrics.Histogram} that retains samples for the
     *         configured {@code SLIDING_TIME_WINDOW} number of seconds
     */
    private com.codahale.metrics.Histogram getHistogram() {
        return new com.codahale.metrics.Histogram(new SlidingTimeWindowReservoir(SLIDING_TIME_WINDOW, TimeUnit.SECONDS));
    }

    /**
     * Update histogram.
     *
     * @param aspects the aspects
     * @param value   the value
     */
    public void updateHistogram(Aspects aspects, long value) {
        if (enabled) {
            histogramMap.get(aspects).update(value);
        }
    }

    /**
     * Mark event.
     *
     * @param aspect the aspect
     */
    public void markEvent(Aspects aspect) {
        if (enabled) {
            meterMap.get(aspect).markEvent();
        }
    }

    /**
     * Register aspects with specified group key and group value pair.
     *
     * @param groupKey   the group key
     * @param groupValue the group value
     * @param aspects    the aspects
     */
    public void register(String groupKey, String groupValue, Aspects[] aspects) {
        if (enabled) {
            register(metricGroup.addGroup(groupKey, groupValue), aspects);
        }
    }

    /**
     * Registers the given aspects against the supplied metric group.
     *
     * <p>For each aspect, a Dropwizard-backed histogram is created when its
     * {@link AspectType} is {@link AspectType#Histogram}, and a meter is created when it is
     * {@link AspectType#Metric}; aspects of any other type are ignored.
     *
     * @param group   the Flink metric group to register the metrics under
     * @param aspects the aspects to register
     */
    private void register(MetricGroup group, Aspects[] aspects) {
        for (Aspects aspect : aspects) {
            if (AspectType.Histogram.equals(aspect.getAspectType())) {
                histogramMap.put(aspect, group.histogram(aspect.getValue(), new DropwizardHistogramWrapper(getHistogram())));
            }
            if (AspectType.Metric.equals(aspect.getAspectType())) {
                meterMap.put(aspect, group.meter(aspect.getValue(), new DropwizardMeterWrapper(new com.codahale.metrics.Meter())));
            }
        }
    }
}
