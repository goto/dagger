package com.gotocompany.dagger.functions.udfs.python.file.source.cos;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;

import java.io.IOException;

public class CosFileSource implements FileSource {

    private CosClient cosClient;
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
    public CosFileSource(String pythonFile, CosClient cosClient) {
        this.pythonFile = pythonFile;
        this.cosClient = cosClient;
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
    private CosClient getCosClient() {
        if (this.cosClient == null) {
            this.cosClient = new CosClient();
        }
        return this.cosClient;
    }
}
