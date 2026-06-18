package com.gotocompany.dagger.common.serde.proto.deserialization;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.exceptions.DescriptorNotFoundException;
import com.gotocompany.dagger.common.exceptions.serde.DaggerDeserializationException;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import com.gotocompany.dagger.common.serde.typehandler.RowFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.types.Row;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Deserializer for protobuf messages.
 */
public class ProtoDeserializer implements KafkaDeserializationSchema<Row>, DaggerDeserializer<Row> {

    /**
     * Logger used to warn about null payloads and invalid protobuf records.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoDeserializer.class);
    /**
     * The fully-qualified protobuf class name used to resolve the message descriptor.
     */
    private final String protoClassName;
    /**
     * The field number of the protobuf timestamp field appended as the rowtime column.
     */
    private final int timestampFieldIndex;
    /**
     * The orchestrator used to obtain the Stencil client that resolves proto descriptors.
     */
    private final StencilClientOrchestrator stencilClientOrchestrator;
    /**
     * The Flink {@link TypeInformation} describing the {@link Row} produced by this deserializer.
     */
    private final TypeInformation<Row> typeInformation;
    /**
     * Cache of field descriptors used to build rows efficiently when schema auto-refresh is on.
     */
    private final FieldDescriptorCache fieldDescriptorCache;
    /**
     * Whether the Stencil cache auto-refresh is enabled, which selects the descriptor-cache row path.
     */
    private final boolean stencilAutoRefreshEnable;

    /**
     * Instantiates a new Proto deserializer.
     *
     * @param protoClassName            the proto class name
     * @param timestampFieldIndex       the timestamp field index
     * @param rowtimeAttributeName      the rowtime attribute name
     * @param stencilClientOrchestrator the stencil client orchestrator
     */
    public ProtoDeserializer(String protoClassName, int timestampFieldIndex, String rowtimeAttributeName, StencilClientOrchestrator stencilClientOrchestrator) {
        this.protoClassName = protoClassName;
        this.timestampFieldIndex = timestampFieldIndex;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.typeInformation = new ProtoType(protoClassName, rowtimeAttributeName, stencilClientOrchestrator).getRowType();
        this.fieldDescriptorCache = new FieldDescriptorCache(getProtoParser());
        this.stencilAutoRefreshEnable = stencilClientOrchestrator.createStencilConfig().getCacheAutoRefresh();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This stream is unbounded, so the implementation always reports that the end of stream
     * has not been reached.
     *
     * @param nextElement the most recently deserialized row
     * @return {@code false} always, since the Kafka source is treated as never-ending
     */
    @Override
    public boolean isEndOfStream(Row nextElement) {
        return false;
    }

    /**
     * Deserializes a Kafka record into a Flink {@link Row}.
     *
     * <p>A {@code null} payload, or a record that fails protobuf parsing, yields a default
     * "invalid" row (flagged as invalid with a zero timestamp) rather than failing the job;
     * a successfully parsed message is converted and augmented with its rowtime timestamp.
     *
     * @param consumerRecord the Kafka record whose key and value byte arrays are read
     * @return the deserialized row, or a default invalid row when the value is {@code null}
     *         or cannot be parsed as the expected protobuf message
     * @throws DescriptorNotFoundException if the proto descriptor cannot be resolved
     * @throws DaggerDeserializationException if an unexpected runtime error occurs while parsing
     */
    @Override
    public Row deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) {
        Descriptors.Descriptor descriptor = getProtoParser();
        byte[] value = consumerRecord.value();
        if (value == null) {
            LOGGER.warn("Record value / byteArray is NULL! " + protoClassName);
            return createDefaultInvalidRow(DynamicMessage.getDefaultInstance(descriptor));
        }
        try {
            DynamicMessage proto = DynamicMessage.parseFrom(descriptor, value);
            return addTimestampFieldToRow(proto);
        } catch (DescriptorNotFoundException e) {
            throw new DescriptorNotFoundException(e);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.warn("Invalid Row encountered for proto " + protoClassName, e);
            return createDefaultInvalidRow(DynamicMessage.getDefaultInstance(descriptor));
        } catch (RuntimeException e) {
            throw new DaggerDeserializationException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return the {@link TypeInformation} of the {@link Row} this deserializer produces
     */
    @Override
    public TypeInformation<Row> getProducedType() {
        return this.typeInformation;
    }

    /**
     * Resolves the protobuf message {@link Descriptors.Descriptor} for {@code protoClassName}
     * from the Stencil client.
     *
     * @return the descriptor for the configured proto class
     * @throws DescriptorNotFoundException if no descriptor is registered for {@code protoClassName}
     */
    private Descriptors.Descriptor getProtoParser() {
        Descriptors.Descriptor dsc = stencilClientOrchestrator.getStencilClient().get(protoClassName);
        if (dsc == null) {
            throw new DescriptorNotFoundException();
        }
        return dsc;
    }

    /**
     * Builds a placeholder {@link Row} for records that cannot be deserialized.
     *
     * <p>The row is created from the proto default instance with two extra trailing columns,
     * the validity flag set to {@code false} and the rowtime set to epoch zero.
     *
     * @param defaultInstance the default protobuf message instance used to shape the row
     * @return a row flagged as invalid with a zero timestamp
     */
    private Row createDefaultInvalidRow(DynamicMessage defaultInstance) {
        Row row;
        if (stencilAutoRefreshEnable) {
            row = RowFactory.createRow(defaultInstance, 2, fieldDescriptorCache);
        } else {
            row = RowFactory.createRow(defaultInstance, 2);
        }
        row.setField(row.getArity() - 2, false);
        row.setField(row.getArity() - 1, new Timestamp(0));
        return row;
    }

    /**
     * Converts a parsed protobuf message into a {@link Row} and appends rowtime metadata.
     *
     * <p>Two trailing columns are added: a validity flag set to {@code true} and a
     * {@link Timestamp} derived from the seconds and nanos of the configured timestamp field.
     *
     * @param proto the successfully parsed protobuf message
     * @return the row representation including the validity flag and event-time timestamp
     */
    private Row addTimestampFieldToRow(DynamicMessage proto) {
        Row finalRecord;
        if (stencilAutoRefreshEnable) {
            finalRecord = RowFactory.createRow(proto, 2, fieldDescriptorCache);
        } else {
            finalRecord = RowFactory.createRow(proto, 2);
        }

        Descriptors.FieldDescriptor fieldDescriptor = proto.getDescriptorForType().findFieldByNumber(timestampFieldIndex);
        DynamicMessage timestampProto = (DynamicMessage) proto.getField(fieldDescriptor);
        List<Descriptors.FieldDescriptor> timestampFields = timestampProto.getDescriptorForType().getFields();

        long timestampSeconds = (long) timestampProto.getField(timestampFields.get(0));
        long timestampNanos = (int) timestampProto.getField(timestampFields.get(1));

        finalRecord.setField(finalRecord.getArity() - 2, true);
        finalRecord.setField(finalRecord.getArity() - 1, Timestamp.from(Instant.ofEpochSecond(timestampSeconds, timestampNanos)));
        return finalRecord;
    }


}
