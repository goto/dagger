package com.gotocompany.dagger.core.sink.csv;

import com.gotocompany.dagger.core.sink.csv.storage.FileStorageClient;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory {@link FileStorageClient} for tests. Records content per path and counts writes so tests
 * can assert no-op behaviour.
 */
public class InMemoryFileStorageClient implements FileStorageClient {

    private final Map<String, byte[]> files = new HashMap<>();
    private int writeCount = 0;

    @Override
    public boolean exists(String path) {
        return files.containsKey(path);
    }

    @Override
    public byte[] read(String path) {
        return files.getOrDefault(path, new byte[0]);
    }

    @Override
    public void write(String path, byte[] content) {
        files.put(path, content);
        writeCount++;
    }

    public String readAsString(String path) {
        return new String(files.getOrDefault(path, new byte[0]), StandardCharsets.UTF_8);
    }

    public int getWriteCount() {
        return writeCount;
    }
}
