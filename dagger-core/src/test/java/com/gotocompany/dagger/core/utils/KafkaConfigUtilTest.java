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

    @Test
    public void shouldReturnEmptyPropertiesWhenInputIsEmpty() {
        Properties properties = new Properties();
        Properties kafkaProperties = KafkaConfigUtil.parseKafkaConfiguration(KafkaConnectorTypesMetadata.SOURCE, properties);

        assertEquals(0, kafkaProperties.size());
    }

    @Test
    public void shouldReturnEmptyPropertiesWhenAllKeysAreInvalid() {
        Properties properties = new Properties();
        properties.put("INVALID_KEY_1", "value1");
        properties.put("INVALID_KEY_2", "value2");
        properties.put("ANOTHER_INVALID_KEY", "value3");

        Properties kafkaProperties = KafkaConfigUtil.parseKafkaConfiguration(KafkaConnectorTypesMetadata.SOURCE, properties);

        assertEquals(0, kafkaProperties.size());
    }

    @Test
    public void shouldParseOnlyValidKeysWhenMixedWithInvalidOnes() {
        Properties properties = new Properties();
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_1", "value1");
        properties.put("INVALID_KEY", "value2");
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIG_KEY_2", "value3");
        properties.put("ANOTHER_INVALID_KEY", "value4");

        Properties kafkaProperties = KafkaConfigUtil.parseKafkaConfiguration(KafkaConnectorTypesMetadata.SOURCE, properties);

        assertEquals(2, kafkaProperties.size());
        assertEquals("value1", kafkaProperties.getProperty("key.1"));
        assertEquals("value3", kafkaProperties.getProperty("key.2"));
    }

    @Test
    public void shouldParseCaseInsensitiveKeys() {
        Properties properties = new Properties();
        properties.put("source_kafka_consumer_config_KEY_1", "value1");
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIG_key_2", "value2");

        Properties kafkaProperties = KafkaConfigUtil.parseKafkaConfiguration(KafkaConnectorTypesMetadata.SOURCE, properties);

        assertEquals(2, kafkaProperties.size());
        assertEquals("value1", kafkaProperties.getProperty("key.1"));
        assertEquals("value2", kafkaProperties.getProperty("key.2"));
    }

    @Test
    public void shouldParseKeysWithMultipleUnderscores() {
        Properties properties = new Properties();
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIG_MULTI_WORD_KEY", "value1");
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIG_ANOTHER_MULTI_WORD_KEY", "value2");
        properties.put("SOURCE_KAFKA_CONSUMER_CONFIG__YET___ANOTHER_MULTI__WORD_KEY", "value3");

        Properties kafkaProperties = KafkaConfigUtil.parseKafkaConfiguration(KafkaConnectorTypesMetadata.SOURCE, properties);

        assertEquals(3, kafkaProperties.size());
        assertEquals("value1", kafkaProperties.getProperty("multi.word.key"));
        assertEquals("value2", kafkaProperties.getProperty("another.multi.word.key"));
        assertEquals("value3", kafkaProperties.getProperty("yet.another.multi.word.key"));
    }

}
