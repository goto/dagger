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

    JobBuilder registerConfigs();

    JobBuilder registerSourceWithPreProcessors();

    JobBuilder registerFunctions() throws IOException;

    JobBuilder registerOutputStream();

    void execute() throws Exception;
}
