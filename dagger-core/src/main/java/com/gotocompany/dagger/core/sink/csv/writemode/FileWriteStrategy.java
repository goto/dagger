package com.gotocompany.dagger.core.sink.csv.writemode;

import com.gotocompany.dagger.core.sink.csv.storage.FileStorageClient;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Strategy for turning a buffer of formatted CSV lines into a write against the destination file.
 * Implementations decide whether the new lines are appended to the existing daily file or fully
 * replace it. All implementations must be a no-op when the buffer is empty (so empty checkpoints do
 * not blank or needlessly rewrite the file).
 */
public interface FileWriteStrategy extends Serializable {

    /**
     * @param storageClient the destination store.
     * @param path          the full target object path for the current day.
     * @param headerLine    the CSV header line, or null when headers are disabled.
     * @param bufferedLines the formatted CSV rows accumulated since the last flush.
     */
    void flush(FileStorageClient storageClient, String path, String headerLine, List<String> bufferedLines) throws IOException;
}
