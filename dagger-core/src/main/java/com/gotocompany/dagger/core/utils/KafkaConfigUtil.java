package com.gotocompany.dagger.core.utils;

import com.gotocompany.dagger.core.enumeration.KafkaConnectorTypesMetadata;

import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Utility for translating Dagger's namespaced configuration entries into native Kafka client
 * properties.
 *
 * <p>Different Kafka connector roles (for example a source consumer or a sink producer) namespace
 * their settings with a role-specific prefix described by {@link KafkaConnectorTypesMetadata}. This
 * helper uses that metadata's pattern to pick the relevant entries and rewrite each key into the
 * dotted property name understood by the Kafka client.
 */
public class KafkaConfigUtil {

    /**
     * Extracts native Kafka client properties from a set of namespaced configuration entries.
     *
     * <p>Each key in {@code properties} is matched against the pattern from
     * {@code kafkaConnectorTypesMetadata}; for matching keys the first capture group is lowercased
     * and its underscore runs are collapsed into dots to form the Kafka property name (for example
     * {@code GROUP_ID} becomes {@code group.id}). Non-matching keys are ignored.
     *
     * @param kafkaConnectorTypesMetadata the connector metadata supplying the key-matching pattern
     * @param properties                  the namespaced configuration entries to translate
     * @return the extracted Kafka client properties with normalized dotted keys
     */
    public static Properties parseKafkaConfiguration(KafkaConnectorTypesMetadata kafkaConnectorTypesMetadata, Properties properties) {
        Properties kafkaProperties = new Properties();
        Set<Object> configKeys = properties.keySet();

        for (Object key : configKeys) {
            Matcher matcher = kafkaConnectorTypesMetadata.getConfigurationPattern()
                    .matcher(key.toString());
            if (matcher.find()) {
                String kafkaConfigKey = matcher.group(1)
                        .toLowerCase()
                        .replaceAll("_+", ".");
                kafkaProperties.setProperty(kafkaConfigKey, properties.get(key).toString());
            }
        }
        return kafkaProperties;
    }

}
