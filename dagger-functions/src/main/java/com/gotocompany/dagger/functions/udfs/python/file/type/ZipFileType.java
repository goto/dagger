package com.gotocompany.dagger.functions.udfs.python.file.type;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The type Zip file type.
 */
public class ZipFileType implements FileType {

    /**
     * Source from which the raw ZIP archive bytes are obtained before extraction.
     */
    private FileSource fileSource;

    /**
     * Creates a ZIP file type backed by the given file source.
     *
     * @param fileSource the source supplying the raw ZIP archive bytes
     */
    public ZipFileType(FileSource fileSource) {
        this.fileSource = fileSource;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Downloads the ZIP archive via the configured {@link FileSource}, iterates over its
     * entries and collects the names of those that are Python ({@code .py}) files.
     *
     * @return the names of the Python files contained in the archive
     * @throws IOException if the archive cannot be read from the underlying source
     */
    @Override
    public List<String> getFileNames() throws IOException {
        byte[] object = fileSource.getObjectFile();

        ZipInputStream zi = new ZipInputStream(new ByteArrayInputStream(object));
        ZipEntry zipEntry;
        List<ZipEntry> entries = new ArrayList<>();
        while ((zipEntry = zi.getNextEntry()) != null) {
            entries.add(zipEntry);
        }

        List<String> fileNames = new ArrayList<>();
        for (ZipEntry entry : entries) {
            String name = entry.getName();
            if (isPythonFile(name)) {
                fileNames.add(name);
            }
        }
        return fileNames;
    }

    /**
     * Determines whether the given entry name refers to a Python source file.
     *
     * @param fileName the ZIP entry name to test
     * @return {@code true} if the name ends with {@code .py}, otherwise {@code false}
     */
    private boolean isPythonFile(String fileName) {
        return fileName.endsWith(".py");
    }
}
