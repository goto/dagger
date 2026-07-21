package com.gotocompany.dagger.core.sink.csv.writemode;

import com.gotocompany.dagger.core.sink.csv.storage.FileStorageClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Fully replaces the daily file with the buffered lines on each flush, producing a snapshot of the
 * most recent window. When the buffer is empty the file is left untouched (no blanking between
 * windows). Restart-safe by construction: a replayed window simply overwrites with the same content.
 */
public class OverwriteWriteStrategy implements FileWriteStrategy {

    private static final long serialVersionUID = 1L;
    private static final String LINE_SEPARATOR = "\n";

    @Override
    public void flush(FileStorageClient storageClient, String path, String headerLine, List<String> bufferedLines) throws IOException {
        if (bufferedLines.isEmpty()) {
            return;
        }
        StringBuilder content = new StringBuilder();
        if (headerLine != null) {
            content.append(headerLine).append(LINE_SEPARATOR);
        }
        for (String line : bufferedLines) {
            content.append(line).append(LINE_SEPARATOR);
        }
        storageClient.write(path, content.toString().getBytes(StandardCharsets.UTF_8));
    }
}
