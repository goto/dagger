package com.gotocompany.dagger.functions.udfs.scalar;

import com.gotocompany.dagger.common.udfs.ScalarUdf;
import org.apache.flink.table.annotation.DataTypeHint;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The Linear trend udf.
 */
public class LinearTrend extends ScalarUdf {
    /**
     * Number of milliseconds in one minute, used to bucket timestamps into per-minute positions.
     */
    private static final long MILLI_SECONDS_IN_MINUTE = 60000;

    /**
     * returns the gradient of the best fit line of the list of non-null demand values given the defined time window.
     *
     * @param localDateTimeArray    the timestamps array
     * @param values                the values
     * @param hopStartTime          the hop start time
     * @param windowLengthInMinutes the window length in minutes
     * @return the double
     */
    public double eval(@DataTypeHint(value = "RAW", bridgedTo = ArrayList.class) ArrayList<LocalDateTime> localDateTimeArray, @DataTypeHint(value = "RAW", bridgedTo = ArrayList.class) ArrayList<Double> values, LocalDateTime hopStartTime, Integer windowLengthInMinutes) {
        ArrayList<Timestamp> timestamps = new ArrayList<Timestamp>();
        localDateTimeArray.forEach(localDateTime -> timestamps.add(Timestamp.valueOf(localDateTime)));
        return calculateLinearTrend(timestamps, values, Timestamp.valueOf(hopStartTime), windowLengthInMinutes);
    }

    /**
     * Computes the gradient of the best-fit line over the demand values arranged across the time window.
     *
     * <p>The slope is derived as the ratio of the time/value covariance to the time variance.
     *
     * @param timestampsArray       the timestamps associated with each value
     * @param valueList             the demand values aligned with {@code timestampsArray}
     * @param hopStartTime          the start time of the hop window
     * @param windowLengthInMinutes the length of the window, in minutes
     * @return the gradient (slope) of the best-fit line
     */
    private double calculateLinearTrend(ArrayList<Timestamp> timestampsArray, ArrayList<Double> valueList, Timestamp hopStartTime, Integer windowLengthInMinutes) {
        ArrayList<Double> hopWindowList = IntStream.range(0, windowLengthInMinutes).mapToObj(i -> (double) i).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Double> orderedValueList = getOrderedValueList(hopStartTime, valueList, timestampsArray, windowLengthInMinutes);

        double timeValueCovariance = getCovariance(hopWindowList, orderedValueList, windowLengthInMinutes);
        double timeVariance = getVariance(hopWindowList, windowLengthInMinutes);
        return (timeValueCovariance / timeVariance);
    }

    /**
     * Places each value at its per-minute position within the window, leaving gaps as {@code 0}.
     *
     * @param hopStartTime          the start time of the hop window
     * @param valueList             the demand values to order
     * @param timestampsArray       the timestamps associated with each value
     * @param windowLengthInMinutes the length of the window, in minutes
     * @return a list of length {@code windowLengthInMinutes} with values placed at their time positions
     */
    private ArrayList<Double> getOrderedValueList(Timestamp hopStartTime, ArrayList<Double> valueList, ArrayList<Timestamp> timestampsArray, int windowLengthInMinutes) {
        ArrayList<Double> orderedValueList = new ArrayList<>(Collections.nCopies(windowLengthInMinutes, 0d));
        IntStream.range(0, valueList.size()).forEach(index -> {
            double value = valueList.get(index);
            Timestamp valueStartTime = timestampsArray.get(index);
            int position = getPosition(valueStartTime, hopStartTime);
            orderedValueList.set(position, value);
        });
        return orderedValueList;
    }

    /**
     * Computes the minute offset of a value's timestamp relative to the hop window start.
     *
     * @param valueStartTime the timestamp of the value
     * @param hopStartTime   the start time of the hop window
     * @return the zero-based position, in minutes, of the value within the window
     */
    private int getPosition(Timestamp valueStartTime, Timestamp hopStartTime) {
        long hopStartMS = hopStartTime.getTime();
        long valueStartMS = valueStartTime.getTime();

        long deltaInMinute = ((valueStartMS - hopStartMS) / MILLI_SECONDS_IN_MINUTE);
        return (int) deltaInMinute;
    }

    /**
     * Computes the (unnormalised) variance of the supplied series across the window.
     *
     * @param list            the series of values (the time positions)
     * @param hopWindowLength the number of positions in the window
     * @return the variance term used in the linear-trend calculation
     */
    private double getVariance(ArrayList<Double> list, int hopWindowLength) {
        return getSumOfAnArray(getSquareArray(list)) - Math.pow(getSumOfAnArray(list), 2) / hopWindowLength;
    }

    /**
     * Computes the (unnormalised) covariance between two equal-length series across the window.
     *
     * @param listOne         the first series (the time positions)
     * @param listTwo         the second series (the ordered values)
     * @param hopWindowLength the number of positions in the window
     * @return the covariance term used in the linear-trend calculation
     */
    private double getCovariance(ArrayList<Double> listOne, ArrayList<Double> listTwo, int hopWindowLength) {
        return getSumOfAnArray(multiplyListsOfSameLength(listOne, listTwo)) - getSumOfAnArray(listOne) * getSumOfAnArray(listTwo) / hopWindowLength;
    }

    /**
     * Multiplies two equal-length lists element by element.
     *
     * @param listOne the first list of operands
     * @param listTwo the second list of operands
     * @return a new list whose elements are the pairwise products of the inputs
     */
    private ArrayList<Double> multiplyListsOfSameLength(ArrayList<Double> listOne, ArrayList<Double> listTwo) {
        ArrayList<Double> arrayAfterMultiplication = new ArrayList<>();
        IntStream.range(0, listOne.size()).forEach(index -> arrayAfterMultiplication.add(index, listOne.get(index) * listTwo.get(index)));
        return arrayAfterMultiplication;
    }

    /**
     * Squares each element of the supplied list.
     *
     * @param array the list of values to square
     * @return a new list containing the square of each input element
     */
    private ArrayList<Double> getSquareArray(ArrayList<Double> array) {
        return array.stream().map(element -> element * element).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Sums all elements of the supplied list.
     *
     * @param array the list of values to sum
     * @return the sum of all elements in {@code array}
     */
    private double getSumOfAnArray(ArrayList<Double> array) {
        return array.stream().mapToDouble(element -> element).sum();
    }
}
