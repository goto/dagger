package com.gotocompany.dagger.core.enumeration;

import java.util.regex.Pattern;

/**
 * Enumerates the two kinds of Kafka connector that Dagger configures, each paired with the prefix
 * used to namespace its configuration keys.
 *
 * <p>Dagger reads Kafka client settings from a flat configuration in which each key is prefixed
 * according to whether it applies to the consumer (source) or producer (sink). This enum captures
 * that prefix as a regex fragment so the matching consumer/producer properties can be extracted from
 * the overall configuration. The constants are {@code SOURCE} (consumer settings prefixed with
 * {@code SOURCE_KAFKA_CONSUMER_CONFIG_}) and {@code SINK} (producer settings prefixed with
 * {@code SINK_KAFKA_PRODUCER_CONFIG_}).
 */
public enum KafkaConnectorTypesMetadata {
    SOURCE("SOURCE_KAFKA_CONSUMER_CONFIG_+"), SINK("SINK_KAFKA_PRODUCER_CONFIG_+");

    /**
     * Associates a connector type with the prefix pattern that identifies its configuration keys.
     *
     * @param prefixPattern the leading regex fragment matching this connector's configuration keys
     */
    KafkaConnectorTypesMetadata(String prefixPattern) {
        this.prefixPattern = prefixPattern;
    }

    /** Leading regex fragment matching the configuration keys that belong to this connector type. */
    private final String prefixPattern;

    /**
     * Builds a case-insensitive {@link Pattern} matching configuration keys for this connector type
     * and capturing the remainder of each key.
     *
     * <p>The returned pattern anchors on this type's prefix and exposes everything after it as a
     * capturing group, so callers can strip the prefix and recover the underlying Kafka property name.
     *
     * @return a compiled, case-insensitive pattern for this connector's configuration keys
     */
    public Pattern getConfigurationPattern() {
        return Pattern.compile(String.format("^%s(.*)", prefixPattern), Pattern.CASE_INSENSITIVE);
    }

}
