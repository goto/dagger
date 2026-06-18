package com.gotocompany.dagger.core.source.config.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import com.gotocompany.dagger.core.utils.Constants;

import java.io.IOException;
import java.util.Arrays;

/**
 * Gson {@link TypeAdapter} that validates the Kafka consumer security protocol while a
 * {@code StreamConfig} is deserialized.
 *
 * <p>It is wired in through a {@code @JsonAdapter} annotation on the stream config's
 * {@code securityProtocol} field and accepts only the protocols listed in
 * {@link Constants#SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL} ({@code SASL_PLAINTEXT},
 * {@code SASL_SSL} or {@code SSL}); any other value aborts job startup with an
 * {@link InvalidConfigurationException}.
 */
public class DaggerSecurityProtocolAdaptor extends TypeAdapter<String> {
    /**
     * Serializes the security protocol value back to JSON, emitting a JSON {@code null} when it is unset.
     *
     * @param jsonWriter the writer receiving the serialized value
     * @param value      the security protocol to write; a {@code null} is rendered as a JSON null literal
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
     * Reads the security protocol value from JSON and validates it against the supported protocols.
     *
     * @param jsonReader the reader positioned at the security protocol string
     * @return the security protocol when it is one of the supported values
     * @throws IOException                  if reading from the underlying JSON stream fails
     * @throws InvalidConfigurationException if the value is not present in
     *                                       {@link Constants#SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL}
     */
    @Override
    public String read(JsonReader jsonReader) throws IOException {
        String securityProtocol = jsonReader.nextString();
        if (Arrays.stream(Constants.SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL).anyMatch(securityProtocol::equals)) {
            return securityProtocol;
        } else {
            throw new InvalidConfigurationException(String.format("Configured wrong SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL supported values are %s", Arrays.toString(Constants.SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL)));
        }
    }
}
