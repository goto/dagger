package com.gotocompany.dagger.core.sink.log;

import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.types.Row;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flink {@link SinkWriter} that logs each output {@link Row} as a column-name-to-value map at info
 * level.
 *
 * <p>Created by {@link LogSink}, it is intended for local development and debugging rather than for
 * writing to an external system. It buffers nothing, produces no committables and holds no state.
 */
public class LogSinkWriter implements SinkWriter<Row, Void, Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogSinkWriter.class.getName());
    private final String[] columnNames;

    /**
     * Creates a log sink writer.
     *
     * @param columnNames the output column names used to label each row field in the logged map
     */
    public LogSinkWriter(String[] columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds a map from each non-null row field to its string value, keyed by the corresponding
     * column name, and logs it at info level.
     *
     * @param row     the output row to log
     * @param context the Flink writer context (unused)
     */
    @Override
    public void write(Row row, Context context) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < columnNames.length; i++) {
            Object field = row.getField(i);
            if (field != null) {
                map.put(columnNames[i], field.toString());
            }
        }
        LOGGER.info(map.toString());
    }

    /**
     * {@inheritDoc}
     *
     * <p>This writer produces no committables, so it does no work and returns {@code null}.
     *
     * @param flush whether Flink is requesting a flush of un-staged data (ignored)
     * @return {@code null}, as there is nothing to commit
     */
    @Override
    public List<Void> prepareCommit(boolean flush) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>No resources are held, so this is a no-op.
     *
     * @throws Exception declared by the {@link SinkWriter} contract; not thrown here
     */
    @Override
    public void close() throws Exception {

    }
}
