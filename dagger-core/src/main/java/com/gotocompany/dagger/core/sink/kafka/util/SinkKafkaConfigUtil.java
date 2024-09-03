package com.gotocompany.dagger.core.sink.kafka.util;

import com.gotocompany.dagger.common.configuration.Configuration;

import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SinkKafkaConfigUtil {

    private static final Pattern SINK_KAFKA_PRODUCER_CONFIG_REGEX = Pattern.compile("SINK_KAFKA_PRODUCER_CONFIG_(.*)", Pattern.CASE_INSENSITIVE);

    public static Properties parseBuiltInKafkaProperties(Configuration configuration) {
        Properties properties = new Properties();
        Set<String> configKeys = configuration.getParam()
                .getConfiguration()
                .keySet();

        for (String key : configKeys) {
            Matcher matcher = SINK_KAFKA_PRODUCER_CONFIG_REGEX.matcher(key);
            if (matcher.find()) {
                String kafkaConfigKey = matcher.group(1)
                        .toLowerCase()
                        .replaceAll("_", ".");
                properties.setProperty(kafkaConfigKey, configuration.getString(key, ""));
            }
        }
        return properties;
    }

}
