package com.gotocompany.dagger.core.enumeration;

import java.util.regex.Pattern;

public enum KafkaConnectorTypesMetadata {
    SOURCE("SOURCE_KAFKA_CONSUMER_CONFIG_"), SINK("SINK_KAFKA_PRODUCER_CONFIG_");

    KafkaConnectorTypesMetadata(String prefixPattern) {
        this.prefixPattern = prefixPattern;
    }

    private final String prefixPattern;

    public Pattern getConfigurationPattern() {
        return Pattern.compile(String.format("^%s(.*)", prefixPattern), Pattern.CASE_INSENSITIVE);
    }

}
