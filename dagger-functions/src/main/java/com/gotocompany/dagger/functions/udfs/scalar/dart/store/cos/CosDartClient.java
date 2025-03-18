package com.gotocompany.dagger.functions.udfs.scalar.dart.store.cos;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.functions.common.CosLibClient;
import com.gotocompany.dagger.functions.exceptions.TagDoesNotExistException;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartAspects;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.DartDataStoreClient;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;

import java.io.IOException;

import static com.gotocompany.dagger.common.core.Constants.UDF_TELEMETRY_GROUP_KEY;

public class CosDartClient implements DartDataStoreClient {
    private static final Double BYTES_TO_KB = 1024.0;
    private static final String DART_PATH = "dartpath";

    private final boolean enableTkeOidcProvider;
    private final String cosRegion;

    public CosDartClient(boolean enableTkeOidcProvider, String cosRegion) {
        this.enableTkeOidcProvider = enableTkeOidcProvider;
        this.cosRegion = cosRegion;
        // the credential provider provides short living token. If we have a libCosClient long living object with these
        // token or say if we refresh the client object before every time its usage, we're not gaining any benefit in doing that, i.e. having refresh method.
        // Additionally, the current usage of the client is to download any resource/artifacts one time when the job starts.
        // Create client when using its operation.
    }

    public String fetchJsonData(String udfName, GaugeStatsManager gaugeStatsManager, String bucketName, String dartName) {
        COSClient cosClient = CosLibClient.getInstance().get(enableTkeOidcProvider, cosRegion);
        COSObject cosObject = cosClient.getObject(bucketName, dartName);
        String dartJson;
        byte[] contentByteArray;
        try (COSObjectInputStream inputStream = cosObject.getObjectContent()) {
            contentByteArray = IOUtils.toByteArray(inputStream);
            dartJson = new String(contentByteArray);
        } catch (IOException e) {
            throw new TagDoesNotExistException("Could not find the content in cos for + dartName", e);
        }
        gaugeStatsManager.registerString(UDF_TELEMETRY_GROUP_KEY, udfName, DartAspects.DART_GCS_PATH.getValue(), dartName);
        gaugeStatsManager.registerDouble(DART_PATH, dartName, DartAspects.DART_GCS_FILE_SIZE.getValue(), contentByteArray.length / BYTES_TO_KB);
        return dartJson;
    }
}
