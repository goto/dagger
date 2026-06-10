package com.gotocompany.dagger.core.sink.csv.storage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FlinkFileSystemStorageClientTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private FlinkFileSystemStorageClient storageClient;

    @Before
    public void setup() {
        storageClient = new FlinkFileSystemStorageClient();
    }

    private String pathFor(String fileName) {
        return new File(temporaryFolder.getRoot(), fileName).getAbsolutePath();
    }

    @Test
    public void shouldReturnFalseWhenFileDoesNotExist() throws Exception {
        assertFalse(storageClient.exists(pathFor("missing.csv")));
    }

    @Test
    public void shouldReturnEmptyBytesWhenReadingMissingFile() throws Exception {
        assertEquals(0, storageClient.read(pathFor("missing.csv")).length);
    }

    @Test
    public void shouldWriteAndReadBackContent() throws Exception {
        String path = pathFor("output.csv");
        byte[] content = "id,name\n1,foo\n".getBytes(StandardCharsets.UTF_8);

        storageClient.write(path, content);

        assertTrue(storageClient.exists(path));
        assertArrayEquals(content, storageClient.read(path));
    }

    @Test
    public void shouldOverwriteExistingContent() throws Exception {
        String path = pathFor("output.csv");

        storageClient.write(path, "first".getBytes(StandardCharsets.UTF_8));
        storageClient.write(path, "second".getBytes(StandardCharsets.UTF_8));

        assertEquals("second", new String(storageClient.read(path), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldCreateParentDirectoriesWhenWriting() throws Exception {
        String path = pathFor("my-job-id/nested/output.csv");
        byte[] content = "a,b\n1,2\n".getBytes(StandardCharsets.UTF_8);

        storageClient.write(path, content);

        assertArrayEquals(content, storageClient.read(path));
    }
}
