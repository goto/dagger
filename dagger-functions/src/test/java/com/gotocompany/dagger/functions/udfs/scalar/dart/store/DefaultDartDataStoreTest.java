package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.functions.exceptions.BucketDoesNotExistException;
import com.gotocompany.dagger.functions.exceptions.TagDoesNotExistException;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.gcs.GcsDartClient;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.MapCache;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.SetCache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultDartDataStoreTest {
    private final String defaultListName = "listName";

    private final String defaultMapName = "mapName";
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private DefaultDartDataStore defaultDartDataStore;
    private List<String> listContent;
    private Map<String, String> mapContent;
    private GcsDartClient gcsDartClient;
    private MeterStatsManager meterStatsManager;
    private GaugeStatsManager gaugeStatsManager;

    @Before
    public void setUp() {
        // Subject
        DartDataStoreClientProvider dartDataStoreClientProvider = mock(DartDataStoreClientProvider.class);
        defaultDartDataStore = new DefaultDartDataStore(dartDataStoreClientProvider, "test-bucket");

        gcsDartClient = mock(GcsDartClient.class);
        meterStatsManager = mock(MeterStatsManager.class);
        gaugeStatsManager = mock(GaugeStatsManager.class);
        when(dartDataStoreClientProvider.getDartDataStoreClient()).thenReturn(gcsDartClient);
        listContent = Arrays.asList("listContent");
        mapContent = Collections.singletonMap("key", "value");
    }

    @Test
    public void shouldGetExistingListGivenName() {
        String jsonData = " { \"data\" : [ \"listContent\" ] } ";

        when(gcsDartClient.fetchJsonData(any(), any(), any(), anyString())).thenReturn(jsonData);
        SetCache setCache = new SetCache(new HashSet<>(listContent));
        Assert.assertEquals(setCache, defaultDartDataStore.getSet(defaultListName, meterStatsManager, gaugeStatsManager));
    }

    @Test
    public void shouldThrowTagDoesNotExistWhenListIsNotThere() {
        thrown.expect(TagDoesNotExistException.class);
        thrown.expectMessage("Could not find the content in gcs for invalidListName");

        when(gcsDartClient.fetchJsonData(any(), any(), any(), anyString())).thenThrow(new TagDoesNotExistException("Could not find the content in gcs for invalidListName"));

        defaultDartDataStore.getSet("invalidListName", meterStatsManager, gaugeStatsManager);
    }

    @Test
    public void shouldThrowBucketDoesNotExistWhenBucketIsNotThere() {
        thrown.expect(BucketDoesNotExistException.class);
        thrown.expectMessage("Could not find the bucket in gcs for invalidListName");

        when(gcsDartClient.fetchJsonData(any(), any(), any(), anyString())).thenThrow(new BucketDoesNotExistException("Could not find the bucket in gcs for invalidListName"));

        defaultDartDataStore.getSet("invalidListName", meterStatsManager, gaugeStatsManager);
    }

    @Test
    public void shouldGetExistingMapGivenName() {
        String jsonData = " { \"key\" :  \"value\"  } ";
        when(gcsDartClient.fetchJsonData(any(), any(), any(), anyString())).thenReturn(jsonData);
        MapCache mapCache = new MapCache(new HashMap<>(mapContent));

        Assert.assertEquals(mapCache, defaultDartDataStore.getMap(defaultMapName, meterStatsManager, gaugeStatsManager));
    }

    @Test
    public void shouldThrowTagDoesNotExistWhenMapIsNotThere() {
        thrown.expect(TagDoesNotExistException.class);
        thrown.expectMessage("Could not find the content in gcs for invalidMapName");

        when(gcsDartClient.fetchJsonData(any(), any(), any(), anyString())).thenThrow(new TagDoesNotExistException("Could not find the content in gcs for invalidMapName"));

        defaultDartDataStore.getSet("invalidMapName", meterStatsManager, gaugeStatsManager);
    }
}
