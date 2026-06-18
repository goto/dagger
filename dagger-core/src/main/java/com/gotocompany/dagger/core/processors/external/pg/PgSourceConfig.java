package com.gotocompany.dagger.core.processors.external.pg;

import com.gotocompany.dagger.core.processors.types.SourceConfig;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that holds Postgre configuration.
 */
public class PgSourceConfig implements Serializable, SourceConfig {

    /**
     * The Postgres server host name.
     */
    private final String host;
    /**
     * The Postgres server port.
     */
    private final String port;
    /**
     * The user name used to authenticate with the database.
     */
    private final String user;
    /**
     * The password used to authenticate with the database.
     */
    private final String password;
    /**
     * The name of the database to connect to.
     */
    private final String database;
    /**
     * Fully qualified protobuf message type used for type-aware conversion of the result.
     */
    private final String type;
    /**
     * Maximum number of concurrent connections and asynchronous requests allowed.
     */
    private final String capacity;
    /**
     * Maximum time, in milliseconds, to wait for the asynchronous response before timing out.
     */
    private final String streamTimeout;
    /**
     * Mapping of output column names to the query column they are populated from.
     */
    private final Map<String, String> outputMapping;
    /**
     * Maximum time, in milliseconds, to wait while establishing the connection.
     */
    private final String connectTimeout;
    /**
     * Maximum time a pooled connection may remain idle before being closed.
     */
    private final String idleTimeout;
    /**
     * Comma-separated input columns whose values populate the query pattern.
     */
    private final String queryVariables;
    /**
     * Format pattern used to build the SQL query from the query variables.
     */
    private final String queryPattern;
    /**
     * Whether a failed query should raise a fatal error instead of being reported as non-fatal.
     */
    private boolean failOnErrors;
    /**
     * Identifier used to tag the metrics emitted for this external source.
     */
    @SerializedName(value = "metricId", alternate = {"MetricId", "METRICID"})
    private String metricId;
    /**
     * Whether the raw result value should be retained as-is instead of being converted to the configured type.
     */
    private boolean retainResponseType;

    /**
     * Instantiates a new Postgre source config.
     *
     * @param host               the host
     * @param port               the port
     * @param user               the user
     * @param password           the password
     * @param database           the database
     * @param type               the type
     * @param capacity           the capacity
     * @param streamTimeout      the stream timeout
     * @param outputMapping      the output mapping
     * @param connectTimeout     the connect timeout
     * @param idleTimeout        the idle timeout
     * @param queryVariables     the query variables
     * @param queryPattern       the query pattern
     * @param failOnErrors       the fail on errors
     * @param metricId           the metric id
     * @param retainResponseType the retain response type
     */
    public PgSourceConfig(String host, String port, String user, String password, String database,
                          String type, String capacity, String streamTimeout, Map<String, String> outputMapping, String connectTimeout, String idleTimeout, String queryVariables, String queryPattern, boolean failOnErrors, String metricId, boolean retainResponseType) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
        this.type = type;
        this.capacity = capacity;
        this.outputMapping = outputMapping;
        this.connectTimeout = connectTimeout;
        this.idleTimeout = idleTimeout;
        this.streamTimeout = streamTimeout;
        this.queryVariables = queryVariables;
        this.queryPattern = queryPattern;
        this.failOnErrors = failOnErrors;
        this.metricId = metricId;
        this.retainResponseType = retainResponseType;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The output columns are derived from the keys of the configured output mapping.
     *
     * @return the {@code List<String>} of output column names
     */
    @Override
    public List<String> getOutputColumns() {
        return new ArrayList<>(outputMapping.keySet());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Collects the settings that must be present for the configuration to be valid, such as the host,
     * port, credentials, database, capacity, timeouts, query pattern and output mapping.
     *
     * @return a {@code HashMap<String, Object>} of mandatory field names to their configured values
     */
    @Override
    public HashMap<String, Object> getMandatoryFields() {
        HashMap<String, Object> mandatoryFields = new HashMap<>();
        mandatoryFields.put("host", host);
        mandatoryFields.put("port", port);
        mandatoryFields.put("user", user);
        mandatoryFields.put("password", password);
        mandatoryFields.put("database", database);
        mandatoryFields.put("capacity", capacity);
        mandatoryFields.put("stream_timeout", streamTimeout);
        mandatoryFields.put("connect_timeout", connectTimeout);
        mandatoryFields.put("idle_timeout", idleTimeout);
        mandatoryFields.put("query_pattern", queryPattern);
        mandatoryFields.put("output_mapping", outputMapping);
        mandatoryFields.put("fail_on_errors", failOnErrors);

        return mandatoryFields;
    }

    /**
     * Gets stream timeout.
     *
     * @return the stream timeout
     */
    public Integer getStreamTimeout() {
        return Integer.valueOf(streamTimeout);
    }

    /**
     * Gets capacity.
     *
     * @return the capacity
     */
    public Integer getCapacity() {
        return Integer.valueOf(capacity);
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public Integer getPort() {
        return Integer.valueOf(port);
    }

    /**
     * Gets host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets database.
     *
     * @return the database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets connect timeout.
     *
     * @return the connect timeout
     */
    public Integer getConnectTimeout() {
        return Integer.valueOf(connectTimeout);
    }

    /**
     * Gets idle timeout.
     *
     * @return the idle timeout
     */
    public Integer getIdleTimeout() {
        return Integer.valueOf(idleTimeout);
    }

    /**
     * Gets query variables.
     *
     * @return the query variables
     */
    public String getQueryVariables() {
        return queryVariables;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For a Postgres source the pattern is the configured query pattern.
     *
     * @return the SQL query format pattern
     */
    @Override
    public String getPattern() {
        return queryPattern;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For a Postgres source the variables are the configured query variables.
     *
     * @return the comma-separated query variable columns
     */
    @Override
    public String getVariables() {
        return queryVariables;
    }

    /**
     * Check if type config is not empty.
     *
     * @return the boolean
     */
    public boolean hasType() {
        return StringUtils.isNotEmpty(type);
    }

    /**
     * Gets the configured protobuf message type.
     *
     * @return the fully qualified type name, or {@code null} when no type is configured
     */
    public String getType() {
        return type;
    }

    /**
     * Gets mapped query param.
     *
     * @param outputColumn the output column
     * @return the mapped query param
     */
    public String getMappedQueryParam(String outputColumn) {
        return outputMapping.get(outputColumn);
    }

    /**
     * Returns whether the connector should fail fatally when the query errors.
     *
     * @return {@code true} if failures should be treated as fatal, {@code false} otherwise
     */
    public boolean isFailOnErrors() {
        return failOnErrors;
    }

    /**
     * {@inheritDoc}
     *
     * @return the metric identifier configured for this source
     */
    @Override
    public String getMetricId() {
        return metricId;
    }

    /**
     * Check if it is retain response type.
     *
     * @return the boolean
     */
    public boolean isRetainResponseType() {
        return retainResponseType;
    }
}
