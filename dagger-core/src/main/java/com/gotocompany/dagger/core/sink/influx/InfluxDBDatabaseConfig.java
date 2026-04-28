package com.gotocompany.dagger.core.sink.influx;

import java.io.Serializable;
import java.util.Objects;

/**
 * Configuration for a single InfluxDB database target.
 * Each instance represents a named database that sinks can write to.
 */
public class InfluxDBDatabaseConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_NAME = "default";

    private final String name;
    private final String url;
    private final String username;
    private final String password;
    private final String dbName;
    private final String retentionPolicy;
    private final int batchSize;
    private final int flushDurationMs;

    public InfluxDBDatabaseConfig(String name, String url, String username, String password,
                                  String dbName, String retentionPolicy, int batchSize, int flushDurationMs) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.username = Objects.requireNonNull(username, "username must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.dbName = Objects.requireNonNull(dbName, "dbName must not be null");
        this.retentionPolicy = Objects.requireNonNull(retentionPolicy, "retentionPolicy must not be null");
        this.batchSize = batchSize;
        this.flushDurationMs = flushDurationMs;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDbName() {
        return dbName;
    }

    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getFlushDurationMs() {
        return flushDurationMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfluxDBDatabaseConfig that = (InfluxDBDatabaseConfig) o;
        return batchSize == that.batchSize
                && flushDurationMs == that.flushDurationMs
                && Objects.equals(name, that.name)
                && Objects.equals(url, that.url)
                && Objects.equals(username, that.username)
                && Objects.equals(password, that.password)
                && Objects.equals(dbName, that.dbName)
                && Objects.equals(retentionPolicy, that.retentionPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, username, password, dbName, retentionPolicy, batchSize, flushDurationMs);
    }

    @Override
    public String toString() {
        return "InfluxDBDatabaseConfig{"
                + "name='" + name + '\''
                + ", url='" + url + '\''
                + ", dbName='" + dbName + '\''
                + ", retentionPolicy='" + retentionPolicy + '\''
                + ", batchSize=" + batchSize
                + ", flushDurationMs=" + flushDurationMs
                + '}';
    }
}
