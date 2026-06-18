package com.gotocompany.dagger.core.sink.influx.errors;

import org.influxdb.dto.Point;

import java.io.IOException;

/**
 * No error found on Influx sink.
 */
public class NoError implements InfluxError {
    /**
     * {@inheritDoc}
     *
     * <p>This no-op strategy never carries an exception, so this always returns {@code false}.
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
     * <p>There is never an associated exception, so this always returns {@code null}.
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
     * <p>This fallback strategy matches nothing, so it always returns {@code false}.
     *
     * @param throwable the throwable raised while writing to InfluxDB
     * @return {@code false} always
     */
    @Override
    public boolean filterError(Throwable throwable) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>No action is taken because there is no error to handle.
     *
     * @param points    the points associated with the (absent) error
     * @param throwable the throwable, which is ignored
     */
    @Override
    public void handle(Iterable<Point> points, Throwable throwable) {
    }
}
