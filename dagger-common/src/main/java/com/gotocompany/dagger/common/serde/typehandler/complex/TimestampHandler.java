package com.gotocompany.dagger.common.serde.typehandler.complex;

import com.google.protobuf.Timestamp;
import com.gotocompany.dagger.common.core.FieldDescriptorCache;
import com.gotocompany.dagger.common.serde.parquet.SimpleGroupValidation;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.RowFactory;
import com.gotocompany.dagger.common.serde.typehandler.TypeInformationFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.types.Row;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

/**
 * The type Timestamp proto handler.
 */
public class TimestampHandler implements TypeHandler {
    /**
     * The number of milliseconds in one second, used to split epoch values into seconds.
     */
    private static final int SECOND_TO_MS_FACTOR = 1000;
    /**
     * The default seconds component used when a timestamp value is absent.
     */
    private static final long DEFAULT_SECONDS_VALUE = 0L;
    /**
     * The default nanoseconds component used when a timestamp value is absent.
     */
    private static final int DEFAULT_NANOS_VALUE = 0;
    /**
     * The number of nanoseconds in one millisecond, used when converting Parquet millis.
     */
    private static final int MS_TO_NANOS_FACTOR = 1000_000;
    /**
     * The UTC date format used to render timestamps as strings for JSON output.
     */
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * The protobuf {@code FieldDescriptor} of the {@code google.protobuf.Timestamp} field handled here.
     */
    private Descriptors.FieldDescriptor fieldDescriptor;

    /**
     * Instantiates a new Timestamp proto handler.
     *
     * @param fieldDescriptor the field descriptor
     */
    public TimestampHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Determines whether this handler applies to the field.
     *
     * @return {@code true} if the field is a {@code google.protobuf.Timestamp} message
     */
    @Override
    public boolean canHandle() {
        return fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE && fieldDescriptor.getMessageType().getFullName().equals("google.protobuf.Timestamp");
    }

    /**
     * Converts a variety of time representations into a protobuf {@code Timestamp} and sets it.
     *
     * <p>Supported inputs include {@code java.sql.Timestamp}, {@code Instant},
     * {@code LocalDateTime}, a two-field {@code Row} of {@code (seconds, nanos)}, an ISO-8601
     * {@code String}, and any {@code Number} of epoch seconds. When the handler cannot apply or
     * {@code field} is {@code null}, the builder is returned unchanged.
     *
     * @param builder the dynamic message builder being populated
     * @param field   the time value to convert and set, or {@code null} to skip
     * @return the same {@code builder}, with the timestamp set when a value could be derived
     * @throws IllegalArgumentException if a {@code Row} input does not have exactly two fields
     */
    @Override
    public DynamicMessage.Builder transformToProtoBuilder(DynamicMessage.Builder builder, Object field) {
        if (!canHandle() || field == null) {
            return builder;
        }
        Timestamp timestamp = null;
        if (field instanceof java.sql.Timestamp) {
            timestamp = convertSqlTimestamp((java.sql.Timestamp) field);
        }

        if (field instanceof Instant) {
            timestamp = Timestamp.newBuilder().setSeconds(((Instant) field).getEpochSecond()).build();
        }

        if (field instanceof LocalDateTime) {
            timestamp = convertLocalDateTime((LocalDateTime) field);
        }

        if (field instanceof Row) {
            Row timeField = (Row) field;
            if (timeField.getArity() == 2) {
                timestamp = Timestamp.newBuilder()
                        .setSeconds((Long) timeField.getField(0))
                        .setNanos((int) timeField.getField(1))
                        .build();
            } else {
                throw new IllegalArgumentException("Row: " + timeField.toString() + " of size: " + timeField.getArity() + " cannot be converted to timestamp");
            }
        }

        if (field instanceof String) {
            timestamp = Timestamp.newBuilder().setSeconds(Instant.parse(((String) field)).getEpochSecond()).build();
        }

        if (field instanceof Number) {
            timestamp = Timestamp.newBuilder().setSeconds(((Number) field).longValue()).build();
        }

        if (timestamp != null) {
            builder.setField(fieldDescriptor, timestamp);
        }
        return builder;
    }

    /**
     * Converts a {@code LocalDateTime} into a protobuf {@code Timestamp} at UTC.
     *
     * @param timeField the local date-time to convert, interpreted as UTC
     * @return the equivalent protobuf timestamp
     */
    private Timestamp convertLocalDateTime(LocalDateTime timeField) {
        return Timestamp.newBuilder()
                .setSeconds(timeField.toEpochSecond(ZoneOffset.UTC))
                .build();
    }

    /**
     * Converts a post-processor value into its ISO-8601 string form when it is a valid instant.
     *
     * @param field the value emitted by an upstream post processor
     * @return the value's string representation, or {@code null} if it is not a valid timestamp
     */
    @Override
    public Object transformFromPostProcessor(Object field) {
        return isValid(field) ? field.toString() : null;
    }

    /**
     * Converts a protobuf {@code Timestamp} message read from the parent into a Flink {@code Row}.
     *
     * @param field the nested {@code DynamicMessage} timestamp read from the parent message
     * @return a row holding the timestamp's {@code seconds} and {@code nanos} fields
     */
    @Override
    public Object transformFromProto(Object field) {
        return RowFactory.createRow((DynamicMessage) field);
    }

    /**
     * Converts the protobuf timestamp into a row using the descriptor cache.
     *
     * @param field the nested {@code DynamicMessage} timestamp read from the parent message
     * @param cache the field descriptor cache used to resolve nested field indices
     * @return a row holding the timestamp's {@code seconds} and {@code nanos} fields
     */
    @Override
    public Object transformFromProtoUsingCache(Object field, FieldDescriptorCache cache) {
        return RowFactory.createRow((DynamicMessage) field, cache);
    }

    /**
     * Reads the timestamp field from a Parquet {@code SimpleGroup} into a {@code (seconds, nanos)} row.
     *
     * <p>Both the {@code INT64} millisecond encoding and the nested group encoding (with
     * {@code seconds} and {@code nanos} fields) are supported; a default zero timestamp is
     * returned when the field is absent.
     *
     * @param simpleGroup the Parquet group holding the encoded record
     * @return a two-field row of seconds and nanos
     */
    @Override
    public Object transformFromParquet(SimpleGroup simpleGroup) {
        String fieldName = fieldDescriptor.getName();
        if (simpleGroup != null && SimpleGroupValidation.checkFieldExistsAndIsInitialized(simpleGroup, fieldName)) {
            Type timestampType = simpleGroup.getType().getType(fieldName);
            if (timestampType instanceof PrimitiveType) {
                return parseInt64TimestampFromSimpleGroup(simpleGroup, fieldName);
            } else if (timestampType instanceof GroupType) {
                return parseGroupTypeTimestampFromSimpleGroup(simpleGroup, fieldName);
            }
        }
        return Row.of(DEFAULT_SECONDS_VALUE, DEFAULT_NANOS_VALUE);
    }

    /**
     * Parses an {@code INT64} millisecond timestamp from a Parquet group into seconds and nanos.
     *
     * @param simpleGroup        the Parquet group containing the timestamp field
     * @param timestampFieldName the name of the timestamp field to read
     * @return a two-field row of seconds and nanos
     */
    private Row parseInt64TimestampFromSimpleGroup(SimpleGroup simpleGroup, String timestampFieldName) {
        /* conversion from ms to nanos borrowed from Instant.java class and inlined here for performance reasons */
        long timeInMillis = simpleGroup.getLong(timestampFieldName, 0);
        long seconds = Math.floorDiv(timeInMillis, SECOND_TO_MS_FACTOR);
        int mos = (int) Math.floorMod(timeInMillis, SECOND_TO_MS_FACTOR);
        int nanos = mos * MS_TO_NANOS_FACTOR;
        return Row.of(seconds, nanos);
    }

    /**
     * Parses a nested-group timestamp (with {@code seconds} and {@code nanos}) from a Parquet group.
     *
     * @param simpleGroup        the Parquet group containing the timestamp field
     * @param timestampFieldName the name of the timestamp group field to read
     * @return a two-field row of seconds and nanos, defaulting to zero for missing components
     */
    private Row parseGroupTypeTimestampFromSimpleGroup(SimpleGroup simpleGroup, String timestampFieldName) {
        SimpleGroup timestampGroup = (SimpleGroup) simpleGroup.getGroup(timestampFieldName, 0);
        long seconds = 0L;
        int nanos = 0;
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(timestampGroup, "seconds")) {
            seconds = timestampGroup.getLong("seconds", 0);
        }
        if (SimpleGroupValidation.checkFieldExistsAndIsInitialized(timestampGroup, "nanos")) {
            nanos = timestampGroup.getInteger("nanos", 0);
        }
        return Row.of(seconds, nanos);
    }

    /**
     * Renders the timestamp row as a UTC date-time string for JSON output.
     *
     * @param field the timestamp {@code Row} of {@code (seconds, nanos)}
     * @return the formatted UTC date-time string, or the original value when it is not a
     *         two-field row
     */
    @Override
    public Object transformToJson(Object field) {
        Row timeField = (Row) field;
        if (timeField.getArity() == 2) {
            java.sql.Timestamp timestamp = new java.sql.Timestamp((Long) timeField.getField(0) * SECOND_TO_MS_FACTOR);
            return dateFormat.format(timestamp);
        } else {
            return field;
        }
    }

    /**
     * Returns the Flink {@code TypeInformation} used to represent this timestamp field.
     *
     * @return the row type derived from the timestamp message descriptor
     */
    @Override
    public TypeInformation getTypeInformation() {
        return TypeInformationFactory.getRowType(fieldDescriptor.getMessageType());
    }

    /**
     * Converts a {@code java.sql.Timestamp} into a protobuf {@code Timestamp}.
     *
     * @param field the SQL timestamp to convert
     * @return the equivalent protobuf timestamp, preserving seconds and nanoseconds
     */
    private Timestamp convertSqlTimestamp(java.sql.Timestamp field) {
        long timestampSeconds = field.getTime() / SECOND_TO_MS_FACTOR;
        int timestampNanos = field.getNanos();
        return Timestamp.newBuilder()
                .setSeconds(timestampSeconds)
                .setNanos(timestampNanos)
                .build();
    }

    /**
     * Checks whether the given value can be parsed as an ISO-8601 instant.
     *
     * @param field the value to validate
     * @return {@code true} if the value is non-null and parses as an {@code Instant}
     */
    private boolean isValid(Object field) {
        if (field == null) {
            return false;
        }
        try {
            Instant.parse(field.toString());
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
