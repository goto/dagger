package com.gotocompany.dagger.core.sink.influx.errors;

import com.gotocompany.dagger.core.exception.InfluxWriteException;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The Valid exception.
 */
public class ValidException implements InfluxError {
    /** Logger used to record the points that failed to be written. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidException.class.getName());
    /** The wrapped write exception captured for the most recently handled failure. */
    private IOException exception;

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
     * <p>Returns the exception captured by the most recent call to {@link #handle}.
     *
     * @return the captured {@link IOException}, or {@code null} if no error has been handled yet
     */
    @Override
    public IOException getCurrentException() {
        return exception;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Matches when the throwable is an {@link Exception}.
     *
     * @param throwable the throwable raised while writing to InfluxDB
     * @return {@code true} if the throwable is an {@link Exception}, {@code false} otherwise
     */
    @Override
    public boolean filterError(Throwable throwable) {
        return throwable instanceof Exception;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the throwable in an {@code InfluxWriteException} and logs the failed points.
     *
     * @param points    the points that failed to be written
     * @param throwable the exception that occurred while writing
     */
    @Override
    public void handle(Iterable<Point> points, Throwable throwable) {
        exception = new InfluxWriteException(throwable);
        logFailedPoints(points, LOGGER);
    }
}
