package com.gotocompany.dagger.core.sink.influx;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class InfluxDBConfigurationParserTest {

    @Mock
    private Configuration configuration;

    @Before
    public void setUp() {
        initMocks(this);
    }

    // --- Legacy fallback tests ---

    @Test
    public void shouldFallBackToLegacyKeysWhenDatabasesConfigIsAbsent() {
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_URL_KEY, Constants.SINK_INFLUX_URL_DEFAULT)).thenReturn("http://localhost:8086");
        when(configuration.getString(Constants.SINK_INFLUX_USERNAME_KEY, Constants.SINK_INFLUX_USERNAME_DEFAULT)).thenReturn("admin");
        when(configuration.getString(Constants.SINK_INFLUX_PASSWORD_KEY, Constants.SINK_INFLUX_PASSWORD_DEFAULT)).thenReturn("secret");
        when(configuration.getString(Constants.SINK_INFLUX_DB_NAME_KEY, Constants.SINK_INFLUX_DB_NAME_DEFAULT)).thenReturn("mydb");
        when(configuration.getString(Constants.SINK_INFLUX_RETENTION_POLICY_KEY, Constants.SINK_INFLUX_RETENTION_POLICY_DEFAULT)).thenReturn("autogen");
        when(configuration.getInteger(Constants.SINK_INFLUX_BATCH_SIZE_KEY, Constants.SINK_INFLUX_BATCH_SIZE_DEFAULT)).thenReturn(100);
        when(configuration.getInteger(Constants.SINK_INFLUX_FLUSH_DURATION_MS_KEY, Constants.SINK_INFLUX_FLUSH_DURATION_MS_DEFAULT)).thenReturn(500);

        InfluxDBConfigurationParser parser = InfluxDBConfigurationParser.parse(configuration);

        assertEquals(1, parser.size());
        assertTrue(parser.hasDatabase("default"));

        InfluxDBDatabaseConfig config = parser.getDefaultDatabase();
        assertEquals("default", config.getName());
        assertEquals("http://localhost:8086", config.getUrl());
        assertEquals("admin", config.getUsername());
        assertEquals("secret", config.getPassword());
        assertEquals("mydb", config.getDbName());
        assertEquals("autogen", config.getRetentionPolicy());
        assertEquals(100, config.getBatchSize());
        assertEquals(500, config.getFlushDurationMs());
    }

    @Test
    public void shouldFallBackToLegacyKeysWithDefaults() {
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_URL_KEY, Constants.SINK_INFLUX_URL_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_USERNAME_KEY, Constants.SINK_INFLUX_USERNAME_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_PASSWORD_KEY, Constants.SINK_INFLUX_PASSWORD_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_DB_NAME_KEY, Constants.SINK_INFLUX_DB_NAME_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_RETENTION_POLICY_KEY, Constants.SINK_INFLUX_RETENTION_POLICY_DEFAULT)).thenReturn("");
        when(configuration.getInteger(Constants.SINK_INFLUX_BATCH_SIZE_KEY, Constants.SINK_INFLUX_BATCH_SIZE_DEFAULT)).thenReturn(0);
        when(configuration.getInteger(Constants.SINK_INFLUX_FLUSH_DURATION_MS_KEY, Constants.SINK_INFLUX_FLUSH_DURATION_MS_DEFAULT)).thenReturn(0);

        InfluxDBConfigurationParser parser = InfluxDBConfigurationParser.parse(configuration);

        assertEquals(1, parser.size());
        InfluxDBDatabaseConfig config = parser.getDefaultDatabase();
        assertEquals("", config.getUrl());
        assertEquals("", config.getDbName());
        assertEquals(0, config.getBatchSize());
    }

    // --- JSON config tests ---

    @Test
    public void shouldParseSingleDatabaseFromJson() {
        String json = "[{\"name\":\"db1\",\"url\":\"http://host1:8086\",\"username\":\"u1\",\"password\":\"p1\","
                + "\"dbName\":\"metrics1\",\"retentionPolicy\":\"autogen\",\"batchSize\":200,\"flushDurationMs\":1000}]";
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn(json);

        InfluxDBConfigurationParser parser = InfluxDBConfigurationParser.parse(configuration);

        assertEquals(1, parser.size());
        assertTrue(parser.hasDatabase("db1"));

        InfluxDBDatabaseConfig config = parser.getDatabase("db1");
        assertEquals("db1", config.getName());
        assertEquals("http://host1:8086", config.getUrl());
        assertEquals("u1", config.getUsername());
        assertEquals("p1", config.getPassword());
        assertEquals("metrics1", config.getDbName());
        assertEquals("autogen", config.getRetentionPolicy());
        assertEquals(200, config.getBatchSize());
        assertEquals(1000, config.getFlushDurationMs());
    }

    @Test
    public void shouldParseMultipleDatabasesFromJson() {
        String json = "["
                + "{\"name\":\"ht-db\",\"url\":\"http://host1:8086\",\"username\":\"u1\",\"password\":\"p1\","
                + "\"dbName\":\"metrics_ht\",\"retentionPolicy\":\"rp1\",\"batchSize\":1000,\"flushDurationMs\":500},"
                + "{\"name\":\"lt-db\",\"url\":\"http://host2:8086\",\"username\":\"u2\",\"password\":\"p2\","
                + "\"dbName\":\"metrics_lt\",\"retentionPolicy\":\"rp2\",\"batchSize\":100,\"flushDurationMs\":2000}"
                + "]";
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn(json);

        InfluxDBConfigurationParser parser = InfluxDBConfigurationParser.parse(configuration);

        assertEquals(2, parser.size());
        assertTrue(parser.hasDatabase("ht-db"));
        assertTrue(parser.hasDatabase("lt-db"));

        assertEquals("http://host1:8086", parser.getDatabase("ht-db").getUrl());
        assertEquals("metrics_ht", parser.getDatabase("ht-db").getDbName());
        assertEquals("http://host2:8086", parser.getDatabase("lt-db").getUrl());
        assertEquals("metrics_lt", parser.getDatabase("lt-db").getDbName());
    }

    @Test
    public void shouldUseDefaultsForOptionalJsonFields() {
        String json = "[{\"name\":\"db1\",\"url\":\"http://host1:8086\",\"dbName\":\"mydb\"}]";
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn(json);

        InfluxDBConfigurationParser parser = InfluxDBConfigurationParser.parse(configuration);
        InfluxDBDatabaseConfig config = parser.getDatabase("db1");

        assertEquals("", config.getUsername());
        assertEquals("", config.getPassword());
        assertEquals("", config.getRetentionPolicy());
        assertEquals(0, config.getBatchSize());
        assertEquals(0, config.getFlushDurationMs());
    }

    @Test
    public void shouldReturnAllDatabases() {
        String json = "["
                + "{\"name\":\"db1\",\"url\":\"http://h1:8086\",\"dbName\":\"d1\"},"
                + "{\"name\":\"db2\",\"url\":\"http://h2:8086\",\"dbName\":\"d2\"}"
                + "]";
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn(json);

        InfluxDBConfigurationParser parser = InfluxDBConfigurationParser.parse(configuration);

        assertEquals(2, parser.getAllDatabases().size());
    }

    // --- Error cases ---

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnDuplicateDatabaseNames() {
        String json = "["
                + "{\"name\":\"db1\",\"url\":\"http://h1:8086\",\"dbName\":\"d1\"},"
                + "{\"name\":\"db1\",\"url\":\"http://h2:8086\",\"dbName\":\"d2\"}"
                + "]";
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn(json);

        InfluxDBConfigurationParser.parse(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnMissingRequiredNameField() {
        String json = "[{\"url\":\"http://h1:8086\",\"dbName\":\"d1\"}]";
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn(json);

        InfluxDBConfigurationParser.parse(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnMissingRequiredUrlField() {
        String json = "[{\"name\":\"db1\",\"dbName\":\"d1\"}]";
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn(json);

        InfluxDBConfigurationParser.parse(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnMissingRequiredDbNameField() {
        String json = "[{\"name\":\"db1\",\"url\":\"http://h1:8086\"}]";
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn(json);

        InfluxDBConfigurationParser.parse(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnInvalidJson() {
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn("not-json");

        InfluxDBConfigurationParser.parse(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnEmptyJsonArray() {
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn("[]");

        InfluxDBConfigurationParser.parse(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGettingNonExistentDatabase() {
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_URL_KEY, Constants.SINK_INFLUX_URL_DEFAULT)).thenReturn("http://localhost:8086");
        when(configuration.getString(Constants.SINK_INFLUX_USERNAME_KEY, Constants.SINK_INFLUX_USERNAME_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_PASSWORD_KEY, Constants.SINK_INFLUX_PASSWORD_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_DB_NAME_KEY, Constants.SINK_INFLUX_DB_NAME_DEFAULT)).thenReturn("mydb");
        when(configuration.getString(Constants.SINK_INFLUX_RETENTION_POLICY_KEY, Constants.SINK_INFLUX_RETENTION_POLICY_DEFAULT)).thenReturn("");
        when(configuration.getInteger(Constants.SINK_INFLUX_BATCH_SIZE_KEY, Constants.SINK_INFLUX_BATCH_SIZE_DEFAULT)).thenReturn(0);
        when(configuration.getInteger(Constants.SINK_INFLUX_FLUSH_DURATION_MS_KEY, Constants.SINK_INFLUX_FLUSH_DURATION_MS_DEFAULT)).thenReturn(0);

        InfluxDBConfigurationParser parser = InfluxDBConfigurationParser.parse(configuration);
        parser.getDatabase("nonexistent");
    }

    @Test
    public void shouldReportHasDatabaseFalseForNonExistent() {
        when(configuration.getString(Constants.SINK_INFLUX_DATABASES_CONFIG_KEY, Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_URL_KEY, Constants.SINK_INFLUX_URL_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_USERNAME_KEY, Constants.SINK_INFLUX_USERNAME_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_PASSWORD_KEY, Constants.SINK_INFLUX_PASSWORD_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_DB_NAME_KEY, Constants.SINK_INFLUX_DB_NAME_DEFAULT)).thenReturn("");
        when(configuration.getString(Constants.SINK_INFLUX_RETENTION_POLICY_KEY, Constants.SINK_INFLUX_RETENTION_POLICY_DEFAULT)).thenReturn("");
        when(configuration.getInteger(Constants.SINK_INFLUX_BATCH_SIZE_KEY, Constants.SINK_INFLUX_BATCH_SIZE_DEFAULT)).thenReturn(0);
        when(configuration.getInteger(Constants.SINK_INFLUX_FLUSH_DURATION_MS_KEY, Constants.SINK_INFLUX_FLUSH_DURATION_MS_DEFAULT)).thenReturn(0);

        InfluxDBConfigurationParser parser = InfluxDBConfigurationParser.parse(configuration);
        assertFalse(parser.hasDatabase("nonexistent"));
    }
}
