package com.gotocompany.dagger.core.sink.csv;

import com.gotocompany.dagger.core.sink.csv.storage.FileStorageClient;
import com.gotocompany.dagger.core.sink.csv.writemode.FileWriteStrategy;
import com.google.gson.Gson;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Buffers formatted CSV rows and flushes them on every checkpoint and on close. The flush is driven
 * from {@code prepareCommit}, which Flink invokes on every checkpoint (via prepareSnapshotPreBarrier)
 * and once with endOfInput=true at end of input; this is the reliable hook for a stateless sink,
 * since {@code snapshotState} is only invoked for sinks that expose a writer-state serializer.
 * The destination path is {@code basePath/<jobId>/<prefix>-<date>.csv}; the rolling/sharding
 * granularity (yearly, daily, hourly, minutely, ...) is decided by the SINK_CSV_PARTITION_DATE_FORMAT pattern.
 * Whether the flush appends or fully replaces the file is decided by the configured {@link FileWriteStrategy}.
 */
public class CsvSinkWriter implements SinkWriter<Row, Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvSinkWriter.class);
    private static final Gson GSON = new Gson();
    private static final String PATH_SEPARATOR = "/";
    private static final String FILE_EXTENSION = ".csv";

    private final String[] columnNames;
    private final CsvSinkConfig config;
    private final FileStorageClient storageClient;
    private final FileWriteStrategy writeStrategy;
    private final DateTimeFormatter dateTimeFormatter;
    private final String sanitizedJobId;
    private final String basePath;
    private final Clock clock;
    private final List<String> bufferedLines = new ArrayList<>();

    public CsvSinkWriter(String[] columnNames, CsvSinkConfig config, FileStorageClient storageClient, FileWriteStrategy writeStrategy) {
        this(columnNames, config, storageClient, writeStrategy, Clock.systemDefaultZone());
    }

    CsvSinkWriter(String[] columnNames, CsvSinkConfig config, FileStorageClient storageClient, FileWriteStrategy writeStrategy, Clock clock) {
        this.columnNames = columnNames;
        this.config = config;
        this.storageClient = storageClient;
        this.writeStrategy = writeStrategy;
        this.clock = clock;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(config.getDateFormat(), Locale.ENGLISH);
        this.sanitizedJobId = sanitize(config.getJobId());
        this.basePath = stripTrailingSeparator(config.getBasePath());
        LOGGER.info("CSV sink writer initialized: basePath={}, jobId={}, writeStrategy={}, writeHeader={}, parallelism=1",
                basePath, sanitizedJobId, writeStrategy.getClass().getSimpleName(), config.isWriteHeader());
    }

    @Override
    public void write(Row row, Context context) {
        bufferedLines.add(formatRow(row));
    }

    @Override
    public List<Void> prepareCommit(boolean endOfInput) throws IOException {
        LOGGER.info("CSV sink flushing ({} buffered row(s), endOfInput={})", bufferedLines.size(), endOfInput);
        flush();
        return Collections.emptyList();
    }

    @Override
    public List<Void> snapshotState(long checkpointId) {
        return Collections.emptyList();
    }

    @Override
    public void close() throws Exception {
        LOGGER.info("CSV sink closing, flushing {} buffered row(s)", bufferedLines.size());
        flush();
    }

    private void flush() throws IOException {
        String path = buildPath();
        if (bufferedLines.isEmpty()) {
            LOGGER.info("CSV sink: nothing to flush (empty buffer) for {}", path);
            return;
        }
        int rowCount = bufferedLines.size();
        try {
            LOGGER.info("CSV sink: writing {} row(s) to {}", rowCount, path);
            writeStrategy.flush(storageClient, path, buildHeaderLine(), bufferedLines);
            bufferedLines.clear();
            LOGGER.info("CSV sink: successfully wrote {} row(s) to {}", rowCount, path);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("CSV sink: failed to write {} row(s) to {} : {}", rowCount, path, e.getMessage(), e);
            throw e;
        }
    }

    private String buildPath() {
        String date = LocalDateTime.now(clock).format(dateTimeFormatter);
        return basePath + PATH_SEPARATOR + sanitizedJobId + PATH_SEPARATOR
                + config.getFilenamePrefix() + "-" + date + FILE_EXTENSION;
    }

    private String buildHeaderLine() {
        if (!config.isWriteHeader()) {
            return null;
        }
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            if (i > 0) {
                header.append(config.getDelimiter());
            }
            header.append(escape(columnNames[i]));
        }
        return header.toString();
    }

    private String formatRow(Row row) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            if (i > 0) {
                line.append(config.getDelimiter());
            }
            line.append(escape(formatValue(row.getField(i))));
        }
        return line.toString();
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Map || value instanceof Collection || value.getClass().isArray()) {
            return GSON.toJson(value);
        }
        return String.valueOf(value);
    }

    private String escape(String field) {
        String delimiter = config.getDelimiter();
        boolean needsQuoting = field.contains(delimiter) || field.contains("\"")
                || field.contains("\n") || field.contains("\r");
        if (!needsQuoting) {
            return field;
        }
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    private static String sanitize(String jobId) {
        return jobId.trim().replaceAll("\\s+", "_");
    }

    private static String stripTrailingSeparator(String path) {
        String trimmed = path.trim();
        int end = trimmed.length();
        while (end > 0 && trimmed.charAt(end - 1) == '/') {
            end--;
        }
        return trimmed.substring(0, end);
    }
}
