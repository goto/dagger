package com.gotocompany.dagger.core.deserializer;

import com.gotocompany.dagger.common.serde.DaggerDeserializer;

/**
 * Strategy interface for components that can supply a {@link DaggerDeserializer} for a Dagger
 * stream source.
 *
 * <p>Each implementation targets a particular combination of source (for example Kafka or Parquet)
 * and input schema/data type (for example Protobuf or JSON). The {@link DaggerDeserializerFactory}
 * asks every provider whether it {@link #canProvide() can provide} a deserializer for the current
 * stream configuration and uses the first one that accepts.
 *
 * @param <D> the record type produced by the deserializer, typically a Flink
 *            {@link org.apache.flink.types.Row}
 */
public interface DaggerDeserializerProvider<D> {
    /**
     * Builds the deserializer for the current stream configuration.
     *
     * <p>Should only be called when {@link #canProvide()} returns {@code true}.
     *
     * @return the deserializer that turns raw source records into {@code D} instances
     */
    DaggerDeserializer<D> getDaggerDeserializer();

    /**
     * Indicates whether this provider can supply a deserializer for the current stream configuration.
     *
     * @return {@code true} if the configured source and data type are compatible with this provider
     */
    boolean canProvide();
}
