package com.gotocompany.dagger.common.configuration;

import com.google.gson.Gson;
import org.apache.flink.api.java.utils.ParameterTool;

import java.io.Serializable;

public class Configuration implements Serializable {
    private static final Gson GSON = new Gson();
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

    public String[] getStringArray(String configKey, String defaultValue) {
        String jsonStringArray = param.get(configKey, defaultValue);
        return GSON.fromJson(jsonStringArray, String[].class);
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
