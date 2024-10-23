package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.functions.common.Constants;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.cos.CosDartClient;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.gcs.GcsDartClient;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.oss.OssDartClient;

import java.io.Serializable;

public class DartDataStoreClientProvider implements Serializable {
    private final String udfStoreProvider;
    private final String projectID;

    // Do not make this final, if so then the implementation of client should be Serializable
    private DartDataStoreClient dartDataStoreClient;

    public DartDataStoreClientProvider(String udfStoreProvider, String projectID) {
        this.udfStoreProvider = udfStoreProvider;
        this.projectID = projectID;
    }

    public DartDataStoreClient getDartDataStoreClient() {
        // In a distributed system, we don't intend the client to be serialized and most of the implementations like
        // GCP Storage implementation doesn't implement java.io.Serializable interface and you may see the below error
        // Caused by: org.apache.flink.api.common.InvalidProgramException: com.google.api.services.storage.Storage@1c666a8f
        // is not serializable. The object probably contains or references non serializable fields.
        // Caused by: java.io.NotSerializableException: com.google.api.services.storage.Storage
        if (dartDataStoreClient != null) {
            return dartDataStoreClient;
        }
        switch (udfStoreProvider) {
            case Constants.UDF_STORE_PROVIDER_GCS:
                dartDataStoreClient = new GcsDartClient(projectID);
                break;
            case Constants.UDF_STORE_PROVIDER_OSS:
                // TODO Check if OSS SDK supports projectID selection
                dartDataStoreClient = new OssDartClient();
                break;
            case Constants.UDF_STORE_PROVIDER_COS:
                // TODO Check if COS SDK supports projectID selection
                dartDataStoreClient = new CosDartClient();
                break;
            default:
                throw new IllegalArgumentException("Unknown UDF Store Provider: " + udfStoreProvider);
        }
        return dartDataStoreClient;
    }
}
