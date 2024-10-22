package com.gotocompany.dagger.functions.udfs.scalar.dart.store.gcs;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.functions.exceptions.BucketDoesNotExistException;
import com.gotocompany.dagger.functions.exceptions.TagDoesNotExistException;
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

public class GcsDartDataStoreTest {
    private final String defaultListName = "listName";

    private final String defaultMapName = "mapName";
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private GcsDartDataStore gcsDataStore;
    private List<String> listContent;
    private Map<String, String> mapContent;
    private GcsClient gcsClient;
    private MeterStatsManager meterStatsManager;
    private GaugeStatsManager gaugeStatsManager;

    @Before
    public void setUp() {
        gcsDataStore = mock(GcsDartDataStore.class);
        gcsClient = mock(GcsClient.class);
        meterStatsManager = mock(MeterStatsManager.class);
        gaugeStatsManager = mock(GaugeStatsManager.class);
        when(gcsDataStore.getSet(anyString(), any(), any())).thenCallRealMethod();
        when(gcsDataStore.getMap(anyString(), any(), any())).thenCallRealMethod();
        when(gcsDataStore.getGcsClient()).thenReturn(gcsClient);
        listContent = Arrays.asList("listContent");
        mapContent = Collections.singletonMap("key", "value");
    }

    @Test
    public void shouldGetExistingListGivenName() {
        String jsonData = " { \"data\" : [ \"listContent\" ] } ";

        when(gcsClient.fetchJsonData(any(), any(), any(), anyString())).thenReturn(jsonData);
        SetCache setCache = new SetCache(new HashSet<>(listContent));
        Assert.assertEquals(setCache, gcsDataStore.getSet(defaultListName, meterStatsManager, gaugeStatsManager));
    }

    @Test
    public void shouldThrowTagDoesNotExistWhenListIsNotThere() {
        thrown.expect(TagDoesNotExistException.class);
        thrown.expectMessage("Could not find the content in gcs for invalidListName");

        when(gcsClient.fetchJsonData(any(), any(), any(), anyString())).thenThrow(new TagDoesNotExistException("Could not find the content in gcs for invalidListName"));

        gcsDataStore.getSet("invalidListName", meterStatsManager, gaugeStatsManager);
    }

    @Test
    public void shouldThrowBucketDoesNotExistWhenBucketIsNotThere() {
        thrown.expect(BucketDoesNotExistException.class);
        thrown.expectMessage("Could not find the bucket in gcs for invalidListName");

        when(gcsClient.fetchJsonData(any(), any(), any(), anyString())).thenThrow(new BucketDoesNotExistException("Could not find the bucket in gcs for invalidListName"));

        gcsDataStore.getSet("invalidListName", meterStatsManager, gaugeStatsManager);
    }

    @Test
    public void shouldGetExistingMapGivenName() {
        String jsonData = " { \"key\" :  \"value\"  } ";
        when(gcsClient.fetchJsonData(any(), any(), any(), anyString())).thenReturn(jsonData);
        MapCache mapCache = new MapCache(new HashMap<>(mapContent));

        Assert.assertEquals(mapCache, gcsDataStore.getMap(defaultMapName, meterStatsManager, gaugeStatsManager));
    }

    @Test
    public void shouldThrowTagDoesNotExistWhenMapIsNotThere() {
        thrown.expect(TagDoesNotExistException.class);
        thrown.expectMessage("Could not find the content in gcs for invalidMapName");

        when(gcsClient.fetchJsonData(any(), any(), any(), anyString())).thenThrow(new TagDoesNotExistException("Could not find the content in gcs for invalidMapName"));

        gcsDataStore.getSet("invalidMapName", meterStatsManager, gaugeStatsManager);
    }
}
