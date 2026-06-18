package com.gotocompany.dagger.core.sink.influx;

import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.types.Row;

import com.google.common.base.Strings;
import com.gotocompany.dagger.common.configuration.Configuration;
import org.apache.flink.util.Preconditions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Flink {@link SinkWriter} that converts each {@link Row} into an InfluxDB {@link Point} and writes
 * it to the configured database through a batched {@link InfluxDB} client.
 *
 * <p>Columns (or, when {@code SINK_INFLUX_USING_ROW_FIELD_NAMES} is enabled, the row's own field
 * names) are mapped onto a point as follows: a {@code window_timestamp} column becomes the point's
 * timestamp (interpreted as UTC), columns prefixed with {@code tag_} become Influx tags, columns
 * prefixed with {@code label_} become tags with the prefix stripped, and any other non-null column
 * becomes a field. Because the underlying client batches and flushes asynchronously, write failures
 * are surfaced through the shared {@link ErrorHandler} and re-checked on every write and at
 * checkpoint time; fatal errors are reported via the {@link ErrorReporter} and rethrown to fail the
 * job. The measurement name and retention policy come from {@link InfluxSinkOverrides} when set,
 * otherwise from configuration.
 */
public class InfluxDBWriter implements SinkWriter<Row, Void, Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBWriter.class.getName());
    /** Target InfluxDB database, from {@code SINK_INFLUX_DB_NAME}. */
    private final String databaseName;
    /** Retention policy applied to writes; taken from overrides when present, otherwise configuration. */
    private final String retentionPolicy;
    /** Influx measurement name; taken from overrides when present, otherwise configuration. */
    private final String measurementName;
    private InfluxDB influxDB;
    private String[] columnNames;
    private ErrorHandler errorHandler;
    private ErrorReporter errorReporter;
    /** When {@code true}, points are built from the row's field names instead of the configured column names. */
    private boolean useRowFieldNames;


    /**
     * Creates an InfluxDB writer, resolving its target database, retention policy and measurement name.
     *
     * <p>The database name and the {@code useRowFieldNames} flag are taken from configuration. The
     * retention policy and measurement name are taken from {@code overrides} when present, otherwise
     * from configuration.
     *
     * @param configuration the job configuration supplying database name, retention policy,
     *                     measurement name and the row-field-name flag defaults
     * @param influxDB      the batched InfluxDB client used to write points
     * @param columnNames   the output column names used to build points when row field names are not used
     * @param errorHandler  the handler that captures asynchronous batch-write failures
     * @param errorReporter the reporter used to surface fatal write failures as metrics
     * @param overrides     optional measurement-name/retention-policy overrides
     */
    public InfluxDBWriter(Configuration configuration, InfluxDB influxDB, String[] columnNames, ErrorHandler errorHandler,
                          ErrorReporter errorReporter, InfluxSinkOverrides overrides) {
        databaseName = configuration.getString(Constants.SINK_INFLUX_DB_NAME_KEY, Constants.SINK_INFLUX_DB_NAME_DEFAULT);
        retentionPolicy = overrides.hasRetentionPolicy()
                ? overrides.getRetentionPolicy()
                : configuration.getString(Constants.SINK_INFLUX_RETENTION_POLICY_KEY, Constants.SINK_INFLUX_RETENTION_POLICY_DEFAULT);
        measurementName = overrides.hasMeasurementName()
                ? overrides.getMeasurementName()
                : configuration.getString(Constants.SINK_INFLUX_MEASUREMENT_NAME_KEY, Constants.SINK_INFLUX_MEASUREMENT_NAME_DEFAULT);
        useRowFieldNames = configuration.getBoolean(Constants.SINK_INFLUX_USING_ROW_FIELD_NAMES_KEY, Constants.SINK_INFLUX_USING_ROW_FIELD_NAMES_DEFAULT);
        this.influxDB = influxDB;
        this.columnNames = columnNames;
        this.errorHandler = errorHandler;
        this.errorReporter = errorReporter;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds an Influx {@link Point} from the row (using the row's field names or the configured
     * column names depending on {@code useRowFieldNames}), checks for any pending asynchronous batch
     * error and rethrows it, then writes the point to the configured database and retention policy. A
     * synchronous write failure is reported as fatal and rethrown.
     *
     * @param row     the output row to write
     * @param context the Flink writer context (unused)
     * @throws IOException          if a pending asynchronous batch error is detected or the write fails
     * @throws InterruptedException if interrupted while writing (declared by the {@link SinkWriter} contract)
     */
    @Override
    public void write(Row row, Context context) throws IOException, InterruptedException {
        LOGGER.info("row to influx: " + row);

        Builder pointBuilder;
        Map<String, Object> fields = new HashMap<>();
        if (useRowFieldNames) {
            pointBuilder = writeUsingRowFieldNames(row, fields);
        } else {
            pointBuilder = writeUsingColumnNames(row, fields);
        }

        addErrorMetricsAndThrow();

        try {
            influxDB.write(databaseName, retentionPolicy, pointBuilder.fields(fields).build());
        } catch (Exception exception) {
            errorReporter.reportFatalException(exception);
            throw exception;
        }
    }

    /**
     * Builds an Influx {@link Point} builder from the row using the configured column names.
     *
     * <p>For each column: {@code window_timestamp} sets the point time (interpreted as UTC), columns
     * prefixed with {@code tag_} are added as tags, columns prefixed with {@code label_} are added as
     * tags with the prefix removed, and any remaining column whose value is non-null and non-empty is
     * collected into {@code fields}.
     *
     * @param row    the row whose field values are read positionally by column index
     * @param fields the mutable map that collects the point's fields, populated as a side effect
     * @return the point builder with measurement, time and tags applied
     */
    private Builder writeUsingColumnNames(Row row, Map<String, Object> fields) {
        Builder pointBuilder = Point.measurement(measurementName);

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            if (columnName.equals("window_timestamp")) {
                LocalDateTime timeField = (LocalDateTime) row.getField(i);
                ZonedDateTime zonedDateTime = timeField.atZone(ZoneOffset.UTC);
                pointBuilder.time(zonedDateTime.toInstant().toEpochMilli(), TimeUnit.MILLISECONDS);
            } else if (columnName.startsWith("tag_")) {
                pointBuilder.tag(columnName, String.valueOf(row.getField(i)));
            } else if (columnName.startsWith("label_")) {
                pointBuilder.tag(columnName.substring("label_".length()), ((String) row.getField(i)));
            } else {
                if (!(Strings.isNullOrEmpty(columnName) || row.getField(i) == null)) {
                    fields.put(columnName, row.getField(i));
                }
            }
        }
        return pointBuilder;
    }

    /**
     * Builds an Influx {@link Point} builder from the row using the row's own field names.
     *
     * <p>Behaves like {@link #writeUsingColumnNames(Row, Map)} but iterates the row's declared field
     * names: a {@code window_timestamp} field sets the point time, {@code tag_} and {@code label_}
     * prefixes map to tags (the latter with the prefix stripped), and the remaining non-null fields
     * become point fields.
     *
     * @param row    the row whose fields are read by name
     * @param fields the mutable map that collects the point's fields, populated as a side effect
     * @return the point builder with measurement, time and tags applied
     * @throws NullPointerException if the row reports no field names
     */
    private Builder writeUsingRowFieldNames(Row row, Map<String, Object> fields) {
        Builder pointBuilder = Point.measurement(measurementName);

        Set<String> fieldNames = row.getFieldNames(false);
        Preconditions.checkNotNull(fieldNames, "Error! in writeUsingRowFieldNames, getFieldNames() returned null");

        for (String fieldName : fieldNames) {
            if (fieldName.equals("window_timestamp")) {
                LocalDateTime timeField = (LocalDateTime) row.getField(fieldName);
                ZonedDateTime zonedDateTime = timeField.atZone(ZoneOffset.UTC);
                pointBuilder.time(zonedDateTime.toInstant().toEpochMilli(), TimeUnit.MILLISECONDS);
            } else if (fieldName.startsWith("tag_")) {
                pointBuilder.tag(fieldName, String.valueOf(row.getField(fieldName)));
            } else if (fieldName.startsWith("label_")) {
                pointBuilder.tag(fieldName.substring("label_".length()), String.valueOf(row.getField(fieldName)));
            } else {
                if (!(Strings.isNullOrEmpty(fieldName) || row.getField(fieldName) == null)) {
                    fields.put(fieldName, row.getField(fieldName));
                }
            }
        }
        return pointBuilder;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This writer produces no committables and relies on the InfluxDB client's own batching, so it
     * performs no work here and returns {@code null}.
     *
     * @param flush whether Flink is requesting a flush of un-staged data (ignored)
     * @return {@code null}, as there is nothing to commit
     * @throws IOException          declared by the {@link SinkWriter} contract; not thrown here
     * @throws InterruptedException declared by the {@link SinkWriter} contract; not thrown here
     */
    @Override
    public List<Void> prepareCommit(boolean flush) throws IOException, InterruptedException {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the underlying {@link InfluxDB} client, flushing and releasing its resources.
     *
     * @throws Exception if the client fails to close cleanly
     */
    @Override
    public void close() throws Exception {
        influxDB.close();
    }


    /**
     * Surfaces any asynchronous batch-write failure captured by the {@link ErrorHandler}.
     *
     * <p>If the handler currently holds an error that carries an exception, that exception is reported
     * as fatal and rethrown, so the failure propagates to Flink instead of being silently swallowed by
     * the asynchronous batch writer.
     *
     * @throws IOException the captured asynchronous write exception, when one is present
     */
    private void addErrorMetricsAndThrow() throws IOException {
        if (errorHandler.getError().isPresent() && errorHandler.getError().get().hasException()) {
            IOException currentException = errorHandler.getError().get().getCurrentException();
            errorReporter.reportFatalException(currentException);
            throw currentException;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Forces a synchronous flush of the InfluxDB client's batch so buffered points are durably
     * written before the checkpoint completes. Pending asynchronous errors are re-checked both before
     * and after the flush and rethrown if present; a flush failure is reported as fatal and rethrown.
     * No writer state is produced, so an empty list is returned.
     *
     * @param checkpointId the id of the checkpoint being taken (unused)
     * @return an empty list of writer state
     * @throws IOException if a pending asynchronous error is detected or the flush fails
     */
    @Override
    public List<Void> snapshotState(long checkpointId) throws IOException {
        addErrorMetricsAndThrow();
        try {
            influxDB.flush();
        } catch (Exception exception) {
            errorReporter.reportFatalException(exception);
            throw exception;
        }
        addErrorMetricsAndThrow();
        return Collections.emptyList();
    }
}
