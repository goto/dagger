package com.gotocompany.dagger.core.deserializer;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.common.serde.DataTypes;
import com.gotocompany.dagger.common.serde.parquet.deserialization.SimpleGroupDeserializer;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import com.gotocompany.dagger.core.source.config.models.SourceDetails;
import com.gotocompany.dagger.core.source.config.models.SourceName;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.types.Row;

import static com.gotocompany.dagger.common.serde.DataTypes.PROTO;

/**
 * Supplies a {@link SimpleGroupDeserializer} for Parquet-backed streams whose records follow a
 * Protobuf schema.
 *
 * <p>This provider accepts a stream only when every configured source is a
 * {@link SourceName#PARQUET_SOURCE} and the input schema type is {@link DataTypes#PROTO}. The
 * resulting deserializer converts Parquet {@code SimpleGroup} rows into Flink {@link Row} records
 * using the configured proto class and the Stencil schema registry.
 */
public class SimpleGroupDeserializerProvider implements DaggerDeserializerProvider<Row> {
    /** Stream configuration describing the source(s), proto class, and event-timestamp field. */
    protected final StreamConfig streamConfig;
    /** Job configuration, used here to resolve the Flink rowtime attribute name. */
    protected final Configuration configuration;
    /** Orchestrator providing Stencil descriptors for interpreting the Parquet rows. */
    protected final StencilClientOrchestrator stencilClientOrchestrator;
    /** The single source type this provider handles, namely the Parquet source. */
    private static final SourceName COMPATIBLE_SOURCE = SourceName.PARQUET_SOURCE;
    /** Input schema type this provider handles, namely Protobuf. */
    private static final DataTypes COMPATIBLE_INPUT_SCHEMA_TYPE = PROTO;

    /**
     * Creates a Parquet {@code SimpleGroup} deserializer provider for the given stream.
     *
     * @param streamConfig              the stream configuration carrying source details, the proto
     *                                  class name, and the event-timestamp field index
     * @param configuration             the job configuration used to resolve the rowtime attribute name
     * @param stencilClientOrchestrator the Stencil orchestrator supplying Protobuf descriptors
     */
    public SimpleGroupDeserializerProvider(StreamConfig streamConfig, Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator) {
        this.streamConfig = streamConfig;
        this.configuration = configuration;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Constructs a {@link SimpleGroupDeserializer} from the configured proto class,
     * event-timestamp field index, and resolved rowtime attribute name, wired to the Stencil
     * orchestrator.
     *
     * @return a Parquet-{@code SimpleGroup}-to-{@link Row} deserializer for the configured stream
     */
    @Override
    public DaggerDeserializer<Row> getDaggerDeserializer() {
        int timestampFieldIndex = Integer.parseInt(streamConfig.getEventTimestampFieldIndex());
        String protoClassName = streamConfig.getProtoClass();
        String rowTimeAttributeName = configuration.getString(Constants.FLINK_ROWTIME_ATTRIBUTE_NAME_KEY, Constants.FLINK_ROWTIME_ATTRIBUTE_NAME_DEFAULT);
        return new SimpleGroupDeserializer(protoClassName, timestampFieldIndex, rowTimeAttributeName, stencilClientOrchestrator);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} only when every configured source is the
     * {@link SourceName#PARQUET_SOURCE} and the stream's data type is {@link DataTypes#PROTO}.
     *
     * @return {@code true} if this provider can deserialize the configured stream, {@code false} otherwise
     */
    @Override
    public boolean canProvide() {
        SourceDetails[] sourceDetailsList = streamConfig.getSourceDetails();
        for (SourceDetails sourceDetails : sourceDetailsList) {
            SourceName sourceName = sourceDetails.getSourceName();
            DataTypes inputSchemaType = DataTypes.valueOf(streamConfig.getDataType());
            if (!sourceName.equals(COMPATIBLE_SOURCE) || !inputSchemaType.equals(COMPATIBLE_INPUT_SCHEMA_TYPE)) {
                return false;
            }
        }
        return true;
    }
}
