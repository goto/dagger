package com.gotocompany.dagger.common.serde;

import org.apache.flink.api.java.typeutils.ResultTypeQueryable;

import java.io.Serializable;

/**
 * Common contract implemented by every Dagger source deserializer.
 *
 * <p>A {@code DaggerDeserializer} turns raw bytes consumed from a source (for example a Kafka
 * record value or a Parquet {@code SimpleGroup}) into a Flink {@link org.apache.flink.types.Row}
 * that subsequently flows through the streaming job. Concrete implementations include the
 * protobuf, JSON and Parquet deserializers, all of which carry this marker so they can be wired
 * into Dagger sources interchangeably.
 *
 * <p>The interface deliberately combines two concerns required by Flink. It extends
 * {@link Serializable} so the deserializer can be shipped to task managers as part of the
 * serialized job graph, and it extends {@link ResultTypeQueryable} so Flink can statically query
 * the {@code TypeInformation} of the records this deserializer produces (needed for the SQL
 * planner and for state/serializer selection).
 *
 * @param <T> the element type emitted by the deserializer, typically
 *            {@link org.apache.flink.types.Row}
 */
public interface DaggerDeserializer<T> extends Serializable, ResultTypeQueryable<T> {

}
