package com.gotocompany.dagger.core.source.flinkkafkaconsumer;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.core.source.config.models.SourceDetails;
import com.gotocompany.dagger.core.source.config.models.SourceName;
import com.gotocompany.dagger.core.source.config.models.SourceType;
import com.gotocompany.dagger.core.source.config.StreamConfig;
import com.gotocompany.dagger.core.source.DaggerSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.types.Row;

import static com.gotocompany.dagger.core.source.config.models.SourceName.KAFKA_CONSUMER;
import static com.gotocompany.dagger.core.source.config.models.SourceType.UNBOUNDED;

/**
 * {@link DaggerSource} implementation backed by Flink's legacy {@code FlinkKafkaConsumer} API.
 *
 * <p>This source is selected for unbounded streams whose {@code SOURCE_DETAILS} declare a single
 * {@link SourceName#KAFKA_CONSUMER} source of type {@link SourceType#UNBOUNDED}. It wraps the
 * configured topic pattern, Kafka properties, and deserializer into a
 * {@link FlinkKafkaConsumerCustom} and registers it on the Flink execution environment. Newer jobs
 * should prefer {@code KafkaDaggerSource} (the {@code KafkaSource}-based implementation); this
 * variant is retained for backwards compatibility.
 */
public class FlinkKafkaConsumerDaggerSource implements DaggerSource<Row> {

    /**
     * Deserializer applied to each Kafka record; must also be a {@code KafkaDeserializationSchema}.
     */
    private final DaggerDeserializer<Row> deserializer;
    /**
     * The per-stream configuration supplying the topic pattern and Kafka properties.
     */
    private final StreamConfig streamConfig;
    /**
     * The global Dagger job configuration used when resolving Kafka properties.
     */
    private final Configuration configuration;
    /**
     * The single source name this implementation supports ({@code KAFKA_CONSUMER}).
     */
    private static final SourceName SUPPORTED_SOURCE_NAME = KAFKA_CONSUMER;
    /**
     * The single source type this implementation supports ({@code UNBOUNDED}).
     */
    private static final SourceType SUPPORTED_SOURCE_TYPE = UNBOUNDED;

    /**
     * Creates a source from the given stream configuration, job configuration, and deserializer.
     *
     * @param streamConfig  the per-stream configuration carrying the topic pattern and Kafka props
     * @param configuration the global Dagger job configuration
     * @param deserializer  the record deserializer; expected to also implement
     *                      {@code KafkaDeserializationSchema}
     */
    public FlinkKafkaConsumerDaggerSource(StreamConfig streamConfig, Configuration configuration, DaggerDeserializer<Row> deserializer) {
        this.streamConfig = streamConfig;
        this.configuration = configuration;
        this.deserializer = deserializer;
    }

    /**
     * Builds the underlying {@link FlinkKafkaConsumerCustom} from the stream configuration.
     *
     * <p>The deserializer is cast to a {@code KafkaDeserializationSchema} and combined with the
     * configured topic pattern and resolved Kafka properties.
     *
     * @return a configured {@link FlinkKafkaConsumerCustom} ready to be added to the environment
     */
    FlinkKafkaConsumerCustom buildSource() {
        KafkaDeserializationSchema kafkaDeserializationSchema = (KafkaDeserializationSchema<Row>) deserializer;
        return new FlinkKafkaConsumerCustom(streamConfig.getTopicPattern(),
                kafkaDeserializationSchema, streamConfig.getKafkaProps(configuration), configuration);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds the {@link FlinkKafkaConsumerCustom}, applies the watermark strategy to it, and adds
     * it to the execution environment via {@code addSource}.
     */
    @Override
    public DataStream<Row> register(StreamExecutionEnvironment executionEnvironment, WatermarkStrategy<Row> watermarkStrategy) {
        FlinkKafkaConsumerCustom source = buildSource();
        return executionEnvironment.addSource(source.assignTimestampsAndWatermarks(watermarkStrategy));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} only when exactly one {@code SOURCE_DETAILS} entry is configured with
     * source name {@link SourceName#KAFKA_CONSUMER} and type {@link SourceType#UNBOUNDED}, and the
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
