package com.gotocompany.dagger.core.sink.influx;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses InfluxDB database configurations from either:
 * 1. The new JSON array config key ({@code SINK_INFLUX_DATABASES_CONFIG}), or
 * 2. The legacy flat keys ({@code SINK_INFLUX_URL}, {@code SINK_INFLUX_DB_NAME}, etc.)
 *    as a single "default" entry — for backward compatibility.
 */
public class InfluxDBConfigurationParser implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();

    private final Map<String, InfluxDBDatabaseConfig> configsByName;

    private InfluxDBConfigurationParser(Map<String, InfluxDBDatabaseConfig> configsByName) {
        this.configsByName = Collections.unmodifiableMap(configsByName);
    }

    /**
     * Parse InfluxDB database configurations from the given Configuration.
     * If {@code SINK_INFLUX_DATABASES_CONFIG} is set, parse it as a JSON array.
     * Otherwise, fall back to legacy flat keys and create a single "default" entry.
     */
    public static InfluxDBConfigurationParser parse(Configuration configuration) {
        String jsonConfig = configuration.getString(
                Constants.SINK_INFLUX_DATABASES_CONFIG_KEY,
                Constants.SINK_INFLUX_DATABASES_CONFIG_DEFAULT);

        if (!Strings.isNullOrEmpty(jsonConfig)) {
            return parseJsonConfig(jsonConfig);
        }
        return parseLegacyConfig(configuration);
    }

    private static InfluxDBConfigurationParser parseJsonConfig(String jsonConfig) {
        Map<String, InfluxDBDatabaseConfig> configs = new LinkedHashMap<>();
        try {
            JsonArray array = GSON.fromJson(jsonConfig, JsonArray.class);
            if (array == null || array.size() == 0) {
                throw new IllegalArgumentException("SINK_INFLUX_DATABASES_CONFIG is empty or not a valid JSON array");
            }
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                String name = getRequiredString(obj, "name");
                String url = getRequiredString(obj, "url");
                String username = getStringOrDefault(obj, "username", "");
                String password = getStringOrDefault(obj, "password", "");
                String dbName = getRequiredString(obj, "dbName");
                String retentionPolicy = getStringOrDefault(obj, "retentionPolicy", "");
                int batchSize = getIntOrDefault(obj, "batchSize", 0);
                int flushDurationMs = getIntOrDefault(obj, "flushDurationMs", 0);

                InfluxDBDatabaseConfig config = new InfluxDBDatabaseConfig(
                        name, url, username, password, dbName, retentionPolicy, batchSize, flushDurationMs);

                if (configs.containsKey(name)) {
                    throw new IllegalArgumentException("Duplicate database name in SINK_INFLUX_DATABASES_CONFIG: " + name);
                }
                configs.put(name, config);
            }
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON in SINK_INFLUX_DATABASES_CONFIG: " + e.getMessage(), e);
        }
        return new InfluxDBConfigurationParser(configs);
    }

    private static InfluxDBConfigurationParser parseLegacyConfig(Configuration configuration) {
        String url = configuration.getString(Constants.SINK_INFLUX_URL_KEY, Constants.SINK_INFLUX_URL_DEFAULT);
        String username = configuration.getString(Constants.SINK_INFLUX_USERNAME_KEY, Constants.SINK_INFLUX_USERNAME_DEFAULT);
        String password = configuration.getString(Constants.SINK_INFLUX_PASSWORD_KEY, Constants.SINK_INFLUX_PASSWORD_DEFAULT);
        String dbName = configuration.getString(Constants.SINK_INFLUX_DB_NAME_KEY, Constants.SINK_INFLUX_DB_NAME_DEFAULT);
        String retentionPolicy = configuration.getString(Constants.SINK_INFLUX_RETENTION_POLICY_KEY, Constants.SINK_INFLUX_RETENTION_POLICY_DEFAULT);
        int batchSize = configuration.getInteger(Constants.SINK_INFLUX_BATCH_SIZE_KEY, Constants.SINK_INFLUX_BATCH_SIZE_DEFAULT);
        int flushDurationMs = configuration.getInteger(Constants.SINK_INFLUX_FLUSH_DURATION_MS_KEY, Constants.SINK_INFLUX_FLUSH_DURATION_MS_DEFAULT);

        InfluxDBDatabaseConfig config = new InfluxDBDatabaseConfig(
                InfluxDBDatabaseConfig.DEFAULT_NAME, url, username, password,
                dbName, retentionPolicy, batchSize, flushDurationMs);

        Map<String, InfluxDBDatabaseConfig> configs = new LinkedHashMap<>();
        configs.put(InfluxDBDatabaseConfig.DEFAULT_NAME, config);
        return new InfluxDBConfigurationParser(configs);
    }

    /**
     * Get the configuration for a named database.
     *
     * @param name the logical database name
     * @return the database configuration
     * @throws IllegalArgumentException if no database with that name is configured
     */
    public InfluxDBDatabaseConfig getDatabase(String name) {
        InfluxDBDatabaseConfig config = configsByName.get(name);
        if (config == null) {
            throw new IllegalArgumentException("No InfluxDB database configured with name: " + name
                    + ". Available: " + configsByName.keySet());
        }
        return config;
    }

    /**
     * Get the default database configuration (used for backward compatibility).
     *
     * @return the default database configuration
     * @throws IllegalArgumentException if no default database is configured
     */
    public InfluxDBDatabaseConfig getDefaultDatabase() {
        return getDatabase(InfluxDBDatabaseConfig.DEFAULT_NAME);
    }

    /**
     * Check if a named database is configured.
     */
    public boolean hasDatabase(String name) {
        return configsByName.containsKey(name);
    }

    /**
     * Get all configured database configs.
     */
    public List<InfluxDBDatabaseConfig> getAllDatabases() {
        return new ArrayList<>(configsByName.values());
    }

    /**
     * Get the number of configured databases.
     */
    public int size() {
        return configsByName.size();
    }

    private static String getRequiredString(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            throw new IllegalArgumentException("Missing required field '" + field + "' in SINK_INFLUX_DATABASES_CONFIG entry");
        }
        String value = obj.get(field).getAsString();
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Field '" + field + "' must not be empty in SINK_INFLUX_DATABASES_CONFIG entry");
        }
        return value;
    }

    private static String getStringOrDefault(JsonObject obj, String field, String defaultValue) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            return defaultValue;
        }
        return obj.get(field).getAsString();
    }

    private static int getIntOrDefault(JsonObject obj, String field, int defaultValue) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            return defaultValue;
        }
        return obj.get(field).getAsInt();
    }
}
