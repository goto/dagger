package com.gotocompany.dagger.core.sink.influx;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class InfluxDBConnectionRegistryTest {

    @Mock
    private InfluxDBFactoryWrapper influxDBFactory;

    @Mock
    private InfluxDB influxDB1;

    @Mock
    private InfluxDB influxDB2;

    private BiConsumer<Iterable<Point>, Throwable> exceptionHandler;

    private InfluxDBDatabaseConfig config1;
    private InfluxDBDatabaseConfig config2;

    @Before
    public void setUp() {
        initMocks(this);
        exceptionHandler = (points, throwable) -> { };

        config1 = new InfluxDBDatabaseConfig("db1", "http://host1:8086", "u1", "p1",
                "metrics1", "autogen", 100, 500);
        config2 = new InfluxDBDatabaseConfig("db2", "http://host2:8086", "u2", "p2",
                "metrics2", "rp2", 200, 1000);

        when(influxDBFactory.connect("http://host1:8086", "u1", "p1")).thenReturn(influxDB1);
        when(influxDBFactory.connect("http://host2:8086", "u2", "p2")).thenReturn(influxDB2);
    }

    @Test
    public void shouldCreateConnectionOnFirstAccess() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Collections.singletonList(config1), influxDBFactory);

        InfluxDB conn = registry.getConnection("db1", exceptionHandler);

        assertSame(influxDB1, conn);
        verify(influxDBFactory).connect("http://host1:8086", "u1", "p1");
    }

    @Test
    public void shouldEnableBatchModeOnConnection() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Collections.singletonList(config1), influxDBFactory);

        registry.getConnection("db1", exceptionHandler);

        verify(influxDB1).enableBatch(eq(100), eq(500), eq(TimeUnit.MILLISECONDS),
                any(ThreadFactory.class), eq(exceptionHandler));
    }

    @Test
    public void shouldReuseConnectionOnSubsequentAccess() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Collections.singletonList(config1), influxDBFactory);

        InfluxDB first = registry.getConnection("db1", exceptionHandler);
        InfluxDB second = registry.getConnection("db1", exceptionHandler);

        assertSame(first, second);
        verify(influxDBFactory, times(1)).connect(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldCreateSeparateConnectionsForDifferentDatabases() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Arrays.asList(config1, config2), influxDBFactory);

        InfluxDB conn1 = registry.getConnection("db1", exceptionHandler);
        InfluxDB conn2 = registry.getConnection("db2", exceptionHandler);

        assertSame(influxDB1, conn1);
        assertSame(influxDB2, conn2);
        assertNotSame(conn1, conn2);
        verify(influxDBFactory, times(2)).connect(anyString(), anyString(), anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGettingConnectionForUnknownDatabase() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Collections.singletonList(config1), influxDBFactory);

        registry.getConnection("nonexistent", exceptionHandler);
    }

    @Test
    public void shouldCloseAllConnections() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Arrays.asList(config1, config2), influxDBFactory);

        registry.getConnection("db1", exceptionHandler);
        registry.getConnection("db2", exceptionHandler);

        registry.closeAll();

        verify(influxDB1).close();
        verify(influxDB2).close();
    }

    @Test
    public void shouldHandleCloseAllWhenNoConnectionsOpen() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Collections.singletonList(config1), influxDBFactory);

        // Should not throw
        registry.closeAll();
    }

    @Test
    public void shouldReturnConfigForDatabase() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Arrays.asList(config1, config2), influxDBFactory);

        assertEquals(config1, registry.getConfig("db1"));
        assertEquals(config2, registry.getConfig("db2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGettingConfigForUnknownDatabase() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Collections.singletonList(config1), influxDBFactory);

        registry.getConfig("nonexistent");
    }

    @Test
    public void shouldReportHasDatabase() {
        InfluxDBConnectionRegistry registry = new InfluxDBConnectionRegistry(
                Collections.singletonList(config1), influxDBFactory);

        assertTrue(registry.hasDatabase("db1"));
        assertFalse(registry.hasDatabase("db2"));
    }
}
