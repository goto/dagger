package com.gotocompany.dagger.core.metrics.reporters;

import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;

/**
 * The interface Error reporter.
 */
public interface ErrorReporter {
    /**
     * Report fatal exception.
     *
     * @param exception the exception
     */
    void reportFatalException(Exception exception);
    /**
     * Report non fatal exception.
     *
     * @param exception the exception
     */
    void reportNonFatalException(Exception exception);


    /**
     * Registers and returns a {@link Counter} that tracks occurrences of the given exception.
     *
     * <p>The counter is nested under {@code metricGroupKey} within the supplied
     * {@link MetricGroup} and is further keyed by the exception's fully-qualified class name,
     * allowing failures to be aggregated per exception type.
     *
     * @param exception      the exception whose occurrences are counted
     * @param metricGroup    the Flink metric group the counter is registered under
     * @param metricGroupKey the key used to group the exception counter
     * @return the counter tracking occurrences of the given exception type
     */
    default Counter addExceptionToCounter(Exception exception, MetricGroup metricGroup, String metricGroupKey) {
        return metricGroup.addGroup(metricGroupKey, exception.getClass().getName()).counter("value");
    }
}
