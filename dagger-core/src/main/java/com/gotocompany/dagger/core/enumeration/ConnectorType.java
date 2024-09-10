package com.gotocompany.dagger.core.enumeration;

import lombok.Getter;

@Getter
public enum ConnectorType {
    SOURCE("SOURCE_KAFKA_CONSUMER_CONFIG_(.*)"), SINK("SINK_KAFKA_PRODUCER_CONFIG_(.*)");

    ConnectorType(String prefixPattern) {
        this.prefixPattern = prefixPattern;
    }

    private final String prefixPattern;
}
