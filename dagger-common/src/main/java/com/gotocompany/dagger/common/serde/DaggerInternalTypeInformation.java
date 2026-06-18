package com.gotocompany.dagger.common.serde;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.core.Constants;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Contract for source schema adapters that expose the Flink {@link Row} type produced by a Dagger
 * input stream and that append Dagger's internal trailing columns to it.
 *
 * <p>Implementations such as {@code ProtoType} and {@code JsonType} translate an external schema
 * (a protobuf descriptor or a JSON schema string) into a Flink {@code TypeInformation<Row>}. On top
 * of the user-visible columns, every row emitted by a Dagger deserializer carries two extra
 * trailing fields used internally by the pipeline; {@link #addInternalFields} centralizes how
 * those columns are added so all input formats stay consistent.
 */
public interface DaggerInternalTypeInformation {

    /**
     * Builds the complete Flink row type for this stream, including Dagger's internal trailing
     * columns.
     *
     * @return the {@code TypeInformation<Row>} describing the {@link Row} emitted by the
     *         corresponding deserializer, with the validation flag and rowtime columns appended
     */
    TypeInformation<Row> getRowType();

    /**
     * Appends Dagger's two internal trailing columns to an existing row type.
     *
     * <p>The supplied {@code initialTypeInfo} (which is expected to be a {@link RowTypeInfo}
     * describing the user-facing columns) is extended with two fields, in this exact order: a
     * boolean validation flag stored under {@link Constants#INTERNAL_VALIDATION_FIELD_KEY} that
     * records whether the source record was parsed successfully, followed by an event-time column
     * named {@code rowtimeAttributeName} of type {@code SQL_TIMESTAMP} that Flink uses as the
     * rowtime attribute for watermarking and windowing.
     *
     * @param initialTypeInfo      the row type describing the user-visible columns; cast to
     *                             {@link RowTypeInfo} to read its field names and types
     * @param rowtimeAttributeName the name to assign to the appended event-time (rowtime) column
     * @return a new named {@code TypeInformation<Row>} containing the original fields followed by
     *         the boolean validation flag and the rowtime timestamp column
     */
    default TypeInformation<Row> addInternalFields(TypeInformation<Row> initialTypeInfo, String rowtimeAttributeName) {
        RowTypeInfo rowTypeInfo = (RowTypeInfo) initialTypeInfo;
        ArrayList<String> fieldNames = new ArrayList<>(Arrays.asList(rowTypeInfo.getFieldNames()));
        ArrayList<TypeInformation> fieldTypes = new ArrayList<>(Arrays.asList(rowTypeInfo.getFieldTypes()));
        fieldNames.add(Constants.INTERNAL_VALIDATION_FIELD_KEY);
        fieldTypes.add(Types.BOOLEAN);
        fieldNames.add(rowtimeAttributeName);
        fieldTypes.add(Types.SQL_TIMESTAMP);
        return Types.ROW_NAMED(fieldNames.toArray(new String[0]), fieldTypes.toArray(new TypeInformation[0]));
    }
}
