package com.gotocompany.dagger.core.sink.kafka.builder;

import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryPublisher;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryTypes;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.proto.serialization.KafkaProtoSerializer;
import com.gotocompany.dagger.core.sink.kafka.KafkaSerializerBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link KafkaSerializerBuilder} that produces a protobuf {@link KafkaRecordSerializationSchema} for
 * the Kafka sink.
 *
 * <p>On {@link #build()} it reads the output topic, proto key/message classes and stream from
 * configuration, records them as telemetry, and wraps a {@link ProtoSerializer} (which resolves
 * protobuf descriptors through the {@link StencilClientOrchestrator}) in a {@link KafkaProtoSerializer}
 * bound to the output topic. As a {@link TelemetryPublisher} it exposes the collected output
 * topic/proto/stream metrics through {@link #getTelemetry()}.
 */
public class KafkaProtoSerializerBuilder implements KafkaSerializerBuilder, TelemetryPublisher {
    /** Collected telemetry (output topic, proto message and stream) keyed by telemetry type. */
    private Map<String, List<String>> metrics;
    private Configuration configuration;
    private StencilClientOrchestrator stencilClientOrchestrator;
    private String[] columnNames;

    /**
     * Creates a protobuf serializer builder.
     *
     * @param configuration             the job configuration providing the output topic and proto settings
     * @param stencilClientOrchestrator the Stencil client orchestrator used to resolve protobuf descriptors
     * @param columnNames               the output column names mapped onto the protobuf message fields
     */
    public KafkaProtoSerializerBuilder(Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, String[] columnNames) {
        this.configuration = configuration;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.columnNames = columnNames;
        this.metrics = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reads the output topic, proto key/message classes and stream from configuration, records them
     * as telemetry and notifies subscribers, then returns a {@link KafkaProtoSerializer} that encodes
     * each row through a {@link ProtoSerializer} and targets the configured output topic.
     *
     * @return a protobuf-based {@link KafkaRecordSerializationSchema} for the output topic
     */
    @Override
    public KafkaRecordSerializationSchema build() {
        String outputTopic = configuration.getString(Constants.SINK_KAFKA_TOPIC_KEY, "");
        String outputProtoKey = configuration.getString(Constants.SINK_KAFKA_PROTO_KEY, null);
        String outputProtoMessage = configuration.getString(Constants.SINK_KAFKA_PROTO_MESSAGE_KEY, "");
        String outputStream = configuration.getString(Constants.SINK_KAFKA_STREAM_KEY, "");
        addMetric(TelemetryTypes.OUTPUT_TOPIC.getValue(), outputTopic);
        addMetric(TelemetryTypes.OUTPUT_PROTO.getValue(), outputProtoMessage);
        addMetric(TelemetryTypes.OUTPUT_STREAM.getValue(), outputStream);
        notifySubscriber();

        ProtoSerializer protoSerializer = new ProtoSerializer(outputProtoKey, outputProtoMessage, columnNames, stencilClientOrchestrator);
        return new KafkaProtoSerializer(protoSerializer, outputTopic);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the output topic, proto message and stream recorded during {@link #build()}.
     */
    @Override
    public Map<String, List<String>> getTelemetry() {
        return metrics;
    }

    /**
     * Appends a telemetry value under the given key, creating the backing list on first use.
     *
     * @param key   the telemetry key
     * @param value the telemetry value to record
     */
    private void addMetric(String key, String value) {
        metrics.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }
}
