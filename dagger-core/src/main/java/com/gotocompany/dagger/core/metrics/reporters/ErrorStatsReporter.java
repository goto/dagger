package com.gotocompany.dagger.core.metrics.reporters;

import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;

import com.gotocompany.dagger.core.processors.telemetry.processor.MetricsTelemetryExporter;
import com.gotocompany.dagger.core.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Error stats reporter.
 */
public class ErrorStatsReporter implements ErrorReporter {
    /**
     * The Flink metric group under which exception counters are registered.
     */
    private MetricGroup metricGroup;
    /**
     * The time in milliseconds to sleep after reporting a fatal exception, allowing the metric to
     * be flushed before the job shuts down.
     */
    private long shutDownPeriod;
    /**
     * Logger used to record interruptions that occur while waiting during fatal exception reporting.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsTelemetryExporter.class.getName());

    /**
     * Instantiates a new error stats reporter.
     *
     * @param metricGroup    the Flink metric group used to register exception counters
     * @param shutDownPeriod the time in milliseconds to wait after reporting a fatal exception
     */
    public ErrorStatsReporter(MetricGroup metricGroup, long shutDownPeriod) {
        this.metricGroup = metricGroup;
        this.shutDownPeriod = shutDownPeriod;
    }

    /**
     * Increments the fatal-exception counter for the given exception and then pauses for the
     * configured shutdown period.
     *
     * <p>The pause gives the metric reporter time to publish the recorded failure before the job
     * is torn down. An interruption during the wait is logged and otherwise ignored.
     *
     * @param exception the fatal exception to record
     */
    @Override
    public void reportFatalException(Exception exception) {
        Counter counter = addExceptionToCounter(exception, metricGroup, Constants.FATAL_EXCEPTION_METRIC_GROUP_KEY);
        counter.inc();
        try {
            Thread.sleep(shutDownPeriod);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Increments the non-fatal-exception counter for the given exception.
     *
     * @param exception the non-fatal exception to record
     */
    @Override
    public void reportNonFatalException(Exception exception) {
        Counter counter = addExceptionToCounter(exception, metricGroup, Constants.NONFATAL_EXCEPTION_METRIC_GROUP_KEY);
        counter.inc();
    }
}
