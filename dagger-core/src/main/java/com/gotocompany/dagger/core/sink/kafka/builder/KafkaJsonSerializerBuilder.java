package com.gotocompany.dagger.core.sink.kafka.builder;

import com.gotocompany.dagger.core.metrics.telemetry.TelemetryPublisher;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryTypes;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.formats.json.JsonRowSchemaConverter;
import org.apache.flink.formats.json.JsonRowSerializationSchema;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.exceptions.serde.InvalidJSONSchemaException;
import com.gotocompany.dagger.core.sink.kafka.KafkaSerializerBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link KafkaSerializerBuilder} that produces a JSON {@link KafkaRecordSerializationSchema} for the
 * Kafka sink.
 *
 * <p>On {@link #build()} it reads the output topic, stream and JSON schema from configuration, records
 * the topic and stream as telemetry, converts the JSON schema into a Flink {@code TypeInformation<Row>}
 * and builds a {@link JsonRowSerializationSchema} wrapped in a record serialization schema targeting
 * the output topic. As a {@link TelemetryPublisher} it exposes the collected metrics through
 * {@link #getTelemetry()}.
 */
public class KafkaJsonSerializerBuilder implements KafkaSerializerBuilder, TelemetryPublisher {
    /** Collected telemetry (output topic and stream) keyed by telemetry type. */
    private Map<String, List<String>> metrics;
    private Configuration configuration;

    /**
     * Creates a JSON serializer builder.
     *
     * @param configuration the job configuration providing the output topic, stream and JSON schema
     */
    public KafkaJsonSerializerBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.metrics = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the output topic and stream recorded during {@link #build()}.
     */
    @Override
    public Map<String, List<String>> getTelemetry() {
        return metrics;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reads the output topic, stream and JSON schema from configuration, records the topic and
     * stream as telemetry and notifies subscribers, converts the JSON schema into a Flink row type, and
     * returns a {@link KafkaRecordSerializationSchema} that serializes each row as JSON to the output
     * topic.
     *
     * @return a JSON-based {@link KafkaRecordSerializationSchema} for the output topic
     * @throws InvalidJSONSchemaException if the configured JSON schema is invalid and cannot be converted
     */
    @Override
    public KafkaRecordSerializationSchema build() {
        String outputTopic = configuration.getString(Constants.SINK_KAFKA_TOPIC_KEY, "");
        String outputStream = configuration.getString(Constants.SINK_KAFKA_STREAM_KEY, "");
        String outputJsonSchema = configuration.getString(Constants.SINK_KAFKA_JSON_SCHEMA_KEY, "");
        addMetric(TelemetryTypes.OUTPUT_TOPIC.getValue(), outputTopic);
        addMetric(TelemetryTypes.OUTPUT_STREAM.getValue(), outputStream);
        notifySubscriber();

        try {
            TypeInformation<Row> opTypeInfo = JsonRowSchemaConverter.convert(outputJsonSchema);
            JsonRowSerializationSchema jsonRowSerializationSchema = JsonRowSerializationSchema
                    .builder()
                    .withTypeInfo(opTypeInfo)
                    .build();
            return KafkaRecordSerializationSchema
                    .builder()
                    .setValueSerializationSchema(jsonRowSerializationSchema)
                    .setTopic(outputTopic)
                    .build();
        } catch (IllegalArgumentException exception) {
            throw new InvalidJSONSchemaException(exception);
        }
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
