package com.gotocompany.dagger.functions.udfs.python.file.source.gcs;

import com.gotocompany.dagger.functions.udfs.python.file.source.FileSource;


/**
 * The type Gcs file source.
 */
public class GcsFileSource implements FileSource {

    /**
     * GCS client used to fetch the object; created lazily on first use unless injected.
     */
    private GcsClient gcsClient;
    /**
     * The {@code gs://} location of the Python file to download.
     */
    private String pythonFile;

    /**
     * Instantiates a new Gcs file source.
     *
     * @param pythonFile the python file
     */
    public GcsFileSource(String pythonFile) {
        this.pythonFile = pythonFile;
    }

    /**
     * Instantiates a new Gcs file source.
     * This constructor used for unit test purposes.
     *
     * @param pythonFile the python file
     * @param gcsClient  the gcs client
     */
    public GcsFileSource(String pythonFile, GcsClient gcsClient) {
        this.pythonFile = pythonFile;
        this.gcsClient = gcsClient;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily obtains a {@link GcsClient} and downloads the configured GCS object,
     * returning its raw bytes.
     *
     * @return the file content downloaded from Google Cloud Storage
     */
    @Override
    public byte[] getObjectFile() {
        return getGcsClient().getFile(pythonFile);
    }

    /**
     * Gets gcs client.
     *
     * @return the gcs client
     */
    private GcsClient getGcsClient() {
        if (this.gcsClient == null) {
            this.gcsClient = new GcsClient();
        }
        return this.gcsClient;
    }
}
