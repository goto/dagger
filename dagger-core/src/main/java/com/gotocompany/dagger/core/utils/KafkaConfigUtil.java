package com.gotocompany.dagger.core.utils;

import com.gotocompany.dagger.core.enumeration.KafkaConnectorTypesMetadata;

import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

public class KafkaConfigUtil {

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
