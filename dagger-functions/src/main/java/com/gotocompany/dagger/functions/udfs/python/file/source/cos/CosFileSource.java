package com.gotocompany.dagger.functions.udfs.python.file.source.cos;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.IOException;

public class CosFileSource implements FileSource {

    private CosFileClient cosFileClient;
    private final String pythonFile;
    private final String cosRegion;
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
