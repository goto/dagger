package com.gotocompany.dagger.core.sink.log;

import org.apache.flink.api.connector.sink.Committer;
import org.apache.flink.api.connector.sink.GlobalCommitter;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.types.Row;

import java.util.List;
import java.util.Optional;

/**
 * The Log sink.
 */
public class LogSink implements Sink<Row, Void, Void, Void> {
    /** The output column names used to label each row value when it is logged. */
    private final String[] columnNames;

    /**
     * Instantiates a new Log sink.
     *
     * @param columnNames the column names
     */
    public LogSink(String[] columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates a {@link LogSinkWriter} that logs each row using the configured column names.
     *
     * @param context the sink initialization context
     * @param states  the restored writer states; unused because this sink keeps no state
     * @return a new {@link LogSinkWriter}
     */
    @Override
    public SinkWriter createWriter(InitContext context, List states) {
        return new LogSinkWriter(columnNames);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This sink keeps no writer state, so no serializer is provided.
     *
     * @return an empty {@link Optional}
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getWriterStateSerializer() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The log sink writes directly and uses no committer.
     *
     * @return an empty {@link Optional}
     */
    @Override
    public Optional<Committer<Void>> createCommitter() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The log sink requires no global commit phase.
     *
     * @return an empty {@link Optional}
     */
    @Override
    public Optional<GlobalCommitter<Void, Void>> createGlobalCommitter() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>No committables are produced, so no serializer is provided.
     *
     * @return an empty {@link Optional}
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getCommittableSerializer() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>No global committables are produced, so no serializer is provided.
     *
     * @return an empty {@link Optional}
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getGlobalCommittableSerializer() {
        return Optional.empty();
    }
}
