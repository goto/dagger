package com.gotocompany.dagger.core.sink.kafka.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SinkKafkaConfigUtil {
    private static final Pattern SINK_KAFKA_CONFIG_REGEX = Pattern.compile("SINK_KAFKA_(.*)");

    public static String getKafkaConfigKey(String key) {
        Matcher matcher = SINK_KAFKA_CONFIG_REGEX.matcher(key);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid kafka config key: " + key);
        }
        return matcher.group(1).toLowerCase().replace("_", ".");
    }

}
