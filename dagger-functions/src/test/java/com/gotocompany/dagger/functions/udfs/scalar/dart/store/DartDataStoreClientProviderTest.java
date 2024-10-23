package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.functions.common.Constants;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.gcs.GcsDartClient;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.oss.OssDartClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DartDataStoreClientProviderTest {
    private DartDataStoreClientProvider dartDataStoreClientProvider;

    @Before
    public void setUp() {
        dartDataStoreClientProvider = null;
    }

    @Test
    public void shouldReturnGcsDartClientWhenUdfStoreProviderIsGcs() {
        String udfStoreProvider = Constants.UDF_STORE_PROVIDER_GCS;
        String projectID = "test-project";

        dartDataStoreClientProvider = new DartDataStoreClientProvider(udfStoreProvider, projectID);

        DartDataStoreClient client = dartDataStoreClientProvider.getDartDataStoreClient();

        assertTrue(client instanceof GcsDartClient);
    }

    @Test
    public void shouldReturnOssDartClientWhenUdfStoreProviderIsOss() {
        String udfStoreProvider = Constants.UDF_STORE_PROVIDER_OSS;

        dartDataStoreClientProvider = new DartDataStoreClientProvider(udfStoreProvider, null);
        DartDataStoreClient client = dartDataStoreClientProvider.getDartDataStoreClient();

        assertTrue(client instanceof OssDartClient);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForUnknownUdfStoreProvider() {
        String udfStoreProvider = "UNKNOWN-PROVIDER";

        dartDataStoreClientProvider = new DartDataStoreClientProvider(udfStoreProvider, null);

        try {
            dartDataStoreClientProvider.getDartDataStoreClient();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Unknown UDF Store Provider: UNKNOWN-PROVIDER", e.getMessage());
            throw e;
        }
    }

    @Test
    public void shouldReturnSameClientOnSubsequentCalls() {
        String udfStoreProvider = Constants.UDF_STORE_PROVIDER_GCS;
        String projectID = "test-project";

        dartDataStoreClientProvider = new DartDataStoreClientProvider(udfStoreProvider, projectID);

        DartDataStoreClient firstClient = dartDataStoreClientProvider.getDartDataStoreClient();
        DartDataStoreClient secondClient = dartDataStoreClientProvider.getDartDataStoreClient();

        Assert.assertEquals(firstClient, secondClient);
    }
}
