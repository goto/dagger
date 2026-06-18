package com.gotocompany.dagger.core.source.config.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.gotocompany.dagger.core.exception.InvalidTimeRangeException;
import com.gotocompany.dagger.core.source.config.models.TimeRange;
import com.gotocompany.dagger.core.source.config.models.TimeRangePool;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

/**
 * Gson {@link TypeAdapter} that parses a Parquet file date-range expression into a
 * {@link TimeRangePool} while a {@code StreamConfig} is deserialized.
 *
 * <p>It is wired in through a {@code @JsonAdapter} annotation on the stream config's
 * {@code parquetFileDateRange} field. The configured value is a single string holding one or more
 * comma-separated start/end timestamp pairs, with multiple ranges separated by {@code ;}. Each
 * timestamp may be either UTC ({@code yyyy-MM-dd'T'HH:mm:ss'Z'}) or local
 * ({@code yyyy-MM-dd'T'HH:mm:ss}, also interpreted as UTC). Malformed input, or a start that is after
 * its end, raises an {@link InvalidTimeRangeException}. Serialization is a no-op since the range is
 * read-only configuration.
 */
public class FileDateRangeAdaptor extends TypeAdapter<TimeRangePool> {
    /**
     * No-op serializer; the date range is read-only configuration and is never written back to JSON.
     *
     * @param out   the writer (unused)
     * @param value the time-range pool (unused)
     */
    @Override
    public void write(JsonWriter out, TimeRangePool value) {

    }

    /**
     * Parses the date-range expression into a {@link TimeRangePool}.
     *
     * <p>The value is split on {@code ;} into individual ranges, and each range is split on {@code ,}
     * into a start and end timestamp. Both timestamps are parsed via {@link #parseInstant(String)} and
     * added as a {@link TimeRange}; the start must not be after the end.
     *
     * @param reader the reader positioned at the date-range string
     * @return a pool containing one {@link TimeRange} per configured timestamp pair
     * @throws IOException               if reading from the underlying JSON stream fails
     * @throws InvalidTimeRangeException if a range does not contain exactly two timestamps, or its
     *                                   start is after its end
     */
    @Override
    public TimeRangePool read(JsonReader reader) throws IOException {
        TimeRangePool timeRangePool = new TimeRangePool();
        String timeRangesString = reader.nextString();
        String[] timeRangesArray = timeRangesString.split(";");
        Arrays.asList(timeRangesArray).forEach(timeRange -> {
            String[] timestamps = timeRange.split(",");
            if (timestamps.length == 2) {
                Instant startTime = parseInstant(timestamps[0].trim());
                Instant endTime = parseInstant(timestamps[1].trim());
                if (startTime.isAfter(endTime)) {
                    throw new InvalidTimeRangeException("startTime should not be after endTime");
                }
                timeRangePool.add(new TimeRange(startTime, endTime));
            } else {
                throw new InvalidTimeRangeException("Each time range should contain a pair of ISO format timestamps separated by comma. Multiple ranges can be provided separated by ;");
            }
        });
        return timeRangePool;
    }

    /**
     * Parses a single timestamp string into an {@link Instant} in the UTC time zone.
     *
     * <p>Two layouts are accepted and distinguished purely by their character length: the UTC form
     * {@code yyyy-MM-dd'T'HH:mm:ss'Z'} and the local form {@code yyyy-MM-dd'T'HH:mm:ss}; both are
     * interpreted as UTC.
     *
     * @param timestamp the timestamp text to parse
     * @return the parsed instant
     * @throws InvalidTimeRangeException if the text matches neither supported layout or cannot be parsed
     */
    private Instant parseInstant(String timestamp) {
        String utcDateFormatPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        SimpleDateFormat utcDateFormat = new SimpleDateFormat(utcDateFormatPattern);
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


        String localDataFormatPattern = "yyyy-MM-dd'T'HH:mm:ss";
        SimpleDateFormat localDateFormat = new SimpleDateFormat(localDataFormatPattern);
        localDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (timestamp.length() == utcDateFormatPattern.replace("'", "").length()) {
            return parse(timestamp, utcDateFormat).toInstant();
        } else if (timestamp.length() == localDataFormatPattern.replace("'", "").length()) {
            return parse(timestamp, localDateFormat).toInstant();
        }
        throw new InvalidTimeRangeException(String.format("Unable to parse timestamp: %s with supported date formats i.e. yyyy-MM-ddTHH:mm:ssZ and yyyy-MM-ddTHH:mm:ss", timestamp));
    }

    /**
     * Parses a timestamp with the supplied formatter, translating parse failures into a domain error.
     *
     * @param timestamp        the timestamp text to parse
     * @param simpleDateFormat the formatter (already configured for the UTC time zone) to apply
     * @return the parsed {@link Date}
     * @throws InvalidTimeRangeException if the text cannot be parsed by the given formatter
     */
    private Date parse(String timestamp, SimpleDateFormat simpleDateFormat) {
        try {
            return simpleDateFormat.parse(timestamp);
        } catch (ParseException e) {
            throw new InvalidTimeRangeException(String.format("Unable to parse timestamp: %s with supported date formats i.e. yyyy-MM-ddTHH:mm:ssZ and yyyy-MM-ddTHH:mm:ss", timestamp));
        }
    }
}
