package com.gotocompany.dagger.core;

import java.io.IOException;

/**
 * An interface derived from the publicly exposed methods of {@code DaggerSqlJobBuilder}
 * previously referred as StreamManager.
 * <p>
 * The {@code KafkaProtoSQLProcessor}, which serves as the program entry point,
 * initializes an instance for the given {@code JOB_BUILDER_FQCN} value.
 * Ensure that the job builder class is bundled with the program during any
 * subsequent build stages. If it is not, the system falls back to the
 * {@code DEFAULT_JOB_BUILDER_FQCN} class, i.e., {@code com.gotocompany.dagger.core.DaggerSqlJobBuilder}.
 * <p>
 * Additionally, the job builder class is expected to provide a constructor
 * that accepts a single parameter of type {@code DaggerContext}
 */
public interface JobBuilder {

    /**
     * Initializes the Flink execution and table environments and registers global job settings.
     *
     * @return this builder, to allow fluent chaining of the registration stages
     */
    JobBuilder registerConfigs();

    /**
     * Registers the configured source streams, assigns their watermarks and applies pre-processors.
     *
     * @return this builder, to allow fluent chaining of the registration stages
     */
    JobBuilder registerSourceWithPreProcessors();

    /**
     * Registers the user-defined functions (including Python UDFs) used by the SQL query.
     *
     * @return this builder, to allow fluent chaining of the registration stages
     * @throws IOException if a function factory or Python UDF resource cannot be loaded
     */
    JobBuilder registerFunctions() throws IOException;

    /**
     * Runs the configured SQL query, applies the post-processors and attaches the configured sink.
     *
     * @return this builder, to allow fluent chaining of the registration stages
     */
    JobBuilder registerOutputStream();

    /**
     * Submits and runs the assembled Flink job.
     *
     * @throws Exception if the job fails to execute
     */
    void execute() throws Exception;
}
