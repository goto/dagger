package com.gotocompany.dagger.functions.udfs.python.file.source.cos;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.IOException;

/**
 * {@link FileSource} implementation that reads a Python UDF artifact from Tencent Cloud
 * Object Storage (COS).
 *
 * <p>Delegates the actual download to a lazily-created {@link CosFileClient}, allowing a
 * pre-built client to be injected for testing.
 */
public class CosFileSource implements FileSource {

    /**
     * COS client used to fetch the object; created lazily on first use unless injected.
     */
    private CosFileClient cosFileClient;
    /**
     * The {@code cosn://} location of the Python file to download.
     */
    private final String pythonFile;
    /**
     * Tencent Cloud region of the COS bucket holding the file.
     */
    private final String cosRegion;
    /**
     * Whether the COS client should authenticate via the TKE OIDC provider.
     */
    private final boolean enableTkeOidcProvider;

    /**
     * Instantiates a new Cos file source.
     *
     * @param pythonFile the python file
     */
    public CosFileSource(String pythonFile, boolean enableTkeOidcProvider, String cosRegion) {
        this.pythonFile = pythonFile;
        this.cosRegion = cosRegion;
        this.enableTkeOidcProvider = enableTkeOidcProvider;
    }

    /**
     * TestONLY
     * Instantiates a new Cos file source.
     *
     * @param pythonFile the python file
     */
    public CosFileSource(String pythonFile, CosFileClient cosFileClient, boolean enableTkeOidcProvider, String cosRegion) {
        this.pythonFile = pythonFile;
        this.cosFileClient = cosFileClient;
        this.cosRegion = cosRegion;
        this.enableTkeOidcProvider = enableTkeOidcProvider;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily obtains a {@link CosFileClient} and downloads the configured COS object,
     * returning its raw bytes.
     *
     * @return the file content downloaded from COS
     * @throws IOException if the object cannot be read from COS
     */
    @Override
    public byte[] getObjectFile() throws IOException {
        return getCosClient().getFile(pythonFile);
    }

    /**
     * Gets cos client.
     *
     * @return the cos client
     */
    private CosFileClient getCosClient() {
        if (this.cosFileClient == null) {
            this.cosFileClient = new CosFileClient(enableTkeOidcProvider, cosRegion);
        }
        return this.cosFileClient;
    }
}
