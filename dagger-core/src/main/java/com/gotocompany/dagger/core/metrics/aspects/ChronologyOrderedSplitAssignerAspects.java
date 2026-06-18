package com.gotocompany.dagger.core.metrics.aspects;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;

/**
 * Metric aspects emitted by the chronology-ordered split assigner of the Parquet/file source.
 *
 * <p>The chronology-ordered split assigner hands {@code FileSourceSplit}s to Flink source readers in
 * timestamp order. These aspects expose how many splits have been discovered, recorded as pending
 * work, and are still awaiting assignment; they are published through the StatsD-backed gauge manager
 * under the split-assigner component tag.
 */
public enum ChronologyOrderedSplitAssignerAspects implements Aspects {
    /**
     * Gauge tracking the total number of file splits discovered and handed to the assigner.
     */
    TOTAL_SPLITS_DISCOVERED("total_splits_discovered", AspectType.Gauge),
    /**
     * Gauge tracking the number of splits the assigner has recorded as pending work.
     */
    TOTAL_SPLITS_RECORDED("total_splits_recorded", AspectType.Gauge),
    /**
     * Tracks how many recorded splits are still waiting to be assigned to a source reader.
     */
    SPLITS_AWAITING_ASSIGNMENT("splits_awaiting_assignment", AspectType.Counter);

    /**
     * Binds the metric name and metric type for this aspect constant.
     *
     * @param value      the metric name to publish on the Flink metric group
     * @param aspectType the kind of metric this aspect is reported as
     */
    ChronologyOrderedSplitAssignerAspects(String value, AspectType aspectType) {
        this.value = value;
        this.aspectType = aspectType;
    }

    /** The metric name published for this aspect on the Flink metric group. */
    private final String value;
    /** The kind of metric this aspect is reported as; see {@link AspectType}. */
    private final AspectType aspectType;

    /**
     * {@inheritDoc}
     *
     * <p>Returns the metric name configured for this constant (the first constructor argument).
     *
     * @return the metric name for this aspect
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the {@link AspectType} configured for this constant, which controls whether it is
     * registered and reported as a meter, histogram, gauge, or counter.
     *
     * @return the metric type for this aspect
     */
    @Override
    public AspectType getAspectType() {
        return aspectType;
    }
}
