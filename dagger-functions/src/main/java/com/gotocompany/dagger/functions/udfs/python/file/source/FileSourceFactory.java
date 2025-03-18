package com.gotocompany.dagger.functions.udfs.python.file.source;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.functions.common.Constants;
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
    public static FileSource getFileSource(String pythonFile, Configuration configuration) {
        if ("GS".equals(getFileSourcePrefix(pythonFile))) {
            return new GcsFileSource(pythonFile);
        } else if ("OSS".equals(getFileSourcePrefix(pythonFile))) {
            return new OssFileSource(
                    pythonFile,
                    configuration.getString(Constants.OSS_ENDPOINT, Constants.DEFAULT_OSS_ENDPOINT)
            );
        } else if ("COSN".equals(getFileSourcePrefix(pythonFile))) {
            return new CosFileSource(
                    pythonFile,
                    configuration.getBoolean(Constants.ENABLE_TKE_OIDC_PROVIDER, false),
                    configuration.getString(Constants.COS_REGION, Constants.DEFAULT_COS_REGION)
            );
        } else {
            return new LocalFileSource(pythonFile);
        }
    }

    private static String getFileSourcePrefix(String pythonFile) {
        String[] files = pythonFile.split("://");
        return files[0].toUpperCase();
    }
}
