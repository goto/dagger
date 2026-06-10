package com.gotocompany.dagger.core.sink.csv;

import com.gotocompany.dagger.core.sink.csv.storage.FileStorageClient;
import com.gotocompany.dagger.core.sink.csv.writemode.FileWriteStrategy;
import com.google.gson.Gson;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Buffers formatted CSV rows and flushes them on every checkpoint (snapshotState) and on close.
 * The destination path rolls over daily: {@code basePath/<jobId>/<prefix>-<date>.csv}. Whether the
 * flush appends or fully replaces the file is decided by the configured {@link FileWriteStrategy}.
 */
public class CsvSinkWriter implements SinkWriter<Row, Void, Void> {

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
    }

    @Override
    public void write(Row row, Context context) {
        bufferedLines.add(formatRow(row));
    }

    @Override
    public List<Void> snapshotState(long checkpointId) throws IOException {
        flush();
        return Collections.emptyList();
    }

    @Override
    public List<Void> prepareCommit(boolean flush) {
        return Collections.emptyList();
    }

    @Override
    public void close() throws Exception {
        flush();
    }

    private void flush() throws IOException {
        writeStrategy.flush(storageClient, buildPath(), buildHeaderLine(), bufferedLines);
        bufferedLines.clear();
    }

    private String buildPath() {
        String date = LocalDate.now(clock).format(dateTimeFormatter);
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
        if (path.endsWith(PATH_SEPARATOR)) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
