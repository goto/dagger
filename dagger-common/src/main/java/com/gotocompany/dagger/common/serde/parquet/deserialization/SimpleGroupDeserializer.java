package com.gotocompany.dagger.common.serde.parquet.deserialization;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.common.exceptions.DescriptorNotFoundException;
import com.gotocompany.dagger.common.exceptions.serde.DaggerDeserializationException;
import com.gotocompany.dagger.common.serde.proto.deserialization.ProtoType;
import com.gotocompany.dagger.common.serde.typehandler.RowFactory;
import com.gotocompany.dagger.common.serde.typehandler.complex.TimestampHandler;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.types.Row;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Deserializes Parquet records (read as {@link SimpleGroup} instances) into Flink {@link Row}
 * objects for a Dagger Parquet source.
 *
 * <p>Dagger's Parquet files are written from protobuf schemas, so the row layout is derived from a
 * protobuf descriptor resolved by class name through the {@link StencilClientOrchestrator}. Each
 * {@link SimpleGroup} is mapped column-by-column via {@link RowFactory}, leaving two trailing slots
 * for Dagger's internal columns, which are then filled with a boolean validation flag and an
 * event-time (rowtime) {@link Timestamp} extracted from the configured timestamp field.
 */
public class SimpleGroupDeserializer implements DaggerDeserializer<Row> {
    /** Fully-qualified protobuf class name whose descriptor defines the Parquet record schema. */
    private final String protoClassName;
    /** Proto field number of the timestamp field used to derive the event-time (rowtime). */
    private final int timestampFieldIndex;
    /** Resolves protobuf descriptors from the Stencil schema registry. */
    private final StencilClientOrchestrator stencilClientOrchestrator;
    /** Produced {@link Row} type, including Dagger's internal validation and rowtime columns. */
    private final TypeInformation<Row> typeInformation;

    /**
     * Instantiates a new Parquet {@link SimpleGroup} deserializer.
     *
     * <p>Eagerly builds the produced row type from the protobuf schema via {@link ProtoType} so
     * Flink can query it; descriptor resolution for actual deserialization happens lazily when
     * {@link #deserialize(SimpleGroup)} is invoked.
     *
     * @param protoClassName            the fully-qualified protobuf class name describing the schema
     * @param timestampFieldIndex       the proto field number of the timestamp field used for rowtime
     * @param rowtimeAttributeName      the name to assign to the appended event-time (rowtime) column
     * @param stencilClientOrchestrator the orchestrator used to resolve protobuf descriptors
     */
    public SimpleGroupDeserializer(String protoClassName, int timestampFieldIndex, String rowtimeAttributeName, StencilClientOrchestrator stencilClientOrchestrator) {
        this.protoClassName = protoClassName;
        this.timestampFieldIndex = timestampFieldIndex;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.typeInformation = new ProtoType(protoClassName, rowtimeAttributeName, stencilClientOrchestrator).getRowType();
    }

    /**
     * Resolves the protobuf descriptor for {@link #protoClassName} from the Stencil registry.
     *
     * @return the descriptor describing the Parquet record schema
     * @throws DescriptorNotFoundException if no descriptor is registered for the configured class
     *                                     name
     */
    private Descriptors.Descriptor getProtoParser() {
        Descriptors.Descriptor dsc = stencilClientOrchestrator.getStencilClient().get(protoClassName);
        if (dsc == null) {
            throw new DescriptorNotFoundException();
        }
        return dsc;
    }

    /**
     * Converts a single Parquet {@link SimpleGroup} into a Flink {@link Row}.
     *
     * <p>The row is built from the resolved descriptor with two extra trailing slots reserved for
     * Dagger's internal columns, which are subsequently populated by
     * {@link #addTimestampFieldToRow(Row, SimpleGroup, Descriptors.Descriptor)}.
     *
     * @param simpleGroup the Parquet record to convert
     * @return the resulting row with its validation flag and rowtime timestamp populated
     * @throws DaggerDeserializationException if conversion of the record fails
     */
    public Row deserialize(SimpleGroup simpleGroup) {
        Descriptors.Descriptor descriptor = getProtoParser();
        try {
            Row row = RowFactory.createRow(descriptor, simpleGroup, 2);
            return addTimestampFieldToRow(row, simpleGroup, descriptor);
        } catch (RuntimeException e) {
            throw new DaggerDeserializationException(e);
        }
    }

    /**
     * Populates Dagger's internal trailing columns on an already-mapped Parquet row.
     *
     * <p>Reads the configured timestamp field (located by {@link #timestampFieldIndex}) using a
     * {@link TimestampHandler}, which yields a two-element row of epoch seconds and nanoseconds. The
     * second-to-last column (the validation flag) is set to {@code true} and the last column is set
     * to the corresponding {@link Timestamp} built via {@link Instant#ofEpochSecond(long, long)}.
     *
     * @param row         the row already populated with the user-visible columns
     * @param simpleGroup the source Parquet record holding the timestamp field
     * @param descriptor  the protobuf descriptor used to locate the timestamp field by number
     * @return the same row instance, with its validation flag and rowtime column populated
     */
    private Row addTimestampFieldToRow(Row row, SimpleGroup simpleGroup, Descriptors.Descriptor descriptor) {
        Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByNumber(timestampFieldIndex);
        TimestampHandler timestampHandler = new TimestampHandler(fieldDescriptor);
        Row timestampRow = (Row) timestampHandler.transformFromParquet(simpleGroup);
        long seconds = timestampRow.getFieldAs(0);
        int nanos = timestampRow.getFieldAs(1);

        row.setField(row.getArity() - 2, true);
        row.setField(row.getArity() - 1, Timestamp.from(Instant.ofEpochSecond(seconds, nanos)));
        return row;
    }

    /**
     * {@inheritDoc}
     *
     * @return the produced {@link Row} {@code TypeInformation}, including Dagger's internal trailing
     *         columns
     */
    @Override
    public TypeInformation<Row> getProducedType() {
        return this.typeInformation;
    }
}
