package com.gotocompany.dagger.core.deserializer;

import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.common.serde.DataTypes;
import com.gotocompany.dagger.common.serde.json.deserialization.JsonDeserializer;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import com.gotocompany.dagger.core.source.config.models.SourceDetails;
import com.gotocompany.dagger.core.source.config.models.SourceName;
import org.apache.flink.types.Row;

import java.util.Arrays;
import java.util.HashSet;

import static com.gotocompany.dagger.common.serde.DataTypes.JSON;

/**
 * Supplies a {@link JsonDeserializer} for Kafka-backed streams whose payload is JSON encoded.
 *
 * <p>This provider accepts a stream only when every configured source is a Kafka source
 * ({@link SourceName#KAFKA_SOURCE} or {@link SourceName#KAFKA_CONSUMER}) and the input schema type
 * is {@link DataTypes#JSON}. The resulting deserializer maps JSON documents onto Flink
 * {@link Row} records using the configured JSON schema and event-timestamp field.
 */
public class JsonDeserializerProvider implements DaggerDeserializerProvider<Row> {
    /** Stream configuration carrying the JSON schema and the event-timestamp field name. */
    private final StreamConfig streamConfig;
    /** Source types this provider can deserialize: the Kafka source and the Kafka consumer. */
    private static final HashSet<SourceName> COMPATIBLE_SOURCES = new HashSet<>(Arrays.asList(SourceName.KAFKA_SOURCE, SourceName.KAFKA_CONSUMER));
    /** Input schema type this provider handles, namely JSON. */
    private static final DataTypes COMPATIBLE_INPUT_SCHEMA_TYPE = JSON;

    /**
     * Creates a JSON deserializer provider for the given stream.
     *
     * @param streamConfig the stream configuration carrying the JSON schema and the JSON
     *                     event-timestamp field name
     */
    public JsonDeserializerProvider(StreamConfig streamConfig) {
        this.streamConfig = streamConfig;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Constructs a {@link JsonDeserializer} from the configured JSON schema and event-timestamp
     * field name.
     *
     * @return a JSON-to-{@link Row} deserializer for the configured stream
     */
    @Override
    public DaggerDeserializer<Row> getDaggerDeserializer() {
        return new JsonDeserializer(streamConfig.getJsonSchema(), streamConfig.getJsonEventTimestampFieldName());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} only when every configured source is a compatible Kafka source and the
     * stream's data type is {@link DataTypes#JSON}.
     *
     * @return {@code true} if this provider can deserialize the configured stream, {@code false} otherwise
     */
    @Override
    public boolean canProvide() {
        SourceDetails[] sourceDetailsList = streamConfig.getSourceDetails();
        for (SourceDetails sourceDetails : sourceDetailsList) {
            SourceName sourceName = sourceDetails.getSourceName();
            DataTypes inputSchemaType = DataTypes.valueOf(streamConfig.getDataType());
            if (!COMPATIBLE_SOURCES.contains(sourceName) || !inputSchemaType.equals(COMPATIBLE_INPUT_SCHEMA_TYPE)) {
                return false;
            }
        }
        return true;
    }
}
