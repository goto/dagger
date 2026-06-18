package com.gotocompany.dagger.core.processors.common;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.gotocompany.dagger.core.processors.types.MapDecorator;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.gotocompany.dagger.common.core.Constants.ROWTIME;

/**
 * The Fetch output decorator.
 */
public class FetchOutputDecorator implements MapDecorator {


    /**
     * The names of the output columns produced by the post processing pipeline.
     */
    private String[] outputColumnNames;
    /**
     * The orchestrator used to obtain the Stencil client for resolving the output descriptor.
     */
    private StencilClientOrchestrator stencilClientOrchestrator;
    /**
     * The fully qualified name of the output Protobuf class used to derive output column types.
     */
    private String outputProtoClassName;
    /**
     * Whether a SQL transformer follows this stage, requiring typed output and timestamp conversion.
     */
    private boolean hasSQLTransformer;

    /**
     * Instantiates a new Fetch output decorator.
     *
     * @param schemaConfig      the schema config
     * @param hasSQLTransformer the has sql transformer
     */
    public FetchOutputDecorator(SchemaConfig schemaConfig, boolean hasSQLTransformer) {
        this.outputColumnNames = schemaConfig.getColumnNameManager().getOutputColumnNames();
        this.stencilClientOrchestrator = schemaConfig.getStencilClientOrchestrator();
        this.outputProtoClassName = schemaConfig.getOutputProtoClassName();
        this.hasSQLTransformer = hasSQLTransformer;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This decorator is applied explicitly via {@link #decorate(DataStream)} rather than through
     * the generic decoration chain, so it never opts into automatic decoration.
     *
     * @return {@code false} always
     */
    @Override
    public Boolean canDecorate() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Extracts the output {@link Row} from the wrapped input record. When a SQL transformer
     * follows, any {@code ROWTIME} column is converted from {@link LocalDateTime} to a SQL
     * {@link Timestamp} so the value matches the expected Flink type.
     *
     * @param input the combined input/output record produced upstream
     * @return the extracted output row, with timestamp conversion applied when required
     */
    @Override
    public Row map(Row input) {
        RowManager rowManager = new RowManager(input);
        return hasSQLTransformer ? convertLocalDateTime(rowManager.getOutputData()) : rowManager.getOutputData();
    }


    /**
     * {@inheritDoc}
     *
     * <p>Maps the input stream through this decorator. When a SQL transformer follows, the mapped
     * stream is annotated with explicit {@link TypeInformation} derived from the output descriptor.
     *
     * @param inputStream the stream of combined input/output records
     * @return the stream of extracted output rows
     */
    @Override
    public DataStream<Row> decorate(DataStream<Row> inputStream) {
        return hasSQLTransformer ? inputStream.map(this).returns(getTypeInformation()) : inputStream.map(this);
    }

    /**
     * Derives the Flink row type information for the output columns.
     *
     * <p>Each output column is typed from its matching Protobuf field descriptor when available; the
     * {@code ROWTIME} column maps to a SQL timestamp, and any unresolved column falls back to a
     * generic {@link Object} type.
     *
     * @return the {@link RowTypeInfo} describing the output columns
     */
    private TypeInformation<Row> getTypeInformation() {
        TypeInformation[] typeInformations = new TypeInformation[outputColumnNames.length];
        Arrays.fill(typeInformations, TypeInformation.of(Object.class));
        Descriptors.Descriptor descriptor = getDescriptor();
        if (descriptor != null) {
            for (int index = 0; index < outputColumnNames.length; index++) {
                String outputColumnName = outputColumnNames[index];
                Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByName(outputColumnName);
                typeInformations[index] = fieldDescriptor != null
                        ? TypeHandlerFactory.getTypeHandler(fieldDescriptor).getTypeInformation()
                        : outputColumnName.equals(ROWTIME) ? Types.SQL_TIMESTAMP : TypeInformation.of(Object.class);
            }
        }
        return new RowTypeInfo(typeInformations, outputColumnNames);
    }

    /**
     * Resolves the Protobuf descriptor for the configured output proto class.
     *
     * @return the output message descriptor, or {@code null} when it cannot be resolved
     */
    private Descriptors.Descriptor getDescriptor() {
        return stencilClientOrchestrator.getStencilClient().get(outputProtoClassName);
    }

    /**
     * Copies the given row, converting the {@code ROWTIME} column to a SQL {@link Timestamp}.
     *
     * <p>All fields are copied verbatim except the {@code ROWTIME} column, whose
     * {@link LocalDateTime} value (when present) is converted to a {@link Timestamp} for SQL
     * compatibility.
     *
     * @param row the row whose fields should be copied and timestamp-converted
     * @return a new row with the timestamp column converted
     */
    private Row convertLocalDateTime(Row row) {
        Row outputRow = new Row(row.getArity());
        for (int index = 0; index < outputColumnNames.length; index++) {
            outputRow.setField(index, row.getField(index));
            if (outputColumnNames[index].equals(ROWTIME)) {
                Object timestampValue = outputRow.getField(index);
                if (timestampValue != null) {
                    outputRow.setField(index, Timestamp.valueOf((LocalDateTime) timestampValue));
                }
            }
        }
        return outputRow;
    }

}
