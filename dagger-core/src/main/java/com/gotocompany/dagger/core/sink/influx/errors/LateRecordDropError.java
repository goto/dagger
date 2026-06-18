package com.gotocompany.dagger.core.sink.influx.errors;

import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorStatsReporter;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.api.connector.sink.Sink.InitContext;
import org.apache.flink.metrics.Counter;

import org.influxdb.InfluxDBException;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The Late record drop error.
 */
public class LateRecordDropError implements InfluxError {
    /** Flink counter tracking the number of late records dropped by InfluxDB. */
    private final Counter counter;
    /** Logger used to record dropped points and counts for this error strategy. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LateRecordDropError.class.getName());
    /** Reporter used to publish the dropped-record error as a non-fatal exception. */
    private ErrorReporter errorStatsReporter;
    /** Message prefix that identifies an InfluxDB retention-policy late-drop error. */
    private static final String PREFIX = "{\"error\":\"partial write: points beyond retention policy dropped=";

    /**
     * Instantiates a new Late record drop error.
     *
     * @param initContext the context available in sink functions
     */
    public LateRecordDropError(InitContext initContext) {
        this.counter = initContext.metricGroup()
                .addGroup(Constants.SINK_INFLUX_LATE_RECORDS_DROPPED_KEY).counter("value");
        this.errorStatsReporter = new ErrorStatsReporter(initContext.metricGroup(),
                Constants.METRIC_TELEMETRY_SHUTDOWN_PERIOD_MS_DEFAULT);
    }

    /**
     * {@inheritDoc}
     *
     * <p>A dropped late record is not treated as an exception, so this always returns {@code false}.
     *
     * @return {@code false} always
     */
    @Override
    public boolean hasException() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>No exception is associated with dropped late records, so this always returns {@code null}.
     *
     * @return {@code null} always
     */
    @Override
    public IOException getCurrentException() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Matches when the throwable is an {@code InfluxDBException} reporting that points beyond the
     * retention policy were dropped.
     *
     * @param throwable the throwable raised while writing to InfluxDB
     * @return {@code true} if the throwable represents a late-record drop, {@code false} otherwise
     */
    @Override
    public boolean filterError(Throwable throwable) {
        return isLateDropping(throwable);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Increments the dropped-records counter by the parsed count, reports the throwable as a
     * non-fatal exception, and logs the failed points.
     *
     * @param points    the points that failed to be written
     * @param throwable the late-record drop error describing how many points were dropped
     */
    @Override
    public void handle(Iterable<Point> points, Throwable throwable) {
        reportDroppedPoints(parseDroppedPointsCount(throwable));
        errorStatsReporter.reportNonFatalException((Exception) throwable);
        logFailedPoints(points, LOGGER);
    }

    /**
     * Increments the dropped-records counter and logs the number of dropped points.
     *
     * @param numPoints the number of points that were dropped
     */
    private void reportDroppedPoints(int numPoints) {
        counter.inc(numPoints);
        LOGGER.warn("Numbers of Points Dropped :" + numPoints);
    }

    /**
     * Extracts the number of dropped points encoded in the InfluxDB error message.
     *
     * @param throwable the throwable whose message encodes the dropped-point count after an {@code =}
     * @return the parsed number of dropped points
     */
    private int parseDroppedPointsCount(Throwable throwable) {
        String[] split = throwable.getMessage().split("=");
        return Integer.parseInt(split[1].trim().replace("\"}", ""));
    }

    /**
     * Determines whether the given throwable is an InfluxDB retention-policy late-drop error.
     *
     * @param throwable the throwable to inspect
     * @return {@code true} if it is an {@code InfluxDBException} whose message starts with the drop prefix
     */
    private boolean isLateDropping(Throwable throwable) {
        return throwable instanceof InfluxDBException
                && throwable.getMessage().startsWith(PREFIX);
    }
}
