package com.gotocompany.dagger.functions.udfs.python.file.source.oss;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.IOException;

/**
 * {@link FileSource} implementation that reads a Python UDF artifact from Alibaba Cloud
 * Object Storage Service (OSS).
 *
 * <p>Delegates the actual download to a lazily-created {@link OssClient}, allowing a
 * pre-built client to be injected for testing.
 */
public class OssFileSource implements FileSource {
    /**
     * OSS client used to fetch the object; created lazily on first use unless injected.
     */
    private OssClient ossClient;
    /**
     * The {@code oss://} location of the Python file to download.
     */
    private final String pythonFile;
    /**
     * OSS endpoint used to construct the client when one is not injected.
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>Lazily obtains an {@link OssClient} and downloads the configured OSS object,
     * returning its raw bytes.
     *
     * @return the file content downloaded from OSS
     * @throws IOException if the object cannot be read from OSS
     */
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
