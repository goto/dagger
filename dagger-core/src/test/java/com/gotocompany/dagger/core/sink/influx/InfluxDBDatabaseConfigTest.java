package com.gotocompany.dagger.core.sink.influx;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class InfluxDBDatabaseConfigTest {

    @Test
    public void shouldCreateConfigWithAllFields() {
        InfluxDBDatabaseConfig config = new InfluxDBDatabaseConfig(
                "mydb", "http://localhost:8086", "user", "pass",
                "metrics", "autogen", 100, 500);

        assertEquals("mydb", config.getName());
        assertEquals("http://localhost:8086", config.getUrl());
        assertEquals("user", config.getUsername());
        assertEquals("pass", config.getPassword());
        assertEquals("metrics", config.getDbName());
        assertEquals("autogen", config.getRetentionPolicy());
        assertEquals(100, config.getBatchSize());
        assertEquals(500, config.getFlushDurationMs());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenNameIsNull() {
        new InfluxDBDatabaseConfig(null, "http://localhost:8086", "user", "pass",
                "metrics", "autogen", 100, 500);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenUrlIsNull() {
        new InfluxDBDatabaseConfig("mydb", null, "user", "pass",
                "metrics", "autogen", 100, 500);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenDbNameIsNull() {
        new InfluxDBDatabaseConfig("mydb", "http://localhost:8086", "user", "pass",
                null, "autogen", 100, 500);
    }

    @Test
    public void shouldBeEqualForSameValues() {
        InfluxDBDatabaseConfig config1 = new InfluxDBDatabaseConfig(
                "mydb", "http://localhost:8086", "user", "pass",
                "metrics", "autogen", 100, 500);
        InfluxDBDatabaseConfig config2 = new InfluxDBDatabaseConfig(
                "mydb", "http://localhost:8086", "user", "pass",
                "metrics", "autogen", 100, 500);

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    public void shouldNotBeEqualForDifferentNames() {
        InfluxDBDatabaseConfig config1 = new InfluxDBDatabaseConfig(
                "mydb1", "http://localhost:8086", "user", "pass",
                "metrics", "autogen", 100, 500);
        InfluxDBDatabaseConfig config2 = new InfluxDBDatabaseConfig(
                "mydb2", "http://localhost:8086", "user", "pass",
                "metrics", "autogen", 100, 500);

        assertNotEquals(config1, config2);
    }

    @Test
    public void shouldNotBeEqualForDifferentUrls() {
        InfluxDBDatabaseConfig config1 = new InfluxDBDatabaseConfig(
                "mydb", "http://host1:8086", "user", "pass",
                "metrics", "autogen", 100, 500);
        InfluxDBDatabaseConfig config2 = new InfluxDBDatabaseConfig(
                "mydb", "http://host2:8086", "user", "pass",
                "metrics", "autogen", 100, 500);

        assertNotEquals(config1, config2);
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        InfluxDBDatabaseConfig config = new InfluxDBDatabaseConfig(
                "mydb", "http://localhost:8086", "user", "pass",
                "metrics", "autogen", 100, 500);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(config);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        InfluxDBDatabaseConfig deserialized = (InfluxDBDatabaseConfig) ois.readObject();
        ois.close();

        assertEquals(config, deserialized);
    }

    @Test
    public void shouldHaveDefaultNameConstant() {
        assertEquals("default", InfluxDBDatabaseConfig.DEFAULT_NAME);
    }

    @Test
    public void shouldNotIncludeCredentialsInToString() {
        InfluxDBDatabaseConfig config = new InfluxDBDatabaseConfig(
                "mydb", "http://localhost:8086", "secretuser", "secretpass",
                "metrics", "autogen", 100, 500);

        String str = config.toString();
        assertTrue(str.contains("mydb"));
        assertTrue(str.contains("http://localhost:8086"));
        assertFalse(str.contains("secretuser"));
        assertFalse(str.contains("secretpass"));
    }
}
