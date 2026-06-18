package com.gotocompany.dagger.functions.udfs.table.outlier.mad;

import java.sql.Timestamp;

/**
 * The Point for OutlierMad udf.
 */
public class Point {
    /**
     * The timestamp of this data point.
     */
    private final Timestamp timestamp;
    /**
     * The observed value of this data point.
     */
    private final Double value;
    /**
     * Whether this point falls inside the observation window and should be considered when detecting outliers.
     */
    private final boolean observable;
    /**
     * The value's distance from the median scaled by the median absolute deviation.
     */
    private Double distanceFromMad;
    /**
     * Whether this point has been classified as an outlier.
     */
    private boolean isOutlier;
    /**
     * The upper bound beyond which the value is considered an outlier.
     */
    private double upperBound;
    /**
     * The lower bound below which the value is considered an outlier.
     */
    private double lowerBound;

    /**
     * A reusable placeholder point with a {@code null} timestamp, a zero value and marked as not
     * observable, used to pre-fill collections before the real points are computed.
     */
    public static final Point EMPTY_POINT = new Point(null, 0d, false);

    /**
     * Instantiates a new Point.
     *
     * @param timestamp  the timestamp
     * @param value      the value
     * @param observable the observable
     */
    public Point(Timestamp timestamp, Double value, boolean observable) {
        this.timestamp = timestamp;
        this.value = value;
        this.upperBound = value;
        this.lowerBound = value;
        this.observable = observable;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public Double getValue() {
        return value;
    }

    /**
     * Check if it is observable.
     *
     * @return the boolean
     */
    public boolean isObservable() {
        return observable;
    }

    /**
     * Gets distance from mad.
     *
     * @return the distance from mad
     */
    public Double getDistanceFromMad() {
        return distanceFromMad;
    }

    /**
     * Check if it is outlier.
     *
     * @return the boolean
     */
    public boolean isOutlier() {
        return isOutlier;
    }

    /**
     * Gets upper bound.
     *
     * @return the upper bound
     */
    public double getUpperBound() {
        return upperBound;
    }

    /**
     * Gets lower bound.
     *
     * @return the lower bound
     */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
     * Sets outlier.
     *
     * @param outlier the outlier
     */
    public void setOutlier(boolean outlier) {
        isOutlier = outlier;
    }

    /**
     * Sets upper bound.
     *
     * @param upperBound the upper bound
     */
    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    /**
     * Sets lower bound.
     *
     * @param lowerBound the lower bound
     */
    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * Sets distance from mad.
     *
     * @param distanceFromMad the distance from mad
     */
    public void setDistanceFromMad(Double distanceFromMad) {
        this.distanceFromMad = distanceFromMad;
    }
}
