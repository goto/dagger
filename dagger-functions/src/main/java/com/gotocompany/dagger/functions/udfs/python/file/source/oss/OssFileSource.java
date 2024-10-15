package com.gotocompany.dagger.functions.udfs.python.file.source.oss;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.IOException;

public class OssFileSource implements FileSource {

    private OssClient ossClient;
    private final String pythonFile;

    /**
     * Instantiates a new Oss file source.
     *
     * @param pythonFile the python file
     */
    public OssFileSource(String pythonFile) {
        this.pythonFile = pythonFile;
    }

    /**
     * Instantiates a new Oss file source.
     *
     * @param pythonFile the python file
     */
    public OssFileSource(String pythonFile, OssClient ossClient) {
        this.pythonFile = pythonFile;
        this.ossClient = ossClient;
    }

    @Override
    public byte[] getObjectFile() throws IOException {
        return getOssClient().getFile(pythonFile);
    }

    /**
     * Gets oss client.
     *
     * @return the oss client
     */
    private OssClient getOssClient() {
        if (this.ossClient == null) {
            this.ossClient = new OssClient();
        }
        return this.ossClient;
    }
}
