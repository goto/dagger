package com.gotocompany.dagger.core.sink.influx;

import org.influxdb.InfluxDB;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Manages InfluxDB connections keyed by database name.
 * Lazily creates connections on first access and caches them.
 * Connections are transient — after deserialization, they are re-created on demand.
 */
public class InfluxDBConnectionRegistry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, InfluxDBDatabaseConfig> configsByName;
    private final InfluxDBFactoryWrapper influxDBFactory;
    private transient Map<String, InfluxDB> connections;

    public InfluxDBConnectionRegistry(List<InfluxDBDatabaseConfig> configs, InfluxDBFactoryWrapper influxDBFactory) {
        this.influxDBFactory = influxDBFactory;
        this.configsByName = new HashMap<>();
        for (InfluxDBDatabaseConfig config : configs) {
            this.configsByName.put(config.getName(), config);
        }
    }

    /**
     * Get or create a connection for the given database name.
     * Batch mode is enabled using the database config's batch settings.
     *
     * @param databaseName the logical name of the database
     * @param exceptionHandler handler for batch write exceptions
     * @return the InfluxDB connection
     */
    public synchronized InfluxDB getConnection(String databaseName, BiConsumer<Iterable<org.influxdb.dto.Point>, Throwable> exceptionHandler) {
        ensureConnectionsMap();
        InfluxDB connection = connections.get(databaseName);
        if (connection != null) {
            return connection;
        }

        InfluxDBDatabaseConfig config = configsByName.get(databaseName);
        if (config == null) {
            throw new IllegalArgumentException("No InfluxDB database configured with name: " + databaseName
                    + ". Available: " + configsByName.keySet());
        }

        connection = influxDBFactory.connect(config.getUrl(), config.getUsername(), config.getPassword());
        connection.enableBatch(config.getBatchSize(), config.getFlushDurationMs(),
                TimeUnit.MILLISECONDS, Executors.defaultThreadFactory(), exceptionHandler);
        connections.put(databaseName, connection);
        return connection;
    }

    /**
     * Get the configuration for a named database.
     */
    public InfluxDBDatabaseConfig getConfig(String databaseName) {
        InfluxDBDatabaseConfig config = configsByName.get(databaseName);
        if (config == null) {
            throw new IllegalArgumentException("No InfluxDB database configured with name: " + databaseName
                    + ". Available: " + configsByName.keySet());
        }
        return config;
    }

    /**
     * Check if a database is configured.
     */
    public boolean hasDatabase(String databaseName) {
        return configsByName.containsKey(databaseName);
    }

    /**
     * Close all open connections.
     */
    public synchronized void closeAll() {
        if (connections != null) {
            for (InfluxDB connection : connections.values()) {
                try {
                    connection.close();
                } catch (Exception e) {
                    // best-effort close
                }
            }
            connections.clear();
        }
    }

    private void ensureConnectionsMap() {
        if (connections == null) {
            connections = new HashMap<>();
        }
    }
}
