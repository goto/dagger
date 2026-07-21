package com.gotocompany.dagger.core.sink.csv;

import java.io.Serializable;
import java.time.ZoneId;

/**
 * Immutable, serializable configuration for the CSV sink. The full output path for a given day is
 * {@code basePath/<sanitized jobId>/<filenamePrefix>-<date>.csv}.
 */
public class CsvSinkConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String basePath;
    private final String jobId;
    private final String filenamePrefix;
    private final String dateFormat;
    private final ZoneId zoneId;
    private final String delimiter;
    private final boolean writeHeader;

    public CsvSinkConfig(String basePath, String jobId, String filenamePrefix, String dateFormat, ZoneId zoneId, String delimiter, boolean writeHeader) {
        this.basePath = basePath;
        this.jobId = jobId;
        this.filenamePrefix = filenamePrefix;
        this.dateFormat = dateFormat;
        this.zoneId = zoneId;
        this.delimiter = delimiter;
        this.writeHeader = writeHeader;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getJobId() {
        return jobId;
    }

    public String getFilenamePrefix() {
        return filenamePrefix;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public boolean isWriteHeader() {
        return writeHeader;
    }
}
