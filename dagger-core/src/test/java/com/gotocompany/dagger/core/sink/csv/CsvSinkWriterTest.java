package com.gotocompany.dagger.core.sink.csv;

import com.gotocompany.dagger.core.sink.csv.writemode.OverwriteWriteStrategy;
import org.apache.flink.types.Row;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvSinkWriterTest {

    private static final String BASE_PATH = "file:///tmp/out";
    private static final Clock DAY_ONE = Clock.fixed(Instant.parse("2026-06-09T10:00:00Z"), ZoneOffset.UTC);
    private static final Clock DAY_TWO = Clock.fixed(Instant.parse("2026-06-10T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryFileStorageClient storageClient;

    @Before
    public void setup() {
        storageClient = new InMemoryFileStorageClient();
    }

    private CsvSinkConfig config(boolean writeHeader) {
        return new CsvSinkConfig(BASE_PATH, "my bookings job", "output", "dd-MMM-yyyy", ",", writeHeader);
    }

    private CsvSinkWriter writer(String[] columnNames, boolean writeHeader, Clock clock) {
        return new CsvSinkWriter(columnNames, config(writeHeader), storageClient, new OverwriteWriteStrategy(), clock);
    }

    @Test
    public void shouldWriteHeaderAndRowOnSnapshot() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"service_type", "booking_count"}, true, DAY_ONE);

        writer.write(Row.of("GO_RIDE", 120L), null);
        writer.snapshotState(1L);

        String expectedPath = "file:///tmp/out/my_bookings_job/output-09-Jun-2026.csv";
        assertTrue(storageClient.exists(expectedPath));
        assertEquals("service_type,booking_count\nGO_RIDE,120\n", storageClient.readAsString(expectedPath));
    }

    @Test
    public void shouldSanitizeJobIdAndBuildDailyPath() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a"}, false, DAY_ONE);

        writer.write(Row.of("x"), null);
        writer.snapshotState(1L);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-09-Jun-2026.csv"));
    }

    @Test
    public void shouldRollOverToNextDayFile() throws Exception {
        writer(new String[]{"a"}, false, DAY_TWO).write(Row.of("x"), null);
        CsvSinkWriter dayTwoWriter = writer(new String[]{"a"}, false, DAY_TWO);
        dayTwoWriter.write(Row.of("x"), null);
        dayTwoWriter.snapshotState(1L);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-10-Jun-2026.csv"));
    }

    @Test
    public void shouldFormatNullFieldAsEmpty() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a", "b"}, false, DAY_ONE);

        writer.write(Row.of("foo", null), null);
        writer.snapshotState(1L);

        assertEquals("foo,\n", storageClient.readAsString(pathDayOne()));
    }

    @Test
    public void shouldFormatLocalDateTimeAsIso() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"window_timestamp"}, false, DAY_ONE);

        writer.write(Row.of(LocalDateTime.of(2026, 6, 9, 12, 3, 0)), null);
        writer.snapshotState(1L);

        assertEquals("2026-06-09T12:03\n", storageClient.readAsString(pathDayOne()));
    }

    @Test
    public void shouldQuoteFieldsContainingDelimiterQuoteOrNewline() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a", "b", "c"}, false, DAY_ONE);

        writer.write(Row.of("has,comma", "has\"quote", "has\nnewline"), null);
        writer.snapshotState(1L);

        assertEquals("\"has,comma\",\"has\"\"quote\",\"has\nnewline\"\n", storageClient.readAsString(pathDayOne()));
    }

    @Test
    public void shouldJsonEncodeCompositeValues() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"ids"}, false, DAY_ONE);

        writer.write(Row.of((Object) new int[]{1, 2, 3}), null);
        writer.snapshotState(1L);

        assertEquals("\"[1,2,3]\"\n", storageClient.readAsString(pathDayOne()));
    }

    @Test
    public void shouldNotWriteWhenNoRowsBuffered() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a"}, true, DAY_ONE);

        writer.snapshotState(1L);

        assertEquals(0, storageClient.getWriteCount());
    }

    private String pathDayOne() {
        return "file:///tmp/out/my_bookings_job/output-09-Jun-2026.csv";
    }
}
