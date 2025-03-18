package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.functions.common.Constants;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.gcs.GcsDartClient;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.oss.OssDartClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import static org.junit.Assert.assertTrue;

public class DartDataStoreClientProviderTest {
    private DartDataStoreClientProvider dartDataStoreClientProvider;
    @Mock
    private Configuration configuration;

    @Before
    public void setUp() {
        initMocks(this);
        dartDataStoreClientProvider = null;
        when(configuration.getString(Constants.OSS_ENDPOINT, Constants.DEFAULT_OSS_ENDPOINT)).thenReturn("oss-ap-southeast-5.aliyuncs.com");
    }

    @Test
    public void shouldReturnGcsDartClientWhenUdfStoreProviderIsGcs() {
        String udfStoreProvider = Constants.UDF_STORE_PROVIDER_GCS;
        String projectID = "test-project";

        dartDataStoreClientProvider = new DartDataStoreClientProvider(udfStoreProvider, projectID, configuration);

        DartDataStoreClient client = dartDataStoreClientProvider.getDartDataStoreClient();

        assertTrue(client instanceof GcsDartClient);
    }

    @Test
    public void shouldReturnOssDartClientWhenUdfStoreProviderIsOss() {
        String udfStoreProvider = Constants.UDF_STORE_PROVIDER_OSS;

        dartDataStoreClientProvider = new DartDataStoreClientProvider(udfStoreProvider, null, configuration);
        DartDataStoreClient client = dartDataStoreClientProvider.getDartDataStoreClient();

        assertTrue(client instanceof OssDartClient);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForUnknownUdfStoreProvider() {
        String udfStoreProvider = "UNKNOWN-PROVIDER";

        dartDataStoreClientProvider = new DartDataStoreClientProvider(udfStoreProvider, null, configuration);

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

        dartDataStoreClientProvider = new DartDataStoreClientProvider(udfStoreProvider, projectID, configuration);

        DartDataStoreClient firstClient = dartDataStoreClientProvider.getDartDataStoreClient();
        DartDataStoreClient secondClient = dartDataStoreClientProvider.getDartDataStoreClient();

        Assert.assertEquals(firstClient, secondClient);
    }
}
