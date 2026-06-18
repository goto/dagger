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

/**
 * Gson {@link TypeAdapter} that reads and validates arbitrary extra Kafka consumer properties while a
 * {@code StreamConfig} is deserialized.
 *
 * <p>It is wired in through a {@code @JsonAdapter} annotation on the stream config's
 * {@code additionalConsumerConfigurations} field, letting operators pass through Kafka consumer
 * settings that are not modelled as dedicated fields. Every key must match the consumer-config naming
 * pattern of {@link KafkaConnectorTypesMetadata#SOURCE}, and entries whose value is {@code null} are
 * dropped.
 */
public class DaggerKafkaConsumerAdditionalConfigurationsAdaptor extends TypeAdapter<Map<String, String>> {

    /**
     * Serializes the additional consumer configuration map to JSON.
     *
     * <p>The map is converted to its JSON object form with a plain {@link Gson} instance and written
     * verbatim to the stream.
     *
     * @param jsonWriter      the writer receiving the serialized map
     * @param stringStringMap the additional consumer properties to serialize
     * @throws IOException if writing to the underlying JSON stream fails
     */
    @Override
    public void write(JsonWriter jsonWriter, Map<String, String> stringStringMap) throws IOException {
        Gson gson = new Gson();
        jsonWriter.jsonValue(gson.toJson(stringStringMap));
    }

    /**
     * Reads the additional consumer configuration object, validating keys and discarding null values.
     *
     * <p>Each key is matched against the {@link KafkaConnectorTypesMetadata#SOURCE} configuration
     * pattern; if any key fails to match, the entire configuration is rejected. The surviving entries
     * whose values are non-{@code null} are returned as the parsed map.
     *
     * @param jsonReader the reader positioned at the JSON object of additional properties
     * @return a map of the valid, non-{@code null} additional consumer properties
     * @throws IOException              if reading from the underlying JSON stream fails
     * @throws IllegalArgumentException if any key does not match the consumer configuration pattern
     */
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
