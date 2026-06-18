package com.gotocompany.dagger.common.serde;

/**
 * Enumerates the wire formats Dagger can deserialize records from on an input stream.
 *
 * <p>The selected value is typically derived from stream configuration and used to choose the
 * matching {@code DaggerDeserializer} implementation (a JSON-backed deserializer versus a
 * protobuf-backed one) when a Dagger source is constructed.
 */
public enum DataTypes {
    /** Records encoded as JSON documents, deserialized against a configured JSON schema. */
    JSON,
    /** Records encoded as Protocol Buffers messages, deserialized via a Stencil descriptor. */
    PROTO
}
