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

public class OssDartClient implements DartDataStoreClient {

    private static final String ENV_OSS_ENDPOINT = "OSS_ENDPOINT";
    private static final String DEFAULT_OSS_ENDPOINT = "oss-ap-southeast-1.aliyuncs.com";

    private static final Double BYTES_TO_KB = 1024.0;
    private static final String DART_PATH = "dartpath";

    private final OSS libOssClient;

    /**
     * Instantiates a new Oss client.
     */
    public OssDartClient() {
        String endpoint = System.getenv(ENV_OSS_ENDPOINT);
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = DEFAULT_OSS_ENDPOINT;
        }
        try {
            libOssClient = new OSSClientBuilder().build(endpoint, CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider());
        } catch (ClientException e) {
            throw new RuntimeException("failed to initialise oss client", e);
        }
    }

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
