package com.gotocompany.dagger.core.sink.csv;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.sink.csv.storage.FileStorageClient;
import com.gotocompany.dagger.core.sink.csv.storage.FlinkFileSystemStorageClient;
import com.gotocompany.dagger.core.sink.csv.writemode.FileWriteStrategy;
import com.gotocompany.dagger.core.sink.csv.writemode.FileWriteStrategyFactory;
import com.gotocompany.dagger.core.utils.Constants;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Builds a {@link CsvSink} from Dagger {@link Configuration}, wiring the write strategy and the
 * Flink filesystem-backed storage client. SINK_CSV_BASE_PATH is required; everything else has a
 * sensible default defined in {@link Constants}.
 */
public class CsvSinkBuilder {

    private static final Pattern ALLOWED_DATE_FORMAT = Pattern.compile("^[A-Za-z_-]+$");

    private CsvSinkBuilder() {
    }

    public static CsvSink build(Configuration configuration, String[] columnNames) {
        String basePath = configuration.getString(Constants.SINK_CSV_BASE_PATH_KEY, "");
        if (basePath == null || basePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required configuration '" + Constants.SINK_CSV_BASE_PATH_KEY
                    + "' for CSV sink. Example: oss://bucket-name/some-folder");
        }

        String dateFormat = configuration.getString(Constants.SINK_CSV_PARTITION_DATE_FORMAT_KEY, Constants.SINK_CSV_PARTITION_DATE_FORMAT_DEFAULT);
        validateDateFormat(dateFormat);

        String writeMode = configuration.getString(Constants.SINK_CSV_WRITE_MODE_KEY, Constants.SINK_CSV_WRITE_MODE_DEFAULT);
        String delimiter = configuration.getString(Constants.SINK_CSV_DELIMITER_KEY, Constants.SINK_CSV_DELIMITER_DEFAULT);
        boolean writeHeader = configuration.getBoolean(Constants.SINK_CSV_WRITE_HEADER_KEY, Constants.SINK_CSV_WRITE_HEADER_DEFAULT);
        String filenamePrefix = configuration.getString(Constants.SINK_CSV_FILENAME_PREFIX_KEY, Constants.SINK_CSV_FILENAME_PREFIX_DEFAULT);
        String jobId = configuration.getString(Constants.FLINK_JOB_ID_KEY, Constants.FLINK_JOB_ID_DEFAULT);

        CsvSinkConfig config = new CsvSinkConfig(basePath.trim(), jobId, filenamePrefix, dateFormat, delimiter, writeHeader);
        FileWriteStrategy writeStrategy = FileWriteStrategyFactory.getWriteStrategy(writeMode);
        FileStorageClient storageClient = new FlinkFileSystemStorageClient();

        return new CsvSink(columnNames, config, storageClient, writeStrategy);
    }

    private static void validateDateFormat(String dateFormat) {
        if (dateFormat == null || !ALLOWED_DATE_FORMAT.matcher(dateFormat).matches()) {
            throw new IllegalArgumentException("Invalid '" + Constants.SINK_CSV_PARTITION_DATE_FORMAT_KEY + "' value '" + dateFormat
                    + "' for CSV sink. Allowed characters: pattern letters (y, M, d, H, m, s, ...) and the separators "
                    + "'-' and '_'. Characters such as '/', ':', '|', '.', and spaces are not allowed.");
        }
        try {
            DateTimeFormatter.ofPattern(dateFormat, Locale.ENGLISH);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid '" + Constants.SINK_CSV_PARTITION_DATE_FORMAT_KEY + "' value '" + dateFormat
                    + "' for CSV sink: not a valid date-time pattern.", e);
        }
    }
}
