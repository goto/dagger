package com.gotocompany.dagger.core.source.parquet.path;

import org.apache.flink.core.fs.Path;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link PathParser} for the {@code dt=YYYY-MM-DD[/hr=HH]} Hive-style partition layout.
 *
 * <p>It extracts a UTC {@link Instant} from a Parquet file path by matching a {@code dt=} date
 * segment and an optional {@code hr=} hour segment. When the hour segment is present the instant is
 * resolved to the hour; otherwise it falls back to midnight UTC of the matched date. Paths that
 * match neither shape cause a {@link ParseException}. The class is {@link Serializable} so it can be
 * captured by the split assigner within the Flink job graph.
 */
public class HourDatePathParser implements PathParser, Serializable {
    /**
     * {@inheritDoc}
     *
     * <p>Matches the path against {@code dt=YYYY-MM-DD} and an optional {@code hr=HH} segment. If
     * both date and hour are present the resulting {@link Instant} is at hour granularity; if only
     * the date is present it is midnight UTC of that date.
     *
     * @param path the Parquet file path to parse
     * @return the UTC {@link Instant} derived from the path's date (and optional hour)
     * @throws ParseException if the path matches no supported partitioning scheme
     */
    @Override
    public Instant instantFromFilePath(Path path) throws ParseException {
        Pattern filePathPattern = Pattern.compile("^.*/dt=([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])/(hr=([0-9][0-9]))?.*$");
        Matcher matcher = filePathPattern.matcher(path.toString());
        final int hourMatcherGroupNumber = 3;
        final int dateMatcherGroupNumber = 1;
        boolean matchFound = matcher.find();
        if (matchFound && matcher.group(hourMatcherGroupNumber) != null && matcher.group(dateMatcherGroupNumber) != null) {
            return convertToInstant(matcher.group(dateMatcherGroupNumber), matcher.group(hourMatcherGroupNumber));
        } else if (matchFound && matcher.group(hourMatcherGroupNumber) == null && matcher.group(dateMatcherGroupNumber) != null) {
            return convertToInstant(matcher.group(dateMatcherGroupNumber));
        } else {
            String message = String.format("Cannot extract timestamp from filepath for deciding order of processing.\n"
                    + "File path doesn't abide with any partitioning strategy: %s", path);
            throw new ParseException(message, 0);
        }
    }

    /**
     * Parses a date-only partition segment into a UTC {@link Instant} at midnight.
     *
     * @param dateSegment the date string in {@code yyyy-MM-dd} format
     * @return the {@link Instant} at 00:00 UTC of the given date
     * @throws ParseException if {@code dateSegment} cannot be parsed
     */
    private Instant convertToInstant(String dateSegment) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        return simpleDateFormat.parse(dateSegment).toInstant();
    }

    /**
     * Parses a date and hour partition pair into a UTC {@link Instant} at hour granularity.
     *
     * @param dateSegment the date string in {@code yyyy-MM-dd} format
     * @param hourSegment the two-digit hour-of-day string ({@code HH})
     * @return the {@link Instant} at the given date and hour in UTC
     * @throws ParseException if the combined date and hour cannot be parsed
     */
    private Instant convertToInstant(String dateSegment, String hourSegment) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        String dateHourString = String.join(" ", dateSegment, hourSegment);
        return simpleDateFormat.parse(dateHourString).toInstant();
    }
}
