package com.gotocompany.dagger.functions.udfs.table.outlier.mad;

import com.gotocompany.dagger.functions.exceptions.MadZeroException;
import com.gotocompany.dagger.functions.exceptions.MedianNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.nCopies;
import static java.util.Collections.sort;

/**
 * The Mad for OutlierMad udf.
 */
public class Mad {
    /**
     * Logger used to record failures encountered while computing the median absolute deviation.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Mad.class.getName());
    /**
     * The time-series points being analysed for outliers.
     */
    private List<Point> points;
    /**
     * Permitted deviation, expressed as a multiple of the MAD, beyond which a point is treated as an outlier.
     */
    private final Integer tolerance;

    /**
     * Instantiates a new Mad.
     *
     * @param points    the points
     * @param tolerance the tolerance
     */
    public Mad(List<Point> points, Integer tolerance) {
        this.points = points;
        this.tolerance = tolerance;
    }

    /**
     * Gets outliers.
     *
     * @return the outliers
     */
    public List<Point> getOutliers() {
        ArrayList<Double> doubleMAD;
        try {
            doubleMAD = getDoubleMAD();
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            return Collections.emptyList();
        }

        setDistanceFromMAD(doubleMAD);
        for (Point point : points) {
            if (point.isObservable() && point.getDistanceFromMad() > this.tolerance) {
                point.setOutlier(true);
            }
        }
        return points.stream().filter(Point::isOutlier).collect(Collectors.toList());
    }

    /**
     * Computes and stores, for every point, its scaled distance from the median together with the
     * tolerance-derived upper and lower bounds.
     *
     * <p>Points at or below the median use the lower-side MAD while points above the median use the
     * upper-side MAD, supporting the double-MAD (asymmetric) variant of the algorithm.
     *
     * @param doubleMAD a two-element list holding the lower-side MAD at index {@code 0} and the
     *                  upper-side MAD at index {@code 1}
     */
    private void setDistanceFromMAD(ArrayList<Double> doubleMAD) {
        Double median = getMedian(points.stream().map(Point::getValue).collect(Collectors.toList()));
        for (Point point : points) {
            Double value = point.getValue();
            Double mad = (value <= median) ? doubleMAD.get(0) : doubleMAD.get(1);
            point.setDistanceFromMad(Math.abs(value - median) / mad);
            point.setUpperBound(median + (this.tolerance * mad));
            point.setLowerBound(median - (this.tolerance * mad));
        }
    }

    /**
     * Computes the double (asymmetric) median absolute deviation around the median of the points.
     *
     * <p>Points are split into those at or below the median and those at or above it, and a MAD is
     * computed for each side.
     *
     * @return a two-element {@code ArrayList<Double>} containing the lower-side MAD followed by the
     *         upper-side MAD
     * @throws MadZeroException if either side has a MAD of zero, which makes outliers undetectable
     * @throws MedianNotFound   if the median cannot be computed because there are no values
     */
    private ArrayList<Double> getDoubleMAD() {
        ArrayList<Point> valuesLessThanMedian = new ArrayList<>();
        ArrayList<Point> valuesGreaterThanMedian = new ArrayList<>();
        Double median = getMedian(points.stream().map(Point::getValue).collect(Collectors.toList()));
        points.forEach(point -> {
            if (point.getValue() <= median) {
                valuesLessThanMedian.add(point);
            }
            if (point.getValue() >= median) {
                valuesGreaterThanMedian.add(point);
            }
        });

        ArrayList<Double> doubleMad = new ArrayList<>();
        doubleMad.add(getMAD(valuesLessThanMedian));
        doubleMad.add(getMAD(valuesGreaterThanMedian));

        return doubleMad;
    }

    /**
     * Computes the median absolute deviation (MAD) of the given points' values.
     *
     * <p>The MAD is the median of the absolute distances of each value from the values' median.
     *
     * @param points the points whose values the MAD is computed from
     * @return the median absolute deviation of the supplied values
     * @throws MadZeroException if the computed MAD is zero, in which case outliers cannot be detected
     * @throws MedianNotFound   if a median cannot be computed because the list is empty
     */
    private static Double getMAD(List<Point> points) {
        Double median = getMedian(points.stream().map(Point::getValue).collect(Collectors.toList()));
        List<Double> absoluteDistancesFromMedian =
                getAbsoluteDistance(points
                        .stream()
                        .map(Point::getValue)
                        .collect(Collectors.toList()), median);
        Double absoluteDistancesMedian = getMedian(absoluteDistancesFromMedian);
        if (absoluteDistancesMedian == 0) {
            throw new MadZeroException("MAD is ZERO, outlier cannot be detected");
        }
        return absoluteDistancesMedian;

    }

    /**
     * Computes the absolute distance of each value from a reference value.
     *
     * @param values the values to measure
     * @param value  the reference value distances are measured from
     * @return a list of absolute distances aligned with the input {@code values}
     */
    private static List<Double> getAbsoluteDistance(List<Double> values, Double value) {
        ArrayList<Double> absoluteDistances = new ArrayList<>(nCopies(values.size(), 0d));
        for (int index = 0; index < values.size(); index++) {
            absoluteDistances.set(index, Math.abs(values.get(index) - value));
        }
        return absoluteDistances;
    }

    /**
     * Computes the median of the supplied values, sorting the list in place.
     *
     * <p>For an even number of elements the mean of the two central values is returned.
     *
     * @param values the values to compute the median of; reordered in place by this call
     * @return the median value
     * @throws MedianNotFound if {@code values} is empty
     */
    private static Double getMedian(List<Double> values) {
        sort(values);
        int pointValueSize = values.size();
        if (pointValueSize == 0) {
            throw new MedianNotFound("To calculate median we need at least 1 element");
        }
        if (pointValueSize % 2 == 0) {
            return ((values.get(pointValueSize / 2 - 1) + values.get(pointValueSize / 2)) / 2);
        }
        return values.get(pointValueSize / 2);
    }

}
