package com.gotocompany.dagger.core.source.config.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.gotocompany.dagger.core.enumeration.KafkaConnectorTypesMetadata;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DaggerKafkaConsumerAdditionalConfigurationsAdaptor extends TypeAdapter<Map<String, String>> {

    @Override
    public void write(JsonWriter jsonWriter, Map<String, String> stringStringMap) throws IOException {
        Gson gson = new Gson();
        jsonWriter.jsonValue(gson.toJson(stringStringMap));
    }

    @Override
    public Map<String, String> read(JsonReader jsonReader) throws IOException {
        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(jsonReader, Map.class);
        List<String> invalidProps = map.keySet().stream()
                .filter(key -> !KafkaConnectorTypesMetadata.SOURCE.getConfigurationPattern()
                        .matcher(key)
                        .matches())
                .collect(Collectors.toList());
        if (!invalidProps.isEmpty()) {
            throw new IllegalArgumentException("Invalid additional kafka consumer configuration properties found: " + invalidProps);
        }
        return map.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
