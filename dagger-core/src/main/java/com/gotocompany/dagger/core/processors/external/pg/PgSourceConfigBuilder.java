package com.gotocompany.dagger.core.processors.external.pg;

import java.util.Map;

/**
 * Fluent builder for {@link PgSourceConfig}.
 *
 * <p>Collects the PostgreSQL connection settings, query pattern/variables, output mapping, timeouts,
 * and failure-handling options through chained setters, then assembles an immutable
 * {@link PgSourceConfig} via {@link #createPgSourceConfig()}. Useful for constructing the config
 * programmatically (for example in tests) instead of deserializing it.
 */
public class PgSourceConfigBuilder {
    /** Hostname of the PostgreSQL server. */
    private String host;
    /** Port of the PostgreSQL server. */
    private String port;
    /** Username used to authenticate with the database. */
    private String user;
    /** Password used to authenticate with the database. */
    private String password;
    /** Name of the database to connect to. */
    private String database;
    /** Optional protobuf message type used to type-cast result values. */
    private String type;
    /** Maximum number of concurrent async queries and the connection-pool size. */
    private String capacity;
    /** Async-IO completion timeout in milliseconds. */
    private String streamTimeout;
    /** Mapping from each output column name to the result-set column supplying its value. */
    private Map<String, String> outputMapping;
    /** Connection timeout in milliseconds. */
    private String connectTimeout;
    /** Idle-connection timeout in milliseconds. */
    private String idleTimeout;
    /** Comma-separated input columns whose values fill the query pattern. */
    private String queryVariables;
    /** Format template for the SQL query. */
    private String queryPattern;
    /** Whether a failed lookup should abort the job. */
    private boolean failOnErrors;
    /** Identifier used to tag the metrics emitted for this source. */
    private String metricId;
    /** Whether to keep the raw result value type instead of casting it. */
    private boolean retainResponseType;

    /**
     * Sets the PostgreSQL server hostname.
     *
     * @param host the server hostname
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the PostgreSQL server port.
     *
     * @param port the server port
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setPort(String port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the database username.
     *
     * @param user the database username
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * Sets the database password.
     *
     * @param password the database password
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Sets the database name to connect to.
     *
     * @param database the database name
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setDatabase(String database) {
        this.database = database;
        return this;
    }

    /**
     * Sets the optional protobuf type used to cast result values.
     *
     * @param type the protobuf message type name
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the maximum number of concurrent queries and the connection-pool size.
     *
     * @param capacity the capacity value
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setCapacity(String capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Sets the async-IO completion timeout in milliseconds.
     *
     * @param streamTimeout the stream timeout in milliseconds
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setStreamTimeout(String streamTimeout) {
        this.streamTimeout = streamTimeout;
        return this;
    }

    /**
     * Sets the mapping from output column to result-set column.
     *
     * @param outputMapping the output column to result-set column mapping
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setOutputMapping(Map<String, String> outputMapping) {
        this.outputMapping = outputMapping;
        return this;
    }

    /**
     * Sets the connection timeout in milliseconds.
     *
     * @param connectTimeout the connect timeout in milliseconds
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the idle-connection timeout in milliseconds.
     *
     * @param idleTimeout the idle timeout in milliseconds
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * Sets the input columns that fill the query pattern.
     *
     * @param queryVariables the comma-separated query variable column names
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setQueryVariables(String queryVariables) {
        this.queryVariables = queryVariables;
        return this;
    }

    /**
     * Sets the SQL query template.
     *
     * @param queryPattern the SQL query pattern
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setQueryPattern(String queryPattern) {
        this.queryPattern = queryPattern;
        return this;
    }

    /**
     * Sets whether a lookup failure should abort the job.
     *
     * @param failOnErrors {@code true} to fail the job on query errors, {@code false} to tolerate them
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setFailOnErrors(boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
        return this;
    }

    /**
     * Sets the identifier used to tag this source's metrics.
     *
     * @param metricId the metric id
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setMetricId(String metricId) {
        this.metricId = metricId;
        return this;
    }

    /**
     * Sets whether to keep the raw result type instead of casting it.
     *
     * @param retainResponseType {@code true} to store values as-is, {@code false} to cast to the protobuf type
     * @return this builder for chaining
     */
    public PgSourceConfigBuilder setRetainResponseType(boolean retainResponseType) {
        this.retainResponseType = retainResponseType;
        return this;
    }

    /**
     * Builds an immutable {@link PgSourceConfig} from the values collected by this builder.
     *
     * @return the assembled Postgres source configuration
     */
    public PgSourceConfig createPgSourceConfig() {
        return new PgSourceConfig(host, port, user, password, database, type, capacity, streamTimeout, outputMapping, connectTimeout, idleTimeout, queryVariables, queryPattern, failOnErrors, metricId, retainResponseType);
    }
}
