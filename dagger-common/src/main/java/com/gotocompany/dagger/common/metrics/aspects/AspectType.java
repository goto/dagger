package com.gotocompany.dagger.common.metrics.aspects;

/**
 * The enum Aspect type.
 */
public enum AspectType {
    /** Aspect reported as a point-in-time gauge value. */
    Gauge,
    /** Aspect reported as a distribution of values via a histogram. */
    Histogram,
    /** Aspect reported as a metered rate of events. */
    Metric,
    /** Aspect reported as a monotonically increasing counter. */
    Counter
}
