package com.gotocompany.dagger.functions.udfs.scalar.dart.store.oss;

import com.aliyun.core.utils.IOUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.model.OSSObject;
import com.aliyuncs.exceptions.ClientException;
import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.functions.exceptions.TagDoesNotExistException;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartAspects;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.DartDataStoreClient;

import java.io.IOException;
import java.io.InputStream;

import static com.gotocompany.dagger.common.core.Constants.UDF_TELEMETRY_GROUP_KEY;

/**
 * The type Oss client.
 *
 * <p>A {@link DartDataStoreClient} implementation that fetches dart JSON payloads from Alibaba Cloud
 * Object Storage Service (OSS) buckets.
 */
public class OssDartClient implements DartDataStoreClient {
    /**
     * The divisor used to convert object sizes from bytes to kilobytes when reporting file-size telemetry.
     */
    private static final Double BYTES_TO_KB = 1024.0;
    /**
     * The gauge group key under which the dart path is registered for file-size telemetry.
     */
    private static final String DART_PATH = "dartpath";

    /**
     * The underlying Alibaba Cloud OSS client used to read dart objects.
     */
    private final OSS libOssClient;

    /**
     * Instantiates a new Oss client.
     */
    public OssDartClient(String ossEndpoint) {
        try {
            libOssClient = new OSSClientBuilder().build(ossEndpoint, CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider());
        } catch (ClientException e) {
            throw new RuntimeException("failed to initialise oss client", e);
        }
    }

    /**
     * Fetches the dart JSON payload for the given object from the configured OSS bucket.
     *
     * <p>Reads the object content as a string and records dart path and file-size telemetry via the
     * supplied gauge manager.
     *
     * @param udfName           the simple name of the UDF requesting the data, used as a telemetry group
     * @param gaugeStatsManager the gauge manager used to record path and size telemetry
     * @param bucketName        the name of the OSS bucket to read from
     * @param dartName          the object key of the dart payload within the bucket
     * @return the dart payload contents as a string
     * @throws TagDoesNotExistException if the object content cannot be read from OSS
     */
    public String fetchJsonData(String udfName, GaugeStatsManager gaugeStatsManager, String bucketName, String dartName) {
        OSSObject ossObject = libOssClient.getObject(bucketName, dartName);
        String dartJson;
        byte[] contentByteArray;
        try (InputStream inputStream = ossObject.getObjectContent()) {
            contentByteArray = IOUtils.toByteArray(inputStream);
            dartJson = new String(contentByteArray);
        } catch (IOException e) {
            throw new TagDoesNotExistException("Could not find the content in oss for + dartName", e);
        }
        gaugeStatsManager.registerString(UDF_TELEMETRY_GROUP_KEY, udfName, DartAspects.DART_GCS_PATH.getValue(), dartName);
        gaugeStatsManager.registerDouble(DART_PATH, dartName, DartAspects.DART_GCS_FILE_SIZE.getValue(), contentByteArray.length / BYTES_TO_KB);
        return dartJson;
    }

    /**
     * Instantiates a new OSS client.
     * This constructor used for unit test purposes.
     *
     * @param libOssClient the storage
     */
    public OssDartClient(OSS libOssClient) {
        this.libOssClient = libOssClient;
    }
}
