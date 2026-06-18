package com.gotocompany.dagger.core.source.kafka;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.core.source.DaggerSource;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import com.gotocompany.dagger.core.source.config.models.SourceDetails;
import com.gotocompany.dagger.core.source.config.models.SourceName;
import com.gotocompany.dagger.core.source.config.models.SourceType;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.types.Row;

/**
 * {@link DaggerSource} implementation backed by Flink's modern {@link KafkaSource} connector
 * (the FLIP-27 source API).
 *
 * <p>This is the preferred Kafka source for unbounded Dagger streams. It is selected when the
 * configured {@code SOURCE_DETAILS} declare a single {@link SourceName#KAFKA_SOURCE} of type
 * {@link SourceType#UNBOUNDED}. The configured topic pattern, starting offsets, Kafka properties,
 * and deserializer are assembled into a {@link KafkaSource} that is then attached to the execution
 * environment via {@code fromSource}.
 */
public class KafkaDaggerSource implements DaggerSource<Row> {
    /**
     * Deserializer applied to each Kafka record; must also be a {@code KafkaDeserializationSchema}.
     */
    private final DaggerDeserializer<Row> deserializer;
    /**
     * The per-stream configuration supplying the topic pattern, starting offsets, and Kafka props.
     */
    private final StreamConfig streamConfig;
    /**
     * The global Dagger job configuration used when resolving Kafka properties.
     */
    private final Configuration configuration;
    /**
     * The single source name this implementation supports ({@code KAFKA_SOURCE}).
     */
    private static final SourceName SUPPORTED_SOURCE_NAME = SourceName.KAFKA_SOURCE;
    /**
     * The single source type this implementation supports ({@code UNBOUNDED}).
     */
    private static final SourceType SUPPORTED_SOURCE_TYPE = SourceType.UNBOUNDED;

    /**
     * Creates a source from the given stream configuration, job configuration, and deserializer.
     *
     * @param streamConfig  the per-stream configuration carrying the topic pattern, offsets and props
     * @param configuration the global Dagger job configuration
     * @param deserializer  the record deserializer; expected to also implement
     *                      {@code KafkaDeserializationSchema}
     */
    public KafkaDaggerSource(StreamConfig streamConfig, Configuration configuration, DaggerDeserializer<Row> deserializer) {
        this.streamConfig = streamConfig;
        this.configuration = configuration;
        this.deserializer = deserializer;
    }

    /**
     * Builds the underlying Flink {@link KafkaSource} from the stream configuration.
     *
     * <p>The deserializer is adapted to a {@code KafkaRecordDeserializationSchema} and the source is
     * configured with the topic pattern, starting offsets, and Kafka client properties.
     *
     * @return a configured {@link KafkaSource} of {@code Row}
     */
    KafkaSource<Row> buildSource() {
        KafkaRecordDeserializationSchema<Row> kafkaRecordDeserializationSchema = KafkaRecordDeserializationSchema
                .of((KafkaDeserializationSchema<Row>) deserializer);
        return KafkaSource.<Row>builder()
                .setTopicPattern(streamConfig.getTopicPattern())
                .setStartingOffsets(streamConfig.getStartingOffset())
                .setProperties(streamConfig.getKafkaProps(configuration))
                .setDeserializer(kafkaRecordDeserializationSchema)
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Registers the built {@link KafkaSource} on the environment via {@code fromSource}, using the
     * supplied watermark strategy and the stream's schema table as the source name.
     */
    @Override
    public DataStream<Row> register(StreamExecutionEnvironment executionEnvironment, WatermarkStrategy<Row> watermarkStrategy) {
        return executionEnvironment.fromSource(buildSource(), watermarkStrategy, streamConfig.getSchemaTable());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} only when exactly one {@code SOURCE_DETAILS} entry is configured with
     * source name {@link SourceName#KAFKA_SOURCE} and type {@link SourceType#UNBOUNDED}, and the
     * deserializer is a {@code KafkaDeserializationSchema}.
     */
    @Override
    public boolean canBuild() {
        SourceDetails[] sourceDetailsArray = streamConfig.getSourceDetails();
        if (sourceDetailsArray.length != 1) {
            return false;
        } else {
            SourceName sourceName = sourceDetailsArray[0].getSourceName();
            SourceType sourceType = sourceDetailsArray[0].getSourceType();
            return sourceName.equals(SUPPORTED_SOURCE_NAME) && sourceType.equals(SUPPORTED_SOURCE_TYPE)
                    && deserializer instanceof KafkaDeserializationSchema;
        }
    }
}
