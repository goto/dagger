package com.gotocompany.dagger.functions.transformers.filter;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;

/**
 * The enum Filter aspects.
 */
public enum FilterAspects implements Aspects {
    /**
     * Filtered invalid records filter aspects.
     */
    FILTERED_INVALID_RECORDS("filtered_invalid_records", AspectType.Counter);

    /**
     * Creates a filter aspect with its metric name and aspect type.
     *
     * @param value the metric name reported for this aspect
     * @param type  the {@link AspectType} describing how this aspect is measured
     */
    FilterAspects(String value, AspectType type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Metric name reported for this aspect.
     */
    private String value;
    /**
     * Aspect type describing how this aspect is measured.
     */
    private AspectType type;

    /**
     * Returns the metric name reported for this filter aspect.
     *
     * @return the metric name of this filter aspect
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the aspect type describing how this filter aspect is measured.
     *
     * @return the {@link AspectType} of this filter aspect
     */
    @Override
    public AspectType getAspectType() {
        return this.type;
    }
}
