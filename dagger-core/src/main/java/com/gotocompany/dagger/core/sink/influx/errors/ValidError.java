package com.gotocompany.dagger.core.sink.influx.errors;

import com.gotocompany.dagger.core.exception.InfluxWriteException;
import com.gotocompany.dagger.core.sink.influx.InfluxDBSink;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The Valid error.
 */
public class ValidError implements InfluxError {

    /** Logger used to record the points that failed to be written. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBSink.class.getName());
    /** The wrapped write error captured for the most recently handled failure. */
    private IOException error;

    /**
     * {@inheritDoc}
     *
     * <p>This strategy always represents an error condition, so it returns {@code true}.
     *
     * @return {@code true} always
     */
    @Override
    public boolean hasException() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the error captured by the most recent call to {@link #handle}.
     *
     * @return the captured {@link IOException}, or {@code null} if no error has been handled yet
     */
    @Override
    public IOException getCurrentException() {
        return error;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Matches when the throwable is a JVM {@link Error}.
     *
     * @param throwable the throwable raised while writing to InfluxDB
     * @return {@code true} if the throwable is an {@link Error}, {@code false} otherwise
     */
    @Override
    public boolean filterError(Throwable throwable) {
        return throwable instanceof Error;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the throwable in an {@code InfluxWriteException} and logs the failed points.
     *
     * @param points    the points that failed to be written
     * @param throwable the error that occurred while writing
     */
    @Override
    public void handle(Iterable<Point> points, Throwable throwable) {
        error = new InfluxWriteException(throwable);
        logFailedPoints(points, LOGGER);
    }
}
