package com.gotocompany.dagger.core.sink.csv;

import com.gotocompany.dagger.core.sink.csv.storage.FileStorageClient;
import com.gotocompany.dagger.core.sink.csv.writemode.FileWriteStrategy;
import org.apache.flink.api.connector.sink.Committer;
import org.apache.flink.api.connector.sink.GlobalCommitter;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.types.Row;

import java.util.List;
import java.util.Optional;

/**
 * Sink that writes the output {@link Row}s as a daily-rolling CSV file to any Flink filesystem.
 * Buffered rows are flushed on every checkpoint (via the writer's snapshotState). At-least-once;
 * the sink must run with parallelism 1 since a single daily object cannot be written concurrently.
 */
public class CsvSink implements Sink<Row, Void, Void, Void> {

    private final String[] columnNames;
    private final CsvSinkConfig config;
    private final FileStorageClient storageClient;
    private final FileWriteStrategy writeStrategy;

    public CsvSink(String[] columnNames, CsvSinkConfig config, FileStorageClient storageClient, FileWriteStrategy writeStrategy) {
        this.columnNames = columnNames;
        this.config = config;
        this.storageClient = storageClient;
        this.writeStrategy = writeStrategy;
    }

    @Override
    public SinkWriter<Row, Void, Void> createWriter(InitContext context, List<Void> states) {
        return new CsvSinkWriter(columnNames, config, storageClient, writeStrategy);
    }

    @Override
    public Optional<SimpleVersionedSerializer<Void>> getWriterStateSerializer() {
        return Optional.empty();
    }

    @Override
    public Optional<Committer<Void>> createCommitter() {
        return Optional.empty();
    }

    @Override
    public Optional<GlobalCommitter<Void, Void>> createGlobalCommitter() {
        return Optional.empty();
    }

    @Override
    public Optional<SimpleVersionedSerializer<Void>> getCommittableSerializer() {
        return Optional.empty();
    }

    @Override
    public Optional<SimpleVersionedSerializer<Void>> getGlobalCommittableSerializer() {
        return Optional.empty();
    }
}
