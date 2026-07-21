package com.gotocompany.dagger.core.sink.csv;

import com.gotocompany.dagger.core.sink.csv.writemode.OverwriteWriteStrategy;
import org.apache.flink.types.Row;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        return config(BASE_PATH, writeHeader);
    }

    private CsvSinkConfig config(String basePath, boolean writeHeader) {
        return new CsvSinkConfig(basePath, "my bookings job", "output", "dd-MMM-yyyy", ZoneId.of("UTC"), ",", writeHeader);
    }

    private CsvSinkWriter writer(String[] columnNames, boolean writeHeader, Clock clock) {
        return new CsvSinkWriter(columnNames, config(writeHeader), storageClient, new OverwriteWriteStrategy(), clock);
    }

    private CsvSinkWriter writerWithBasePath(String basePath) {
        return new CsvSinkWriter(new String[]{"a"}, config(basePath, false), storageClient, new OverwriteWriteStrategy(), DAY_ONE);
    }

    private CsvSinkWriter writerWithFormat(String dateFormat, Clock clock) {
        CsvSinkConfig config = new CsvSinkConfig(BASE_PATH, "my bookings job", "output", dateFormat, ZoneId.of("UTC"), ",", false);
        return new CsvSinkWriter(new String[]{"a"}, config, storageClient, new OverwriteWriteStrategy(), clock);
    }

    @Test
    public void shouldWriteHeaderAndRowOnPrepareCommit() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"service_type", "booking_count"}, true, DAY_ONE);

        writer.write(Row.of("GO_RIDE", 120L), null);
        writer.prepareCommit(false);

        String expectedPath = "file:///tmp/out/my_bookings_job/output-09-Jun-2026.csv";
        assertTrue(storageClient.exists(expectedPath));
        assertEquals("service_type,booking_count\nGO_RIDE,120\n", storageClient.readAsString(expectedPath));
    }

    @Test
    public void shouldNotFlushOnSnapshotState() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a"}, true, DAY_ONE);

        writer.write(Row.of("x"), null);
        writer.snapshotState(1L);

        assertEquals(0, storageClient.getWriteCount());
    }

    @Test
    public void shouldFlushBufferedRowsOnClose() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a"}, false, DAY_ONE);

        writer.write(Row.of("x"), null);
        writer.close();

        assertTrue(storageClient.exists(pathDayOne()));
    }

    @Test
    public void shouldSanitizeJobIdAndBuildDailyPath() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a"}, false, DAY_ONE);

        writer.write(Row.of("x"), null);
        writer.prepareCommit(false);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-09-Jun-2026.csv"));
    }

    @Test
    public void shouldRollOverToNextDayFile() throws Exception {
        writer(new String[]{"a"}, false, DAY_TWO).write(Row.of("x"), null);
        CsvSinkWriter dayTwoWriter = writer(new String[]{"a"}, false, DAY_TWO);
        dayTwoWriter.write(Row.of("x"), null);
        dayTwoWriter.prepareCommit(false);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-10-Jun-2026.csv"));
    }

    @Test
    public void shouldRollYearlyWithYearPattern() throws Exception {
        CsvSinkWriter writer = writerWithFormat("yyyy", DAY_ONE);

        writer.write(Row.of("x"), null);
        writer.prepareCommit(false);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-2026.csv"));
    }

    @Test
    public void shouldRollMinutelyWithMinutePattern() throws Exception {
        CsvSinkWriter writer = writerWithFormat("yyyy-MMM-dd-HH-mm", DAY_ONE);

        writer.write(Row.of("x"), null);
        writer.prepareCommit(false);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-2026-Jun-09-10-00.csv"));
    }

    @Test
    public void shouldShardIntoSeparateFilesPerMinute() throws Exception {
        Clock minuteZero = Clock.fixed(Instant.parse("2026-06-09T10:00:00Z"), ZoneOffset.UTC);
        Clock minuteOne = Clock.fixed(Instant.parse("2026-06-09T10:01:00Z"), ZoneOffset.UTC);

        CsvSinkWriter firstMinuteWriter = writerWithFormat("yyyy-MMM-dd-HH-mm", minuteZero);
        firstMinuteWriter.write(Row.of("x"), null);
        firstMinuteWriter.prepareCommit(false);

        CsvSinkWriter secondMinuteWriter = writerWithFormat("yyyy-MMM-dd-HH-mm", minuteOne);
        secondMinuteWriter.write(Row.of("y"), null);
        secondMinuteWriter.prepareCommit(false);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-2026-Jun-09-10-00.csv"));
        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-2026-Jun-09-10-01.csv"));
        assertEquals(2, storageClient.getWriteCount());
    }

    @Test
    public void shouldResolvePartitionDateInConfiguredTimezone() throws Exception {
        // 2026-06-09T17:30:00Z is already 2026-06-10 00:30 in Asia/Jakarta (UTC+7), so the daily partition rolls to the 10th.
        Clock jakartaClock = Clock.fixed(Instant.parse("2026-06-09T17:30:00Z"), ZoneId.of("Asia/Jakarta"));
        CsvSinkConfig config = new CsvSinkConfig(BASE_PATH, "my bookings job", "output", "dd-MMM-yyyy", ZoneId.of("Asia/Jakarta"), ",", false);
        CsvSinkWriter writer = new CsvSinkWriter(new String[]{"a"}, config, storageClient, new OverwriteWriteStrategy(), jakartaClock);

        writer.write(Row.of("x"), null);
        writer.prepareCommit(false);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-10-Jun-2026.csv"));
    }

    @Test
    public void shouldStripSingleTrailingSlashFromBasePath() throws Exception {
        CsvSinkWriter writer = writerWithBasePath("file:///tmp/out/");

        writer.write(Row.of("x"), null);
        writer.prepareCommit(false);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-09-Jun-2026.csv"));
    }

    @Test
    public void shouldStripMultipleTrailingSlashesFromBasePath() throws Exception {
        CsvSinkWriter writer = writerWithBasePath("file:///tmp/out///");

        writer.write(Row.of("x"), null);
        writer.prepareCommit(false);

        assertTrue(storageClient.exists("file:///tmp/out/my_bookings_job/output-09-Jun-2026.csv"));
    }

    @Test
    public void shouldFormatNullFieldAsEmpty() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a", "b"}, false, DAY_ONE);

        writer.write(Row.of("foo", null), null);
        writer.prepareCommit(false);

        assertEquals("foo,\n", storageClient.readAsString(pathDayOne()));
    }

    @Test
    public void shouldFormatLocalDateTimeAsIso() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"window_timestamp"}, false, DAY_ONE);

        writer.write(Row.of(LocalDateTime.of(2026, 6, 9, 12, 3, 0)), null);
        writer.prepareCommit(false);

        assertEquals("2026-06-09T12:03\n", storageClient.readAsString(pathDayOne()));
    }

    @Test
    public void shouldQuoteFieldsContainingDelimiterQuoteOrNewline() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a", "b", "c"}, false, DAY_ONE);

        writer.write(Row.of("has,comma", "has\"quote", "has\nnewline"), null);
        writer.prepareCommit(false);

        assertEquals("\"has,comma\",\"has\"\"quote\",\"has\nnewline\"\n", storageClient.readAsString(pathDayOne()));
    }

    @Test
    public void shouldJsonEncodeCompositeValues() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"ids"}, false, DAY_ONE);

        writer.write(Row.of((Object) new int[]{1, 2, 3}), null);
        writer.prepareCommit(false);

        assertEquals("\"[1,2,3]\"\n", storageClient.readAsString(pathDayOne()));
    }

    @Test
    public void shouldNotWriteWhenNoRowsBuffered() throws Exception {
        CsvSinkWriter writer = writer(new String[]{"a"}, true, DAY_ONE);

        writer.prepareCommit(false);

        assertEquals(0, storageClient.getWriteCount());
    }

    private String pathDayOne() {
        return "file:///tmp/out/my_bookings_job/output-09-Jun-2026.csv";
    }
}
