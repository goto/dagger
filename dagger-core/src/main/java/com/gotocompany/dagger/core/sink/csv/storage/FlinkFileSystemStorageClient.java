package com.gotocompany.dagger.core.sink.csv.storage;

import org.apache.flink.core.fs.FSDataInputStream;
import org.apache.flink.core.fs.FSDataOutputStream;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * {@link FileStorageClient} backed by Flink's {@link FileSystem} abstraction. The scheme of the
 * supplied path (e.g. {@code file://}, {@code gs://}, {@code oss://}, {@code cosn://}, {@code s3://})
 * selects the underlying filesystem, so credentials and protocol handling are delegated to the
 * filesystem Flink already configures. No cloud-provider SDKs are required.
 */
public class FlinkFileSystemStorageClient implements FileStorageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkFileSystemStorageClient.class);
    private static final long serialVersionUID = 1L;
    private static final int BUFFER_SIZE = 8192;

    @Override
    public boolean exists(String path) throws IOException {
        Path filePath = new Path(path);
        return filePath.getFileSystem().exists(filePath);
    }

    @Override
    public byte[] read(String path) throws IOException {
        Path filePath = new Path(path);
        FileSystem fileSystem = filePath.getFileSystem();
        if (!fileSystem.exists(filePath)) {
            return new byte[0];
        }
        try (FSDataInputStream inputStream = fileSystem.open(filePath);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    @Override
    public void write(String path, byte[] content) throws IOException {
        Path filePath = new Path(path);
        FileSystem fileSystem = filePath.getFileSystem();
        LOGGER.info("Writing {} bytes to {} using filesystem {}", content.length, filePath, fileSystem.getClass().getName());
        try (FSDataOutputStream outputStream = fileSystem.create(filePath, FileSystem.WriteMode.OVERWRITE)) {
            outputStream.write(content);
        }
        LOGGER.info("Finished writing {} bytes to {}", content.length, filePath);
    }
}
