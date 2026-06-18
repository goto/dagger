package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.functions.common.Constants;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.cos.CosDartClient;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.gcs.GcsDartClient;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.oss.OssDartClient;

import java.io.Serializable;

/**
 * Serializable factory that lazily creates the {@link DartDataStoreClient} for the configured object
 * storage provider.
 *
 * <p>Provider implementations (for example the GCP {@code Storage} client) are generally not
 * {@link Serializable}, which would break Flink's operator distribution if a client instance were
 * held in a serialized field. To avoid that, this provider keeps only the lightweight configuration
 * needed to build a client and constructs the concrete client on first use, on the task manager
 * where it is actually needed.
 */
public class DartDataStoreClientProvider implements Serializable {
    /** Identifier of the backend to use, one of the {@code UDF_STORE_PROVIDER_*} constants. */
    private final String udfStoreProvider;
    /** Cloud project id passed to backends (such as GCS) that require it. */
    private final String projectID;
    /** Dagger configuration consulted for backend-specific settings (endpoint, region, OIDC). */
    private final Configuration configuration;

    // Do not make this final, if so then the implementation of client should be Serializable
    /** Lazily instantiated, cached client; intentionally non-final so it is not serialized. */
    private DartDataStoreClient dartDataStoreClient;

    /**
     * Creates a provider capturing the settings needed to build a store client later.
     *
     * @param udfStoreProvider the backend identifier (one of the {@code UDF_STORE_PROVIDER_*} values)
     * @param projectID        the cloud project id forwarded to backends that require it (e.g. GCS)
     * @param configuration    the Dagger configuration supplying backend-specific settings
     */
    public DartDataStoreClientProvider(String udfStoreProvider, String projectID, Configuration configuration) {
        this.udfStoreProvider = udfStoreProvider;
        this.projectID = projectID;
        this.configuration = configuration;
    }

    /**
     * Returns the store client for the configured provider, creating and caching it on first call.
     *
     * <p>The client is built from configuration the first time it is requested and reused on
     * subsequent calls. {@code GCS} uses the project id, {@code OSS} reads its endpoint, and
     * {@code COS} reads its region and OIDC-provider flag from the {@link Configuration}.
     *
     * @return the lazily created {@link DartDataStoreClient} matching the configured provider
     * @throws IllegalArgumentException if the configured provider is not recognized
     */
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
                dartDataStoreClient = new OssDartClient(
                        configuration.getString(Constants.OSS_ENDPOINT, Constants.DEFAULT_OSS_ENDPOINT)
                );
                break;
            case Constants.UDF_STORE_PROVIDER_COS:
                dartDataStoreClient = new CosDartClient(
                        configuration.getBoolean(Constants.ENABLE_TKE_OIDC_PROVIDER, false),
                        configuration.getString(Constants.COS_REGION, Constants.DEFAULT_COS_REGION)
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown UDF Store Provider: " + udfStoreProvider);
        }
        return dartDataStoreClient;
    }
}
