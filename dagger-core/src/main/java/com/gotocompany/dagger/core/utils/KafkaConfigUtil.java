package com.gotocompany.dagger.core.utils;

import com.gotocompany.dagger.core.enumeration.ConnectorType;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KafkaConfigUtil {

    private static final Map<ConnectorType, Pattern> CONFIG_PATTERN;

    static {
        CONFIG_PATTERN = new HashMap<>();
        CONFIG_PATTERN.put(ConnectorType.SOURCE, Pattern.compile(ConnectorType.SOURCE.getPrefixPattern(), Pattern.CASE_INSENSITIVE));
        CONFIG_PATTERN.put(ConnectorType.SINK, Pattern.compile(ConnectorType.SINK.getPrefixPattern(), Pattern.CASE_INSENSITIVE));
    }

    public static Properties parseKafkaConfiguration(ConnectorType connectorType, Properties properties) {
        Pattern pattern = CONFIG_PATTERN.get(connectorType);
        Properties kafkaProperties = new Properties();
        Set<Object> configKeys = properties.keySet();

        for (Object key : configKeys) {
            Matcher matcher = pattern.matcher(key.toString());
            if (matcher.find()) {
                String kafkaConfigKey = matcher.group(1)
                        .toLowerCase()
                        .replaceAll("_", ".");
                kafkaProperties.setProperty(kafkaConfigKey, properties.get(key).toString());
            }
        }
        return kafkaProperties;
    }

}
