package com.gotocompany.dagger.core.sink.csv.storage;

import java.io.IOException;
import java.io.Serializable;

/**
 * Abstraction over the destination file store used by the CSV sink. Implementations are expected to
 * support whole-object read and overwrite semantics (object stores like GCS/OSS/COS/S3 do not allow
 * true append, so the sink relies on read-modify-write).
 */
public interface FileStorageClient extends Serializable {

    /**
     * @return true if an object already exists at the given path.
     */
    boolean exists(String path) throws IOException;

    /**
     * Reads the full content of the object at the given path.
     *
     * @return the object bytes, or an empty array if the object does not exist.
     */
    byte[] read(String path) throws IOException;

    /**
     * Writes (creating or fully replacing) the object at the given path with the given content.
     */
    void write(String path, byte[] content) throws IOException;
}
