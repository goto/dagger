package com.gotocompany.dagger.common.serde.json.deserialization;

import com.gotocompany.dagger.common.exceptions.serde.DaggerDeserializationException;
import com.gotocompany.dagger.common.serde.DaggerDeserializer;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.formats.json.JsonRowDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.types.Row;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import static com.gotocompany.dagger.common.core.Constants.ROWTIME;

/**
 * Kafka deserialization schema that converts JSON-encoded record values into Flink {@link Row}
 * instances for a Dagger input stream.
 *
 * <p>Parsing is delegated to Flink's {@link JsonRowDeserializationSchema}, which is built from the
 * row type produced by {@link JsonType} (the user-visible columns plus Dagger's internal trailing
 * columns). After parsing, each row is post-processed so that the trailing validation flag is set
 * and the rowtime column is populated with a {@link Timestamp} derived from the configured source
 * field. Any parsing or conversion failure is surfaced as a {@link DaggerDeserializationException}
 * so that Dagger sources can handle errors uniformly.
 */
public class JsonDeserializer implements KafkaDeserializationSchema<Row>, DaggerDeserializer<Row> {
    /** Backing Flink schema that parses JSON bytes into a {@link Row} matching {@link #typeInformation}. */
    private final JsonRowDeserializationSchema jsonRowDeserializationSchema;
    /** Index, within the produced row, of the field whose value supplies the event-time (rowtime). */
    private final int rowtimeIdx;
    /** Full produced {@link Row} type, including Dagger's internal validation and rowtime columns. */
    private final TypeInformation<Row> typeInformation;

    /**
     * Instantiates a new JSON deserializer for a Dagger source.
     *
     * <p>Builds the produced row type from the JSON schema (appending Dagger's internal columns
     * under the {@code ROWTIME} attribute name), constructs the backing Flink JSON schema from that
     * type, and resolves the index of the field that carries the event time.
     *
     * @param jsonSchema       the JSON schema string describing the stream's records
     * @param rowtimeFieldName the name of the field whose value is used as the event-time (rowtime)
     */
    public JsonDeserializer(String jsonSchema, String rowtimeFieldName) {
        this.typeInformation = new JsonType(jsonSchema, ROWTIME).getRowType();
        this.jsonRowDeserializationSchema = new JsonRowDeserializationSchema.Builder(typeInformation).build();
        RowTypeInfo rowTypeInfo = (RowTypeInfo) typeInformation;
        this.rowtimeIdx = rowTypeInfo.getFieldIndex(rowtimeFieldName);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Dagger streams are unbounded, so this always returns {@code false}; no element is ever
     * treated as an end-of-stream marker.
     *
     * @param nextElement the most recently deserialized row (ignored)
     * @return {@code false} always
     */
    @Override
    public boolean isEndOfStream(Row nextElement) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Parses the Kafka record's value bytes as JSON into a {@link Row} using the backing
     * {@link JsonRowDeserializationSchema}, then populates Dagger's internal validation and rowtime
     * columns via {@link #addTimestampFieldToRow(Row)}.
     *
     * @param consumerRecord the Kafka record whose value holds the JSON payload
     * @return the deserialized row with its validation flag set and rowtime column populated
     * @throws DaggerDeserializationException if the payload cannot be parsed, or the rowtime field
     *                                        carries an unsupported type
     */
    @Override
    public Row deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) {
        try {
            Row inputRow = jsonRowDeserializationSchema.deserialize(consumerRecord.value());
            return addTimestampFieldToRow(inputRow);
        } catch (RuntimeException | IOException e) {
            throw new DaggerDeserializationException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return the {@link Row} {@code TypeInformation} produced by the backing JSON schema, that is
     *         the user columns followed by Dagger's internal trailing columns
     */
    @Override
    public TypeInformation<Row> getProducedType() {
        return jsonRowDeserializationSchema.getProducedType();
    }

    /**
     * Copies the user-visible columns of a freshly parsed row and fills in Dagger's internal
     * trailing columns.
     *
     * <p>All fields except the final two are copied verbatim into a new {@link Row} of the same
     * arity. The event-time value is read from {@link #rowtimeIdx}: a {@link BigDecimal} is
     * interpreted as epoch seconds and converted with {@link Instant#ofEpochSecond(long)}, while an
     * existing {@link Timestamp} is used as-is. The last column is then set to that timestamp and
     * the second-to-last column (the validation flag) is set to {@code true}.
     *
     * @param row the row produced by the backing JSON schema
     * @return a new row holding the user columns plus the populated validation flag and rowtime
     *         timestamp
     * @throws DaggerDeserializationException if the rowtime field is neither a {@link BigDecimal}
     *                                        nor a {@link Timestamp}
     */
    private Row addTimestampFieldToRow(Row row) {
        Row finalRecord = new Row(row.getArity());

        for (int i = 0; i < row.getArity() - 2; i++) {
            finalRecord.setField(i, row.getField(i));
        }

        Object rowtimeField = row.getField(rowtimeIdx);
        if (rowtimeField instanceof BigDecimal) {
            BigDecimal bigDecimalField = (BigDecimal) row.getField(rowtimeIdx);
            finalRecord.setField(finalRecord.getArity() - 1, Timestamp.from(Instant.ofEpochSecond(bigDecimalField.longValue())));
        } else if (rowtimeField instanceof Timestamp) {
            finalRecord.setField(finalRecord.getArity() - 1, rowtimeField);
        } else {
            throw new DaggerDeserializationException("Invalid Rowtime datatype for rowtimeField");
        }
        finalRecord.setField(finalRecord.getArity() - 2, true);

        return finalRecord;
    }
}
