package com.gotocompany.dagger.functions.udfs.python.file.source.local;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The type Local file source.
 */
public class LocalFileSource implements FileSource {

    /**
     * Absolute or relative path on the local file system to the Python file to read.
     */
    private String pythonFile;

    /**
     * Instantiates a new Local file source.
     *
     * @param pythonFile the python file
     */
    public LocalFileSource(String pythonFile) {
        this.pythonFile = pythonFile;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reads the configured local file in full and returns its raw bytes.
     *
     * @return the content of the local Python file
     * @throws IOException if the file cannot be read
     */
    @Override
    public byte[] getObjectFile() throws IOException {
        return Files.readAllBytes(Paths.get(pythonFile));
    }
}
