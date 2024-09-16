package com.gotocompany.dagger.core.source.config.adapter;

import com.google.gson.JsonSyntaxException;
import org.junit.Rule;
import org.junit.Test;
import com.google.gson.stream.JsonReader;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DaggerKafkaConsumerAdditionalConfigurationsAdaptorTest {

    private final DaggerKafkaConsumerAdditionalConfigurationsAdaptor daggerKafkaConsumerAdditionalConfigurationsAdaptor = new DaggerKafkaConsumerAdditionalConfigurationsAdaptor();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldParseEmptyProperty() throws IOException {
        String input = "{}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));

        Map<String, String> result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);

        assertEquals(0, result.size());
    }

    @Test
    public void shouldParseJsonStringToMap() throws IOException {
        String input = "{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\":\"value1\",\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\":\"value2\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1", "value1");
        expectedResult.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2", "value2");

        Map<String, String> result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);

        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldParseJsonStringWithCaseInsensitiveKeyToMap() throws IOException {
        String input = "{\"sOurCe_KAFKA_CONSUMER_CONFIG_key_1\":\"value1\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("sOurCe_KAFKA_CONSUMER_CONFIG_key_1", "value1");

        Map<String, String> result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);

        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldIgnoreNullValues() throws IOException {
        String input = "{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\":null,\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\":\"value2\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));

        Map<String, String> result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);

        assertFalse(result.containsKey("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1"));
        assertEquals("value2", result.get("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2"));
    }

    @Test
    public void shouldHandleSpecialCharactersInValues() throws IOException {
        String input = "{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY\":\"value with spaces and $pecial ch@racters\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));

        Map<String, String> result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);

        assertEquals("value with spaces and $pecial ch@racters", result.get("SOURCE_KAFKA_CONSUMER_CONFIG_KEY"));
    }

    @Test
    public void shouldHandleNumericalValue() throws IOException {
        String input = "{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\": \"120\", \"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\": \"120.5\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));

        Map<String, String> result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);

        assertEquals("120", result.get("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1"));
        assertEquals("120.5", result.get("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2"));
    }

    @Test
    public void shouldHandleBooleanValue() throws IOException {
        String input = "{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\": \"true\", \"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\": \"false\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));

        Map<String, String> result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);

        assertEquals("true", result.get("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1"));
        assertEquals("false", result.get("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2"));
    }

    @Test
    public void shouldWriteMapToStringJson() {
        Map<String, String> map = new HashMap<>();
        map.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1", "value1");
        map.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2", "120");
        map.put("source_kafka_consumer_config_key_3", "120.5");

        String result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.toJson(map);

        assertEquals("{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\":\"value1\",\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\":\"120\",\"source_kafka_consumer_config_key_3\":\"120.5\"}", result);
    }

    @Test
    public void shouldThrowExceptionForInvalidProperties() throws IOException {
        String input = "{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\":\"value1\",\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\":\"value2\",\"INVALID_KEY\":\"value3\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid additional kafka consumer configuration properties found: [INVALID_KEY]");

        daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);
    }

    @Test(expected = JsonSyntaxException.class)
    public void shouldThrowExceptionForMalformedJson() throws IOException {
        String input = "\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\":\"value1\",\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\":\"value2\"";
        JsonReader jsonReader = new JsonReader(new StringReader(input));

        daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);
    }

}
