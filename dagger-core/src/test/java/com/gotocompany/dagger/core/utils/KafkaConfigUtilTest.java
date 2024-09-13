package com.gotocompany.dagger.core.utils;

import com.gotocompany.dagger.core.enumeration.KafkaConnectorTypesMetadata;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class KafkaConfigUtilTest {

    @Test
    public void shouldParseMatchingSourceKafkaConsumerConfiguration() {
        Properties properties = new Properties();
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1", "value1");
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2", "value2");
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIGKEY_3", "value3");
        properties.put("INVALID_KEY_4", "value4");

        Properties kafkaProperties = KafkaConfigUtil.parseKafkaConfiguration(KafkaConnectorTypesMetadata.SOURCE, properties);

        assertEquals(2, kafkaProperties.size());
        assertEquals("value1", kafkaProperties.getProperty("key.1"));
        assertEquals("value2", kafkaProperties.getProperty("key.2"));
    }

    @Test
    public void shouldParseMatchingSinkKafkaProducerConfiguration() {
        Properties properties = new Properties();
        properties.put("SINK_KAFKA_PRODUCER_CONFIG_KEY_1", "value1");
        properties.put("SINK_KAFKA_PRODUCER_CONFIG_KEY_2", "value2");
        properties.put("SINK_KAFKA_PRODUCER_CONFIGKEY_3", "value3");
        properties.put("INVALID_KEY_4", "value4");

        Properties kafkaProperties = KafkaConfigUtil.parseKafkaConfiguration(KafkaConnectorTypesMetadata.SINK, properties);

        assertEquals(2, kafkaProperties.size());
        assertEquals("value1", kafkaProperties.getProperty("key.1"));
        assertEquals("value2", kafkaProperties.getProperty("key.2"));
    }

}
