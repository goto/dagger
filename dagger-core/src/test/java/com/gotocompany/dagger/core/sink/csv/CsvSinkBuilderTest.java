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
}
