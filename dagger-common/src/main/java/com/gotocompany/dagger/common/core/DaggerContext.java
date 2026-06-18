package com.gotocompany.dagger.common.core;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.exceptions.DaggerContextException;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DaggerContext singleton object.
 * It initializes with StreamExecutionEnvironment, StreamTableEnvironment and Configuration.
 */
public class DaggerContext {
    /**
     * Logger used to record lifecycle events of the {@link DaggerContext} singleton.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DaggerContext.class.getName());
    /**
     * The lazily-created, {@code volatile} singleton instance shared across the Dagger job.
     */
    private static volatile DaggerContext daggerContext = null;
    /**
     * The Flink {@link StreamExecutionEnvironment} that backs the streaming job.
     */
    private final StreamExecutionEnvironment executionEnvironment;
    /**
     * The Flink {@link StreamTableEnvironment} used to evaluate the Table/SQL pipeline.
     */
    private final StreamTableEnvironment tableEnvironment;
    /**
     * The user-supplied {@link Configuration} that parameterizes the Dagger job.
     */
    private final Configuration configuration;

    /**
     * Instantiates a new DaggerContext.
     *
     * @param configuration the Configuration
     */
    private DaggerContext(Configuration configuration) {
        this.executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings environmentSettings = EnvironmentSettings.newInstance().inStreamingMode().build();
        tableEnvironment = StreamTableEnvironment.create(executionEnvironment, environmentSettings);
        this.configuration = configuration;
    }

    /**
     * Get the instance of DaggerContext.
     */
    public static DaggerContext getInstance() {
        if (daggerContext == null) {
            throw new DaggerContextException("DaggerContext object is not initialized");
        }
        return daggerContext;
    }

    /**
     * Initialization of a new DaggerContext.
     *
     * @param configuration the Configuration
     */
    public static synchronized DaggerContext init(Configuration configuration) {
        if (daggerContext != null) {
            throw new DaggerContextException("DaggerContext object is already initialized");
        }
        daggerContext = new DaggerContext(configuration);
        LOGGER.info("DaggerContext is initialized");
        return daggerContext;
    }

    /**
     * Returns the Flink {@link StreamExecutionEnvironment} held by this context.
     *
     * @return the stream execution environment
     */
    public StreamExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    /**
     * Returns the Flink {@link StreamTableEnvironment} held by this context.
     *
     * @return the stream table environment
     */
    public StreamTableEnvironment getTableEnvironment() {
        return tableEnvironment;
    }

    /**
     * Returns the {@link Configuration} that was used to initialize this context.
     *
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }
}
