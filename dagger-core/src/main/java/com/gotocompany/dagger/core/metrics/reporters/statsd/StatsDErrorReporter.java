package com.gotocompany.dagger.core.metrics.reporters.statsd;

import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.StatsDTag;
import com.gotocompany.depot.metrics.StatsDReporter;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;

import java.io.Serializable;

import static com.gotocompany.dagger.core.utils.Constants.FATAL_EXCEPTION_METRIC_GROUP_KEY;
import static com.gotocompany.dagger.core.utils.Constants.NONFATAL_EXCEPTION_METRIC_GROUP_KEY;

/**
 * {@link ErrorReporter} implementation that publishes job exceptions to StatsD as counters.
 *
 * <p>This reporter is the StatsD-backed error channel for a Dagger Flink job. Whenever the pipeline
 * encounters a fatal or non-fatal exception, the corresponding {@code report*} method emits a count
 * of {@code 1} on a dedicated metric, tagged with the fully-qualified class name of the offending
 * exception so failures can be grouped by type in the metrics backend. Reporting is delegated to a
 * depot {@link StatsDReporter} obtained from a {@link SerializedStatsDReporterSupplier}, which keeps
 * this class {@link Serializable} so it can be shipped as part of the Flink job graph.
 */
public class StatsDErrorReporter implements ErrorReporter, Serializable {
    /** Tag key under which a fatal exception's class name is published. */
    private static final String FATAL_EXCEPTION_TAG_KEY = "fatal_exception_type";
    /** Tag key under which a non-fatal exception's class name is published. */
    private static final String NON_FATAL_EXCEPTION_TAG_KEY = "non_fatal_exception_type";
    /** Shared depot reporter used to emit the StatsD counts; obtained from the supplier. */
    private final StatsDReporter statsDReporter;

    /**
     * Creates a reporter backed by the StatsD reporter produced by the given supplier.
     *
     * @param statsDReporterSupplier serializable supplier whose {@code buildStatsDReporter()} is
     *                               invoked once to obtain the underlying depot reporter
     */
    public StatsDErrorReporter(SerializedStatsDReporterSupplier statsDReporterSupplier) {
        this.statsDReporter = statsDReporterSupplier.buildStatsDReporter();
    }

    /**
     * Records a fatal exception by emitting a StatsD count of {@code 1} on the fatal-exception
     * metric.
     *
     * <p>The exception's fully-qualified class name is attached as the value of the
     * {@code fatal_exception_type} tag, allowing fatal failures to be aggregated by type in the
     * metrics backend.
     *
     * @param exception the fatal exception whose type is reported
     */
    @Override
    public void reportFatalException(Exception exception) {
        StatsDTag statsDTag = new StatsDTag(FATAL_EXCEPTION_TAG_KEY, exception.getClass().getName());
        statsDReporter.captureCount(FATAL_EXCEPTION_METRIC_GROUP_KEY, 1L, statsDTag.getFormattedTag());
    }

    /**
     * Records a non-fatal exception by emitting a StatsD count of {@code 1} on the
     * non-fatal-exception metric.
     *
     * <p>The exception's fully-qualified class name is attached as the value of the
     * {@code non_fatal_exception_type} tag, allowing recoverable failures to be aggregated by type
     * in the metrics backend.
     *
     * @param exception the non-fatal exception whose type is reported
     */
    @Override
    public void reportNonFatalException(Exception exception) {
        StatsDTag statsDTag = new StatsDTag(NON_FATAL_EXCEPTION_TAG_KEY, exception.getClass().getName());
        statsDReporter.captureCount(NONFATAL_EXCEPTION_METRIC_GROUP_KEY, 1L, statsDTag.getFormattedTag());
    }

    /**
     * Unsupported for the StatsD reporter.
     *
     * <p>Unlike the default {@link ErrorReporter} behavior, this implementation does not register
     * exceptions against a Flink {@link MetricGroup}; all reporting happens through the depot
     * {@link StatsDReporter} instead. Callers should use {@link #reportFatalException(Exception)} or
     * {@link #reportNonFatalException(Exception)}.
     *
     * @param exception      the exception that would be counted; ignored
     * @param metricGroup    the Flink metric group; ignored
     * @param metricGroupKey the metric group key; ignored
     * @return never returns normally
     * @throws UnsupportedOperationException always, because this operation is not supported here
     */
    @Override
    public Counter addExceptionToCounter(Exception exception, MetricGroup metricGroup, String metricGroupKey) {
        throw new UnsupportedOperationException("This operation is not supported on StatsDErrorReporter");
    }
}
