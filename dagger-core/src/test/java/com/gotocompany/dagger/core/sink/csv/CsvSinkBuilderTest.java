package com.gotocompany.dagger.core.sink.csv;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.api.java.utils.ParameterTool;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class CsvSinkBuilderTest {

    private static final String[] COLUMN_NAMES = new String[]{"id", "name"};

    private Configuration configurationOf(Map<String, String> values) {
        return new Configuration(ParameterTool.fromMap(values));
    }

    @Test
    public void shouldBuildCsvSinkWhenBasePathIsProvided() {
        Map<String, String> values = new HashMap<>();
        values.put(Constants.SINK_CSV_BASE_PATH_KEY, "file:///tmp/out");

        CsvSink sink = CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES);

        assertNotNull(sink);
    }

    @Test
    public void shouldBuildCsvSinkWithExplicitAppendWriteMode() {
        Map<String, String> values = new HashMap<>();
        values.put(Constants.SINK_CSV_BASE_PATH_KEY, "file:///tmp/out");
        values.put(Constants.SINK_CSV_WRITE_MODE_KEY, Constants.SINK_CSV_WRITE_MODE_APPEND);

        CsvSink sink = CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES);

        assertNotNull(sink);
    }

    @Test
    public void shouldThrowWhenBasePathIsMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> CsvSinkBuilder.build(configurationOf(new HashMap<>()), COLUMN_NAMES));
    }

    @Test
    public void shouldThrowWhenBasePathIsBlank() {
        Map<String, String> values = new HashMap<>();
        values.put(Constants.SINK_CSV_BASE_PATH_KEY, "   ");

        assertThrows(IllegalArgumentException.class,
                () -> CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES));
    }

    @Test
    public void shouldThrowForUnsupportedWriteMode() {
        Map<String, String> values = new HashMap<>();
        values.put(Constants.SINK_CSV_BASE_PATH_KEY, "file:///tmp/out");
        values.put(Constants.SINK_CSV_WRITE_MODE_KEY, "UPSERT");

        assertThrows(IllegalArgumentException.class,
                () -> CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES));
    }

    @Test
    public void shouldBuildCsvSinkWithValidDateFormats() {
        for (String dateFormat : new String[]{"yyyy", "yyyy-MMM-dd-HH-mm", "yyyy_MM_dd"}) {
            Map<String, String> values = new HashMap<>();
            values.put(Constants.SINK_CSV_BASE_PATH_KEY, "file:///tmp/out");
            values.put(Constants.SINK_CSV_PARTITION_DATE_FORMAT_KEY, dateFormat);

            assertNotNull(CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES));
        }
    }

    @Test
    public void shouldThrowForDateFormatWithDisallowedCharacters() {
        for (String dateFormat : new String[]{"yyyy/MM/dd", "yyyy:MM", "yyyy|MM", "yyyy.MM.dd", "yyyy MM dd"}) {
            Map<String, String> values = new HashMap<>();
            values.put(Constants.SINK_CSV_BASE_PATH_KEY, "file:///tmp/out");
            values.put(Constants.SINK_CSV_PARTITION_DATE_FORMAT_KEY, dateFormat);

            assertThrows("Expected rejection for date format '" + dateFormat + "'", IllegalArgumentException.class,
                    () -> CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES));
        }
    }

    @Test
    public void shouldBuildCsvSinkWithValidTimezones() {
        for (String timezone : new String[]{"Asia/Jakarta", "UTC", "Europe/London"}) {
            Map<String, String> values = new HashMap<>();
            values.put(Constants.SINK_CSV_BASE_PATH_KEY, "file:///tmp/out");
            values.put(Constants.SINK_CSV_PARTITION_TIMEZONE_KEY, timezone);

            assertNotNull(CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES));
        }
    }

    @Test
    public void shouldThrowForInvalidTimezone() {
        for (String timezone : new String[]{"Indonesia/Jakarta", "Not/AZone", "GMT+25"}) {
            Map<String, String> values = new HashMap<>();
            values.put(Constants.SINK_CSV_BASE_PATH_KEY, "file:///tmp/out");
            values.put(Constants.SINK_CSV_PARTITION_TIMEZONE_KEY, timezone);

            assertThrows("Expected rejection for timezone '" + timezone + "'", IllegalArgumentException.class,
                    () -> CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES));
        }
    }

    @Test
    public void shouldThrowForUnparseableDateFormat() {
        // 'J' passes the character allowlist but is an unknown DateTimeFormatter pattern letter.
        Map<String, String> values = new HashMap<>();
        values.put(Constants.SINK_CSV_BASE_PATH_KEY, "file:///tmp/out");
        values.put(Constants.SINK_CSV_PARTITION_DATE_FORMAT_KEY, "J");

        assertThrows(IllegalArgumentException.class,
                () -> CsvSinkBuilder.build(configurationOf(values), COLUMN_NAMES));
    }
}
