package com.gotocompany.dagger.functions.udfs.python.file.source.cos;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.IOException;

public class CosFileSource implements FileSource {

    private CosFileClient cosFileClient;
    private final String pythonFile;

    /**
     * Instantiates a new Cos file source.
     *
     * @param pythonFile the python file
     */
    public CosFileSource(String pythonFile) {
        this.pythonFile = pythonFile;
    }

    /**
     * Instantiates a new Cos file source.
     *
     * @param pythonFile the python file
     */
    public CosFileSource(String pythonFile, CosFileClient cosFileClient) {
        this.pythonFile = pythonFile;
        this.cosFileClient = cosFileClient;
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
            this.cosFileClient = new CosFileClient();
        }
        return this.cosFileClient;
    }
}
