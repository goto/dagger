package com.gotocompany.dagger.core.deserializer;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.common.serde.DataTypes;
import com.gotocompany.dagger.common.serde.proto.deserialization.ProtoDeserializer;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import com.gotocompany.dagger.core.source.config.models.SourceDetails;
import com.gotocompany.dagger.core.source.config.models.SourceName;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.types.Row;

import java.util.Arrays;
import java.util.HashSet;

import static com.gotocompany.dagger.common.serde.DataTypes.PROTO;

/**
 * Supplies a {@link ProtoDeserializer} for Kafka-backed streams whose payload is Protobuf encoded.
 *
 * <p>This provider accepts a stream only when every configured source is a Kafka source
 * ({@link SourceName#KAFKA_SOURCE} or {@link SourceName#KAFKA_CONSUMER}) and the input schema type
 * is {@link DataTypes#PROTO}. The resulting deserializer decodes Protobuf messages into Flink
 * {@link Row} records using the configured proto class and the Stencil schema registry.
 */
public class ProtoDeserializerProvider implements DaggerDeserializerProvider<Row> {
    /** Source types this provider can deserialize: the Kafka source and the Kafka consumer. */
    private static final HashSet<SourceName> COMPATIBLE_SOURCES = new HashSet<>(Arrays.asList(SourceName.KAFKA_SOURCE, SourceName.KAFKA_CONSUMER));
    /** Input schema type this provider handles, namely Protobuf. */
    private static final DataTypes COMPATIBLE_INPUT_SCHEMA_TYPE = PROTO;
    /** Stream configuration describing the source(s), proto class, and event-timestamp field. */
    protected final StreamConfig streamConfig;
    /** Job configuration, used here to resolve the Flink rowtime attribute name. */
    protected final Configuration configuration;
    /** Orchestrator providing Stencil descriptors for decoding the Protobuf payload. */
    protected final StencilClientOrchestrator stencilClientOrchestrator;

    /**
     * Creates a Protobuf deserializer provider for the given stream.
     *
     * @param streamConfig              the stream configuration carrying source details, the proto
     *                                  class name, and the event-timestamp field index
     * @param configuration             the job configuration used to resolve the rowtime attribute name
     * @param stencilClientOrchestrator the Stencil orchestrator supplying Protobuf descriptors
     */
    public ProtoDeserializerProvider(StreamConfig streamConfig, Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator) {
        this.streamConfig = streamConfig;
        this.configuration = configuration;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Constructs a {@link ProtoDeserializer} from the configured proto class, event-timestamp
     * field index, and resolved rowtime attribute name, wired to the Stencil orchestrator.
     *
     * @return a Protobuf-to-{@link Row} deserializer for the configured stream
     */
    @Override
    public DaggerDeserializer<Row> getDaggerDeserializer() {
        int timestampFieldIndex = Integer.parseInt(streamConfig.getEventTimestampFieldIndex());
        String protoClassName = streamConfig.getProtoClass();
        String rowTimeAttributeName = configuration.getString(Constants.FLINK_ROWTIME_ATTRIBUTE_NAME_KEY, Constants.FLINK_ROWTIME_ATTRIBUTE_NAME_DEFAULT);
        return new ProtoDeserializer(protoClassName, timestampFieldIndex, rowTimeAttributeName, stencilClientOrchestrator);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} only when every configured source is a compatible Kafka source and the
     * stream's data type is {@link DataTypes#PROTO}.
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
