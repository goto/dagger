package com.gotocompany.dagger.common.configuration;

import org.apache.flink.api.java.utils.ParameterTool;

import java.io.Serializable;
import java.util.Arrays;

public class Configuration implements Serializable {
    private final ParameterTool param;

    public Configuration(ParameterTool param) {
        this.param = param;
    }

    public ParameterTool getParam() {
        return param;
    }

    public String getString(String configKey) {
        return param.get(configKey);
    }

    public String getString(String configKey, String defaultValue) {
        return param.get(configKey, defaultValue);
    }

    public String[] getStringArray(String configKey, String[] defaultValue) {
        String value = param.get(configKey);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return Arrays.stream(value.split(",")).map(String::trim).toArray(String[]::new);
    }

    public Integer getInteger(String configKey, Integer defaultValue) {
        return param.getInt(configKey, defaultValue);
    }

    public Boolean getBoolean(String configKey, Boolean defaultValue) {
        return param.getBoolean(configKey, defaultValue);
    }

    public Long getLong(String configKey, Long defaultValue) {
        return param.getLong(configKey, defaultValue);
    }
}
