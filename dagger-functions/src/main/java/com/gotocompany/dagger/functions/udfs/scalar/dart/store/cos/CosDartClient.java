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

/**
 * {@link DartDataStoreClient} backed by Tencent Cloud Object Storage (COS).
 *
 * <p>It downloads dart JSON files from a COS bucket using a {@link COSClient} obtained from the
 * shared {@link CosLibClient} singleton. Because COS credentials are short-lived tokens, no
 * long-lived client is cached here: a client is created per operation, which is acceptable given that
 * darts are typically fetched once at job startup. Each successful download also records the dart
 * path and file size through the supplied gauge stats manager.
 */
public class CosDartClient implements DartDataStoreClient {
    /** Divisor used to convert a byte count into kilobytes for the file-size gauge. */
    private static final Double BYTES_TO_KB = 1024.0;
    /** Metric group key under which the per-dart file-size gauge is registered. */
    private static final String DART_PATH = "dartpath";

    /** Whether to authenticate via the TKE OIDC credential provider. */
    private final boolean enableTkeOidcProvider;
    /** COS region the bucket resides in. */
    private final String cosRegion;

    /**
     * Creates a COS dart client capturing the credentials/region settings used to build a
     * {@link COSClient} on demand.
     *
     * @param enableTkeOidcProvider whether to authenticate using the TKE OIDC credential provider
     * @param cosRegion             the COS region of the bucket to read from
     */
    public CosDartClient(boolean enableTkeOidcProvider, String cosRegion) {
        this.enableTkeOidcProvider = enableTkeOidcProvider;
        this.cosRegion = cosRegion;
        // the credential provider provides short living token. If we have a libCosClient long living object with these
        // token or say if we refresh the client object before every time its usage, we're not gaining any benefit in doing that, i.e. having refresh method.
        // Additionally, the current usage of the client is to download any resource/artifacts one time when the job starts.
        // Create client when using its operation.
    }

    /**
     * Downloads the dart object from COS and returns its content as a JSON string.
     *
     * <p>Obtains a {@link COSClient} for the configured region/credentials, reads the object's bytes
     * fully into a string, and records the dart path and file size (in KB) on the gauge stats
     * manager.
     *
     * @param udfName           the name of the calling UDF, used as the gauge metric group label
     * @param gaugeStatsManager gauge manager used to record the dart path and file size
     * @param bucketName        the COS bucket to read from
     * @param dartName          the object key of the dart file within the bucket
     * @return the raw JSON content of the dart object
     * @throws TagDoesNotExistException if the object content cannot be read from COS
     */
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
