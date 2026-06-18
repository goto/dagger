package com.gotocompany.dagger.core.processors.longbow.request;

import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.processors.longbow.storage.PutRequest;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static com.gotocompany.dagger.common.core.Constants.ROWTIME;

/**
 * Create PutRequest in form of proto byte.
 */
public class ProtoBytePutRequest implements PutRequest {
    /**
     * Default BigTable column family, in bytes, under which the serialized record is stored.
     */
    private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT);
    /**
     * Default BigTable column qualifier, in bytes, holding the serialized proto value.
     */
    private static final byte[] QUALIFIER_NAME = Bytes.toBytes(Constants.LONGBOW_QUALIFIER_DEFAULT);
    /**
     * Schema describing the Longbow key layout used to build the BigTable row key.
     */
    private final LongbowSchema longbowSchema;
    /**
     * Input row to be serialized and written to BigTable.
     */
    private final Row input;
    /**
     * Serializer that converts the input row into its Protobuf byte representation.
     */
    private final ProtoSerializer protoSerializer;
    /**
     * Identifier of the BigTable table this put targets.
     */
    private final String tableId;


    /**
     * Instantiates a new Proto byte put request.
     *
     * @param longbowSchema   the longbow schema
     * @param input           the input
     * @param protoSerializer the proto serializer
     * @param tableId         the table id
     */
    public ProtoBytePutRequest(LongbowSchema longbowSchema, Row input, ProtoSerializer protoSerializer, String tableId) {
        this.longbowSchema = longbowSchema;
        this.input = input;
        this.protoSerializer = protoSerializer;
        this.tableId = tableId;
    }

    /**
     * Builds the BigTable {@link Put} for the input row in Longbow-plus (proto byte) form.
     *
     * <p>The row key is derived from the Longbow key, and a single cell is written under the default
     * column family and qualifier with the row time as its timestamp and the serialized Protobuf bytes
     * as its value.
     *
     * @return the assembled {@link Put} request
     */
    @Override
    public Put get() {
        Put putRequest = new Put(longbowSchema.getKey(input, 0));
        Timestamp rowtime = convertToTimeStamp(longbowSchema.getValue(input, ROWTIME));
        putRequest.addColumn(COLUMN_FAMILY_NAME, QUALIFIER_NAME, rowtime.getTime(), protoSerializer.serializeValue(input));
        return putRequest;
    }

    /**
     * {@inheritDoc}
     *
     * @return the identifier of the target BigTable table
     */
    @Override
    public String getTableId() {
        return this.tableId;
    }

    /**
     * Normalises a row-time field into a {@link Timestamp}.
     *
     * @param timeStampField the row-time value, either a {@link LocalDateTime} or a {@link Timestamp}
     * @return the equivalent {@link Timestamp}
     */
    private Timestamp convertToTimeStamp(Object timeStampField) {
        if (timeStampField instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) timeStampField);
        }
        return (Timestamp) timeStampField;
    }
}
