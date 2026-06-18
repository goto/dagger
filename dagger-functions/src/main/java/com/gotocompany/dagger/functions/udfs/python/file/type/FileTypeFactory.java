package com.gotocompany.dagger.functions.udfs.python.file.type;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.functions.exceptions.PythonFilesFormatException;
import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.FileSourceFactory;

/**
 * The type File type factory.
 */
public class FileTypeFactory {

    /**
     * Gets file type.
     *
     * @param pythonFile the python file
     * @return the file type
     */
    public static FileType getFileType(String pythonFile, Configuration configuration) {
        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile, configuration);
        switch (getFileTypeFormat(pythonFile)) {
            case "PY":
                return new PythonFileType(pythonFile);
            case "ZIP":
                return new ZipFileType(fileSource);
            default:
                throw new PythonFilesFormatException("Python files should be in .py or .zip format");
        }
    }

    /**
     * Determines the file format of a Python file from its extension.
     *
     * <p>The substring after the final {@code .} is returned upper-cased, for example
     * {@code PY} or {@code ZIP}, and is used to select the matching {@link FileType}.
     *
     * @param pythonFile the configured Python file location
     * @return the upper-cased file extension
     */
    private static String getFileTypeFormat(String pythonFile) {
        String[] files = pythonFile.split("\\.");
        return files[files.length - 1].toUpperCase();
    }
}
