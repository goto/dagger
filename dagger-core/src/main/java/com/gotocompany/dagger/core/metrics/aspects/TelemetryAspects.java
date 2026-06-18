package com.gotocompany.dagger.core.metrics.aspects;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;

/**
 * The enum Telemetry aspects.
 */
public enum TelemetryAspects implements Aspects {
    /**
     * Value telemetry aspects.
     */
    VALUE("value", AspectType.Metric);

    /**
     * The metric identifier reported for this aspect.
     */
    private String value;
    /**
     * The metric type this aspect represents.
     */
    private AspectType aspectType;

    /**
     * Instantiates a new telemetry aspect.
     *
     * @param value      the metric identifier for this aspect
     * @param aspectType the metric type this aspect represents
     */
    TelemetryAspects(String value, AspectType aspectType) {
        this.value = value;
        this.aspectType = aspectType;
    }

    /**
     * Returns the metric identifier reported for this aspect.
     *
     * @return the metric identifier configured for this aspect
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Returns the metric type associated with this aspect.
     *
     * @return the metric type configured for this aspect
     */
    @Override
    public AspectType getAspectType() {
        return aspectType;
    }
}
