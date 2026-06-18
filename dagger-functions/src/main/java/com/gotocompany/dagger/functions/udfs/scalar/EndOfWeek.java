package com.gotocompany.dagger.functions.udfs.scalar;

import com.gotocompany.dagger.common.udfs.ScalarUdf;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * The EndOfWeek udf.
 */
public class EndOfWeek extends ScalarUdf {

    /**
     * Hour-of-day ({@code 23}) representing the final hour, used to roll a timestamp to the end of the day.
     */
    private static final Integer END_OF_DAY_HOUR = 23;

    /**
     * Minute and second value ({@code 59}) marking the last minute and second of the day.
     */
    private static final Integer END_OF_DAY_MINUTE_AND_SECOND = 59;

    /**
     * Number of days in a week ({@code 7}), added to advance from the start of the week to its end.
     */
    private static final Integer DAY_SPAN = 7;

    /**
     * Largest millisecond value ({@code 999}) within a second, used to reach the very end of the day.
     */
    private static final Integer MAX_MILLISECONDS = 999;

    /**
     * Calculates the milliSeconds in Unix time for end of a week of a given timestamp second and timezone.
     *
     * @param milliSeconds the milliSeconds for which start of the week to be calculated
     * @param timeZone     the time zone
     * @return unix timestamp in milliSeconds for the end of the week
     * @author Rasyid
     * @team DE
     */
    public long eval(Long milliSeconds, String timeZone) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(timeZone));
        cal.setTime(new Date(milliSeconds));

        cal.set(Calendar.HOUR_OF_DAY, END_OF_DAY_HOUR);
        cal.set(Calendar.MINUTE, END_OF_DAY_MINUTE_AND_SECOND);
        cal.set(Calendar.SECOND, END_OF_DAY_MINUTE_AND_SECOND);
        cal.set(Calendar.MILLISECOND, MAX_MILLISECONDS);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.DATE, DAY_SPAN);

        return cal.getTimeInMillis();
    }
}
