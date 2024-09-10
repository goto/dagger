# Kafka

Kafka topics are used as the source and output of daggers. Both of source and output kafka configurations are defined through the properties file.

## Source Kafka Configuration

There can be multiple source kafka configurations in the properties file. Source configurations are defined through `STREAMS` property.
Here are the predefined properties for source kafka configuration:

- SOURCE_KAFKA_CONSUMER_CONFIG_AUTO_COMMIT_ENABLE
- SOURCE_KAFKA_CONSUMER_CONFIG_GROUP_ID
- SOURCE_KAFKA_CONSUMER_CONFIG_BOOTSTRAP_SERVERS
- SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL
- SOURCE_KAFKA_CONSUMER_CONFIG_SASL_MECHANISM
- SOURCE_KAFKA_CONSUMER_CONFIG_SASL_JAAS_CONFIG
- SOURCE_KAFKA_CONSUMER_ADDITIONAL_CONFIGURATIONS
- SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEY_PASSWORD
- SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_LOCATION
- SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_PASSWORD
- SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_TYPE
- SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_LOCATION
- SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_PASSWORD
- SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_TYPE
- SOURCE_KAFKA_CONSUMER_CONFIG_SSL_PROTOCOL

Additional kafka configuration can be passed through the `SOURCE_KAFKA_CONSUMER_ADDITIONAL_CONFIGURATIONS` property. This property should be a json key-value map.
For example : 
- SOURCE_KAFKA_CONSUMER_ADDITIONAL_CONFIGURATIONS={"SOURCE_KAFKA_CONSUMER_CONFIG_KEY_DESERIALIZER":"org.apache.kafka.common.serialization.StringDeserializer","SOURCE_KAFKA_CONSUMER_CONFIG_VALUE_DESERIALIZER":"org.apache.kafka.common.serialization.StringDeserializer"}


## Sink Kafka Configuration

There is only one sink kafka configuration in the properties file. Sink configuration is defined by properties having `SINK_KAFKA_PRODUCER_CONFIG`

```properties

```bash