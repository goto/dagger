package com.gotocompany.dagger.core.sink.kafka;

import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;

/**
 * Builds the Flink {@link KafkaRecordSerializationSchema} that serializes output rows before they are
 * produced to the Kafka sink topic.
 *
 * <p>Each implementation encapsulates a particular output encoding: {@code KafkaProtoSerializerBuilder}
 * for protobuf and {@code KafkaJsonSerializerBuilder} for JSON. The concrete builder is chosen by
 * {@code KafkaSerializationSchemaFactory} from the configured sink data type, and implementations also
 * publish output topic/proto/stream telemetry while building.
 */
public interface KafkaSerializerBuilder {
    /**
     * Builds the Kafka record serialization schema for the configured output topic and encoding.
     *
     * @return the {@link KafkaRecordSerializationSchema} that maps each output row to a Kafka
     *         producer record
     */
    KafkaRecordSerializationSchema build();
}
