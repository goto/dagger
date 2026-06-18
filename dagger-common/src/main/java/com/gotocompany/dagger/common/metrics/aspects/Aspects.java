package com.gotocompany.dagger.common.metrics.aspects;

/**
 * The interface for aspects.
 */
public interface Aspects {
    /**
     * Returns the metric name used when registering this aspect with a metric group.
     *
     * @return the metric name of the aspect
     */
    String getValue();

    /**
     * Returns the kind of metric this aspect represents.
     *
     * @return the {@link AspectType} of the aspect
     */
    AspectType getAspectType();
}
