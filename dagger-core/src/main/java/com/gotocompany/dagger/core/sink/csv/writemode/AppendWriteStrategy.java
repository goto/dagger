package com.gotocompany.dagger.core.sink.csv.writemode;

import com.gotocompany.dagger.core.sink.csv.storage.FileStorageClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Appends the buffered lines to the existing daily file (read-modify-write, since object stores do
 * not support true append). The header is written only when the file is new/empty. Produces a
 * growing time-series file. At-least-once: on restart, replayed rows may be appended again.
 */
public class AppendWriteStrategy implements FileWriteStrategy {

    private static final long serialVersionUID = 1L;
    private static final String LINE_SEPARATOR = "\n";

    @Override
    public void flush(FileStorageClient storageClient, String path, String headerLine, List<String> bufferedLines) throws IOException {
        if (bufferedLines.isEmpty()) {
            return;
        }
        byte[] existingContent = storageClient.read(path);
        boolean fileIsEmpty = existingContent == null || existingContent.length == 0;

        StringBuilder content = new StringBuilder();
        if (fileIsEmpty) {
            if (headerLine != null) {
                content.append(headerLine).append(LINE_SEPARATOR);
            }
        } else {
            content.append(new String(existingContent, StandardCharsets.UTF_8));
        }
        for (String line : bufferedLines) {
            content.append(line).append(LINE_SEPARATOR);
        }
        storageClient.write(path, content.toString().getBytes(StandardCharsets.UTF_8));
    }
}
