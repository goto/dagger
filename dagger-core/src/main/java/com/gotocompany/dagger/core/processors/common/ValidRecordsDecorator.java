package com.gotocompany.dagger.core.processors.common;

import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.types.Row;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.Constants;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporterFactory;
import com.gotocompany.dagger.core.processors.types.FilterDecorator;

import java.util.Arrays;

/**
 * The Valid records decorator.
 */
public class ValidRecordsDecorator extends RichFilterFunction<Row> implements FilterDecorator {

    /**
     * The name of the table whose records are being validated, used in error messages.
     */
    private final String tableName;
    /**
     * The index of the internal validation field within each row.
     */
    private final int validationIndex;
    /**
     * The job configuration used to construct the error reporter.
     */
    private final Configuration configuration;
    /**
     * The Error reporter.
     */
    protected ErrorReporter errorReporter;

    /**
     * Instantiates a new Valid records decorator.
     *
     * @param tableName     the table name
     * @param columns       the columns
     * @param configuration
     */
    public ValidRecordsDecorator(String tableName, String[] columns, Configuration configuration) {
        this.tableName = tableName;
        validationIndex = Arrays.asList(columns).indexOf(Constants.INTERNAL_VALIDATION_FIELD_KEY);
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Initializes the error reporter from the Flink runtime metric group when the function is
     * opened on a task manager.
     *
     * @param internalFlinkConfig the Flink configuration supplied when the function is opened
     * @throws Exception if initialization fails
     */
    @Override
    public void open(org.apache.flink.configuration.Configuration internalFlinkConfig) throws Exception {
        errorReporter = ErrorReporterFactory.getErrorReporter(getRuntimeContext().getMetricGroup(), this.configuration);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always, since invalid records must always be filtered out
     */
    @Override
    public Boolean canDecorate() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Keeps only valid records. When a record's internal validation flag is {@code false} the
     * failure is reported and an exception is thrown to fail the job, preventing bad records from
     * propagating downstream.
     *
     * @param value the record to validate
     * @return {@code true} when the record is valid
     * @throws Exception if the record is invalid, wrapping an {@code InvalidProtocolBufferException}
     */
    @Override
    public boolean filter(Row value) throws Exception {
        if (!(boolean) value.getField(validationIndex)) {
            Exception ex = new InvalidProtocolBufferException("Bad Record Encountered for table `" + this.tableName + "`");
            errorReporter.reportFatalException(ex);
            throw ex;
        }
        return true;
    }
}
