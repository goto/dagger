package com.gotocompany.dagger.common.serde.proto.serialization;

import com.gotocompany.dagger.common.exceptions.serde.DaggerSerializationException;
import org.apache.flink.api.common.serialization.SerializationSchema.InitializationContext;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.types.Row;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Kafka sink serialization schema that turns Flink {@link Row} records into protobuf-encoded
 * {@link ProducerRecord}s for a Dagger Kafka sink.
 *
 * <p>The actual row-to-protobuf encoding is delegated to a {@link ProtoSerializer}, which produces
 * the key and value byte arrays; this class wraps those bytes into a {@link ProducerRecord} aimed
 * at the configured output topic. It implements Flink's {@code KafkaRecordSerializationSchema} so it
 * can be plugged directly into a Flink {@code KafkaSink}.
 */
public class KafkaProtoSerializer implements KafkaRecordSerializationSchema<Row> {
    /** Kafka topic the serialized records are published to; must be non-empty when serializing. */
    private final String outputTopic;
    /** Delegate that encodes a {@link Row} into protobuf key and value byte arrays. */
    private final ProtoSerializer protoSerializer;
    /** Logger (named {@code "KafkaSink"}) used to trace rows being written to Kafka. */
    private static final Logger LOGGER = LoggerFactory.getLogger("KafkaSink");

    /**
     * Creates a serializer with an empty output topic, delegating to
     * {@link #KafkaProtoSerializer(ProtoSerializer, String)}.
     *
     * <p>An output topic must be configured before records are written, because
     * {@link #serialize(Row, KafkaSinkContext, Long)} rejects an empty topic with a
     * {@link DaggerSerializationException}.
     *
     * @param protoSerializer the delegate that encodes rows into protobuf key/value bytes
     */
    public KafkaProtoSerializer(ProtoSerializer protoSerializer) {
        this(protoSerializer, "");
    }

    /**
     * Creates a serializer targeting a specific Kafka topic.
     *
     * @param protoSerializer the delegate that encodes rows into protobuf key/value bytes
     * @param outputTopic     the Kafka topic to publish serialized records to
     */
    public KafkaProtoSerializer(ProtoSerializer protoSerializer, String outputTopic) {
        this.protoSerializer = protoSerializer;
        this.outputTopic = outputTopic;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the default {@link KafkaRecordSerializationSchema} initialization; this
     * serializer holds no additional state that needs setting up.
     *
     * @param context     the serialization initialization context
     * @param sinkContext the Kafka sink context
     * @throws Exception if the default initialization fails
     */
    @Override
    public void open(InitializationContext context, KafkaSinkContext sinkContext) throws Exception {
        KafkaRecordSerializationSchema.super.open(context, sinkContext);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Encodes the given row into protobuf key and value byte arrays via the delegate
     * {@link ProtoSerializer} and wraps them in a {@link ProducerRecord} for the configured output
     * topic. The row being written is logged at info level.
     *
     * @param row       the Flink row to serialize
     * @param context   the Kafka sink context (unused)
     * @param timestamp the event timestamp supplied by Flink (unused)
     * @return a {@link ProducerRecord} carrying the protobuf key and value for the output topic
     * @throws DaggerSerializationException if no output topic has been configured
     */
    @Override
    public ProducerRecord<byte[], byte[]> serialize(Row row, KafkaSinkContext context, Long timestamp) {
        if (Objects.isNull(outputTopic) || outputTopic.equals("")) {
            throw new DaggerSerializationException("outputTopic is required");
        }
        LOGGER.info("row to kafka: " + row);
        byte[] key = protoSerializer.serializeKey(row);
        byte[] message = protoSerializer.serializeValue(row);
        return new ProducerRecord<>(outputTopic, key, message);
    }
}
