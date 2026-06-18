package com.gotocompany.dagger.common.configuration;

import org.apache.flink.api.java.utils.ParameterTool;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Serializable, typed view over a Flink {@link ParameterTool} that exposes a Dagger job's
 * configuration.
 *
 * <p>It wraps the parameters supplied at job submission and provides typed accessors (string,
 * string array, integer, boolean, long) with optional defaults, so the rest of the codebase can
 * read configuration keys without dealing with parsing. Being {@link Serializable}, the
 * configuration can be captured by operators and shipped with the Flink job graph.
 */
public class Configuration implements Serializable {
    /** The underlying Flink parameter source backing all lookups. */
    private final ParameterTool param;

    /**
     * Wraps the given Flink parameters.
     *
     * @param param the parameter source holding the job's configuration key/value pairs
     */
    public Configuration(ParameterTool param) {
        this.param = param;
    }

    /**
     * Returns the underlying Flink parameter source.
     *
     * @return the wrapped {@link ParameterTool}
     */
    public ParameterTool getParam() {
        return param;
    }

    /**
     * Returns the value of a configuration key as a string.
     *
     * @param configKey the configuration key to look up
     * @return the configured value, or {@code null} if the key is absent
     */
    public String getString(String configKey) {
        return param.get(configKey);
    }

    /**
     * Returns the value of a configuration key as a string, or a default when absent.
     *
     * @param configKey    the configuration key to look up
     * @param defaultValue the value to return when the key is not present
     * @return the configured value, or {@code defaultValue} if the key is absent
     */
    public String getString(String configKey, String defaultValue) {
        return param.get(configKey, defaultValue);
    }

    /**
     * Returns the value of a configuration key split into a trimmed string array.
     *
     * <p>The raw value is split on commas with each element trimmed. When the key is missing or its
     * value is blank, the supplied default array is returned instead.
     *
     * @param configKey    the configuration key to look up
     * @param defaultValue the array to return when the key is absent or blank
     * @return the parsed comma-separated values, or {@code defaultValue} if none are present
     */
    public String[] getStringArray(String configKey, String[] defaultValue) {
        String value = param.get(configKey);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return Arrays.stream(value.split(",")).map(String::trim).toArray(String[]::new);
    }

    /**
     * Returns the value of a configuration key as an integer, or a default when absent.
     *
     * @param configKey    the configuration key to look up
     * @param defaultValue the value to return when the key is not present
     * @return the configured integer, or {@code defaultValue} if the key is absent
     */
    public Integer getInteger(String configKey, Integer defaultValue) {
        return param.getInt(configKey, defaultValue);
    }

    /**
     * Returns the value of a configuration key as a boolean, or a default when absent.
     *
     * @param configKey    the configuration key to look up
     * @param defaultValue the value to return when the key is not present
     * @return the configured boolean, or {@code defaultValue} if the key is absent
     */
    public Boolean getBoolean(String configKey, Boolean defaultValue) {
        return param.getBoolean(configKey, defaultValue);
    }

    /**
     * Returns the value of a configuration key as a long, or a default when absent.
     *
     * @param configKey    the configuration key to look up
     * @param defaultValue the value to return when the key is not present
     * @return the configured long, or {@code defaultValue} if the key is absent
     */
    public Long getLong(String configKey, Long defaultValue) {
        return param.getLong(configKey, defaultValue);
    }
}
