package com.gotocompany.dagger.core.source.config.adapter;

import org.junit.Test;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class DaggerKafkaConsumerAdditionalConfigurationsAdaptorTest {

    @Test
    public void shouldMapJsonStringToMap() throws IOException {
        String input = "{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\":\"value1\",\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\":\"value2\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));
        DaggerKafkaConsumerAdditionalConfigurationsAdaptor daggerKafkaConsumerAdditionalConfigurationsAdaptor = new DaggerKafkaConsumerAdditionalConfigurationsAdaptor();
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1", "value1");
        expectedResult.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2", "value2");

        Map<String, String> result = daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);

        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldThrowExceptionForInvalidProperties() throws IOException {
        String input = "{\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1\":\"value1\",\"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2\":\"value2\",\"INVALID_KEY\":\"value3\"}";
        JsonReader jsonReader = new JsonReader(new StringReader(input));
        DaggerKafkaConsumerAdditionalConfigurationsAdaptor daggerKafkaConsumerAdditionalConfigurationsAdaptor = new DaggerKafkaConsumerAdditionalConfigurationsAdaptor();

        try {
            daggerKafkaConsumerAdditionalConfigurationsAdaptor.read(jsonReader);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid additional kafka consumer configuration properties found: [INVALID_KEY]", e.getMessage());
        }
    }

}
