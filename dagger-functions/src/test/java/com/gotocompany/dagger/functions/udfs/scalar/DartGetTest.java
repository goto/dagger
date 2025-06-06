package com.gotocompany.dagger.functions.udfs.scalar;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.functions.exceptions.KeyDoesNotExistException;
import com.gotocompany.dagger.functions.exceptions.TagDoesNotExistException;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.DefaultDartDataStore;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.MapCache;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.table.functions.FunctionContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class DartGetTest {
    private DefaultDartDataStore dataStore;

    @Mock
    private MetricGroup metricGroup;

    @Mock
    private FunctionContext functionContext;

    @Mock
    private MeterStatsManager meterStatsManager;

    @Mock
    private GaugeStatsManager gaugeStatsManager;

    // Subject
    private DartGet dartGet;

    @Before
    public void setUp() {
        initMocks(this);
        when(functionContext.getMetricGroup()).thenReturn(metricGroup);
        when(metricGroup.addGroup("udf", "DartGet")).thenReturn(metricGroup);
        when(metricGroup.addGroup("DartGet")).thenReturn(metricGroup);
        this.dataStore = mock(DefaultDartDataStore.class);

        dartGet = new DartGet(dataStore);

        dartGet.setMeterStatsManager(meterStatsManager);
        dartGet.setGaugeStatsManager(gaugeStatsManager);
    }

    @Test
    public void shouldReturnValueWhenMapAndKeyExist() {
        String key = "some-key";
        String value = "expected-value";
        when(dataStore.getMap("someMap", meterStatsManager, gaugeStatsManager)).thenReturn(new MapCache(singletonMap(key, value)));

        assertEquals(value, dartGet.eval("someMap", "some-key", 1));
    }

    @Test
    public void shouldReturnDifferentValueWhenMapAndKeyExistForAllOfThem() {
        String key = "some-key";
        String key2 = "other-key";
        String value = "expected-value";
        String value2 = "other-expected-value";
        when(dataStore.getMap("someMap", meterStatsManager, gaugeStatsManager)).thenReturn(new MapCache(singletonMap(key, value)));
        when(dataStore.getMap("otherMap", meterStatsManager, gaugeStatsManager)).thenReturn(new MapCache(singletonMap(key2, value2)));

        assertEquals(value, dartGet.eval("someMap", "some-key", 1));
        assertEquals(value2, dartGet.eval("otherMap", "other-key", 1));
    }

    @Test(expected = TagDoesNotExistException.class)
    public void shouldThrowErrorWhenMapDoesNotExist() {
        when(dataStore.getMap("nonExistingMap", meterStatsManager, gaugeStatsManager)).thenThrow(TagDoesNotExistException.class);

        dartGet.eval("nonExistingMap", "some-key", 1);
    }

    @Test(expected = KeyDoesNotExistException.class)
    public void shouldThrowErrorWhenKeyDoesNotExistAndDefaultValueNotGiven() {
        MapCache mapCache = mock(MapCache.class);
        when(dataStore.getMap("someMap", meterStatsManager, gaugeStatsManager)).thenReturn(mapCache);
        when(mapCache.get("nonExistingKey")).thenThrow(KeyDoesNotExistException.class);

        dartGet.eval("someMap", "nonExistingKey", 1);
    }

    @Test
    public void shouldReturnDefaultValueWhenKeyIsNotFoundAndDefaultValueGiven() {
        MapCache mapCache = mock(MapCache.class);
        when(dataStore.getMap("someMap", meterStatsManager, gaugeStatsManager)).thenReturn(mapCache);
        when(mapCache.get("nonExistingKey")).thenThrow(KeyDoesNotExistException.class);
        String defaultValue = "some value";

        assertEquals(defaultValue, dartGet.eval("someMap", "nonExistingKey", 1, defaultValue));
    }

    @Test
    public void shouldNotInvokeDataSourceWhenNotExceededRefreshRate() {
        MapCache mapCache = mock(MapCache.class);
        when(dataStore.getMap("someMap", meterStatsManager, gaugeStatsManager)).thenReturn(mapCache);
        when(mapCache.hasExpired(1)).thenReturn(false);

        dartGet.eval("someMap", "some-key", 1);
        dartGet.eval("someMap", "some-key", 1);

        verify(dataStore, times(1)).getMap("someMap", meterStatsManager, gaugeStatsManager);
    }


    @Test
    public void shouldInvokeDataSourceWhenExceededRefreshRate() {
        MapCache mapCache = mock(MapCache.class);
        when(dataStore.getMap("someMap", meterStatsManager, gaugeStatsManager)).thenReturn(mapCache);
        when(mapCache.hasExpired(-1)).thenReturn(true);

        dartGet.eval("someMap", "some-key", -1);

        verify(dataStore, times(1)).getMap("someMap", meterStatsManager, gaugeStatsManager);
    }

    @Test
    public void shouldRegisterGauge() throws Exception {
        dartGet.open(functionContext);
        verify(metricGroup, times(1)).gauge(any(String.class), any(Gauge.class));
    }
}
