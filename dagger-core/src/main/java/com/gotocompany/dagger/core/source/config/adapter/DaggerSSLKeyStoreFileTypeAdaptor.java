package com.gotocompany.dagger.core.source.config.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import com.gotocompany.dagger.core.utils.Constants;

import java.io.IOException;
import java.util.Arrays;

/**
 * Gson {@link TypeAdapter} that validates the Kafka consumer SSL keystore file type while a
 * {@code StreamConfig} is deserialized.
 *
 * <p>It is wired in through a {@code @JsonAdapter} annotation on the stream config's
 * {@code sslKeystoreType} field and accepts only the store formats listed in
 * {@link Constants#SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SSL_STORE_FILE_TYPE} ({@code JKS},
 * {@code PKCS12} or {@code PEM}); any other value aborts job startup with an
 * {@link InvalidConfigurationException}.
 */
public class DaggerSSLKeyStoreFileTypeAdaptor extends TypeAdapter<String> {
    /**
     * Serializes the keystore type value back to JSON, emitting a JSON {@code null} when it is unset.
     *
     * @param jsonWriter the writer receiving the serialized value
     * @param value      the keystore type to write; a {@code null} is rendered as a JSON null literal
     * @throws IOException if writing to the underlying JSON stream fails
     */
    @Override
    public void write(JsonWriter jsonWriter, String value) throws IOException {
        if (value == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(value);
    }

    /**
     * Reads the keystore type value from JSON and validates it against the supported store formats.
     *
     * @param jsonReader the reader positioned at the keystore type string
     * @return the keystore type when it is one of the supported values
     * @throws IOException                  if reading from the underlying JSON stream fails
     * @throws InvalidConfigurationException if the value is not present in
     *                                       {@link Constants#SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SSL_STORE_FILE_TYPE}
     */
    @Override
    public String read(JsonReader jsonReader) throws IOException {
        String keyStoreFileType = jsonReader.nextString();
        if (Arrays.stream(Constants.SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SSL_STORE_FILE_TYPE).anyMatch(keyStoreFileType::equals)) {
            return keyStoreFileType;
        } else {
            throw new InvalidConfigurationException(String.format("Configured wrong SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_TYPE_KEY supported values are %s", Arrays.toString(Constants.SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SSL_STORE_FILE_TYPE)));
        }
    }
}
