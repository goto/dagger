package com.gotocompany.dagger.functions.udfs.python.file.source;

import com.gotocompany.dagger.functions.udfs.python.file.source.cos.CosFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.gcs.GcsFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.local.LocalFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.oss.OssFileSource;

/**
 * The type File source factory.
 */
public class FileSourceFactory {

    /**
     * Gets file source.
     *
     * @param pythonFile the python file
     * @return the file source
     */
    public static FileSource getFileSource(String pythonFile) {
        if ("GS".equals(getFileSourcePrefix(pythonFile))) {
            return new GcsFileSource(pythonFile);
        } else if ("OSS".equals(getFileSourcePrefix(pythonFile))) {
            return new OssFileSource(pythonFile);
        } else if ("COSN".equals(getFileSourcePrefix(pythonFile))) {
            return new CosFileSource(pythonFile);
        } else {
            return new LocalFileSource(pythonFile);
        }
    }

    private static String getFileSourcePrefix(String pythonFile) {
        String[] files = pythonFile.split("://");
        return files[0].toUpperCase();
    }
}
