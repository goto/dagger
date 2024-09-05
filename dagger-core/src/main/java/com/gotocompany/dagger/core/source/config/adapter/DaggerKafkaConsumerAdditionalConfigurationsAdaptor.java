package com.gotocompany.dagger.core.source.config.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

public class DaggerKafkaConsumerAdditionalConfigurationsAdaptor extends TypeAdapter<Map<String, String>> {
    @Override
    public void write(JsonWriter jsonWriter, Map<String, String> stringStringMap) throws IOException {
        Gson gson = new Gson();
        jsonWriter.jsonValue(gson.toJson(stringStringMap));
    }

    @Override
    public Map<String, String> read(JsonReader jsonReader) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(jsonReader, Map.class);
    }
}
