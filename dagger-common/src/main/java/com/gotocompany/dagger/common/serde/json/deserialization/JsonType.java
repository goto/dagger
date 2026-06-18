package com.gotocompany.dagger.common.serde.json.deserialization;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.formats.json.JsonRowSchemaConverter;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.serde.DaggerInternalTypeInformation;

import java.io.Serializable;

/**
 * Derives the Flink {@link Row} {@code TypeInformation} for a JSON-encoded input stream from its
 * JSON schema.
 *
 * <p>The configured JSON schema string is converted into a named row type using Flink's
 * {@link JsonRowSchemaConverter}, after which Dagger's internal trailing columns (the boolean
 * validation flag and the rowtime timestamp) are appended through {@link #addInternalFields}.
 * Instances are {@link Serializable} so the schema description can travel with the serialized job
 * graph; the resulting type is consumed when constructing the JSON deserializer for a Kafka source.
 */
public class JsonType implements Serializable, DaggerInternalTypeInformation {
    /** JSON schema string (JSON-Schema syntax) describing the user-visible columns of the stream. */
    private String jsonSchema;
    /** Name to assign to the appended event-time (rowtime) column. */
    private String rowtimeAttributeName;

    /**
     * Instantiates a new JSON type descriptor for a stream.
     *
     * @param jsonSchema           the JSON schema string describing the stream's records
     * @param rowtimeAttributeName the name of the event-time (rowtime) column to append
     */
    public JsonType(String jsonSchema, String rowtimeAttributeName) {
        this.jsonSchema = jsonSchema;
        this.rowtimeAttributeName = rowtimeAttributeName;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Converts the configured JSON schema into a named row type via
     * {@link JsonRowSchemaConverter#convert(String)} and then appends Dagger's internal validation
     * and rowtime columns.
     *
     * @return the {@code TypeInformation<Row>} for the JSON stream, including the internal trailing
     *         columns
     */
    public TypeInformation<Row> getRowType() {
        TypeInformation<Row> rowNamed = JsonRowSchemaConverter.convert(jsonSchema);

        return addInternalFields(rowNamed, rowtimeAttributeName);
    }
}
