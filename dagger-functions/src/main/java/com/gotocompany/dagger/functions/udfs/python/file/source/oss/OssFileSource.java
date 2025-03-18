package com.gotocompany.dagger.functions.udfs.python.file.source.oss;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.IOException;

public class OssFileSource implements FileSource {
    private OssClient ossClient;
    private final String pythonFile;
    private final String ossEndpoint;

    /**
     * Instantiates a new Oss file source.
     *
     * @param pythonFile the python file
     */
    public OssFileSource(String pythonFile, String ossEndpoint) {
        this.pythonFile = pythonFile;
        this.ossEndpoint = ossEndpoint;
    }

    /**
     * TestOnly
     * Instantiates a new Oss file source.
     *
     * @param pythonFile the python file
     */
    public OssFileSource(String pythonFile, OssClient ossClient, String ossEndpoint) {
        this.pythonFile = pythonFile;
        this.ossClient = ossClient;
        this.ossEndpoint = ossEndpoint;
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
            this.ossClient = new OssClient(this.ossEndpoint);
        }
        return this.ossClient;
    }
}
