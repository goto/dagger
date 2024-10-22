package com.gotocompany.dagger.functions.udfs.scalar.dart.store.cos;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.functions.exceptions.TagDoesNotExistException;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartAspects;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.DartDataStoreClient;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.IOUtils;

import java.io.IOException;

import static com.gotocompany.dagger.common.core.Constants.UDF_TELEMETRY_GROUP_KEY;

public class CosDartClient implements DartDataStoreClient {
    private static final Double BYTES_TO_KB = 1024.0;
    private static final String DART_PATH = "dartpath";

    private static final String ENV_COS_SECRET_ID = "COS_SECRET_ID";
    private static final String ENV_COS_SECRET_KEY = "COS_SECRET_KEY";
    private static final String ENV_COS_REGION = "COS_REGION";

    private final COSClient libCosClient;

    /**
     * Instantiates a new Cos client.
     */
    public CosDartClient() {
        String secretID = System.getenv(ENV_COS_SECRET_ID);
        String secretKey = System.getenv(ENV_COS_SECRET_KEY);
        String region = System.getenv(ENV_COS_REGION); // ap-singapore

        COSCredentials credentials = new BasicCOSCredentials(secretID, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        libCosClient = new COSClient(credentials, clientConfig);
    }


    public String fetchJsonData(String udfName, GaugeStatsManager gaugeStatsManager, String bucketName, String dartName) {
        COSObject cosObject = libCosClient.getObject(bucketName, dartName);
        String dartJson;
        byte[] contentByteArray;
        try (COSObjectInputStream inputStream = cosObject.getObjectContent()) {
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
     * Instantiates a new Cos client.
     * This constructor used for unit test purposes.
     *
     * @param libCosClient the storage
     */
    public CosDartClient(COSClient libCosClient) {
        this.libCosClient = libCosClient;
    }
}
