package com.gotocompany.dagger.core.sink.kafka;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.DataTypes;
import com.gotocompany.dagger.core.sink.kafka.builder.KafkaJsonSerializerBuilder;
import com.gotocompany.dagger.core.sink.kafka.builder.KafkaProtoSerializerBuilder;
import com.gotocompany.dagger.core.utils.Constants;

/**
 * Factory that selects the appropriate {@link KafkaSerializerBuilder} for the configured Kafka sink
 * output encoding.
 *
 * <p>It reads {@code SINK_KAFKA_DATA_TYPE} (defaulting to {@code PROTO}) and returns a
 * {@link KafkaJsonSerializerBuilder} for {@code JSON} output or a {@link KafkaProtoSerializerBuilder}
 * otherwise, so callers such as the {@code SinkOrchestrator} can obtain a serializer without knowing
 * the concrete encoding.
 */
public class KafkaSerializationSchemaFactory {
    /**
     * Returns the serializer builder matching the configured Kafka sink output data type.
     *
     * @param configuration             the job configuration; its {@code SINK_KAFKA_DATA_TYPE} selects
     *                                  the encoding (defaults to {@code PROTO})
     * @param stencilClientOrchestrator the Stencil client orchestrator used by the protobuf builder to
     *                                  resolve descriptors (unused for JSON output)
     * @param columnNames               the output column names mapped onto the serialized record
     * @return a {@link KafkaJsonSerializerBuilder} for {@code JSON}, otherwise a
     *         {@link KafkaProtoSerializerBuilder}
     */
    public static KafkaSerializerBuilder getSerializationSchema(Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, String[] columnNames) {
        DataTypes dataTypes = DataTypes.valueOf(configuration.getString(Constants.SINK_KAFKA_DATA_TYPE, "PROTO"));

        if (dataTypes == DataTypes.JSON) {
            return new KafkaJsonSerializerBuilder(configuration);
        }
        return new KafkaProtoSerializerBuilder(configuration, stencilClientOrchestrator, columnNames);
    }
}
