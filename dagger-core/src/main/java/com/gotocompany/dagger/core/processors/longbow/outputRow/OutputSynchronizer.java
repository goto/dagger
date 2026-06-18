package com.gotocompany.dagger.core.processors.longbow.outputRow;

import com.gotocompany.dagger.core.processors.longbow.validator.LongbowType;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;

import org.apache.flink.types.Row;

import java.util.stream.IntStream;

/**
 * The Output synchronizer.
 */
public class OutputSynchronizer implements WriterOutputRow {
    /**
     * Schema describing the Longbow columns and key layout for the current job.
     */
    private LongbowSchema longbowSchema;
    /**
     * Identifier of the BigTable table that received the written record.
     */
    private String tableId;
    /**
     * Fully qualified name of the input Protobuf message class.
     */
    private String inputProto;

    /**
     * Instantiates a new Output synchronizer.
     *
     * @param longbowSchema the longbow schema
     * @param tableId       the table id
     * @param inputProto    the input proto
     */
    public OutputSynchronizer(LongbowSchema longbowSchema, String tableId, String inputProto) {
        this.longbowSchema = longbowSchema;
        this.tableId = tableId;
        this.inputProto = inputProto;
    }

    /**
     * Builds the synchronizer output row for a written Longbow record.
     *
     * <p>The returned row copies every field of {@code input} and appends three extra fields: the
     * BigTable table id, the input Protobuf class name, and the Longbow key extracted from the
     * configured write key column. The output arity is the input arity plus
     * {@code Constants.LONGBOW_OUTPUT_ADDITIONAL_ARITY}.
     *
     * @param input the input row produced upstream of the Longbow writer
     * @return a new row containing the original fields followed by the synchronizer metadata
     */
    @Override
    public Row get(Row input) {
        int outputArity = input.getArity() + Constants.LONGBOW_OUTPUT_ADDITIONAL_ARITY;
        int inputArity = input.getArity();
        Row output = new Row(outputArity);
        IntStream.range(0, inputArity).forEach(i -> output.setField(i, input.getField(i)));
        output.setField(inputArity, tableId);
        output.setField(inputArity + 1, inputProto);
        output.setField(inputArity + 2, longbowSchema.getValue(input, LongbowType.LongbowWrite.getKeyName()));
        return output;
    }
}
