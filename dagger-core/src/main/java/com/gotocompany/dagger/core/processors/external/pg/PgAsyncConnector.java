package com.gotocompany.dagger.core.processors.external.pg;

import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import com.gotocompany.dagger.core.metrics.aspects.ExternalSourceAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.processors.common.PostResponseTelemetry;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.core.processors.external.ExternalMetricConfig;
import com.gotocompany.dagger.core.processors.external.AsyncConnector;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.impl.PgPoolImpl;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.RowSet;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * The Postgre async connector.
 */
public class PgAsyncConnector extends AsyncConnector {
    /**
     * Logger used to record connection pool lifecycle events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PgAsyncConnector.class.getName());
    /**
     * Configuration describing the Postgres connection, pool sizing, query pattern and output mapping.
     */
    private final PgSourceConfig pgSourceConfig;
    /**
     * The Vert.x Postgres connection pool used to execute lookup queries.
     */
    private PgPool pgClient;

    /**
     * Instantiates a new Postgre async connector with specified postgre client.
     *
     * @param pgSourceConfig       the pg source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     * @param meterStatsManager    the meter stats manager
     * @param pgClient             the pg client
     * @param errorReporter        the error reporter
     */
    public PgAsyncConnector(PgSourceConfig pgSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig,
                            MeterStatsManager meterStatsManager, PgPool pgClient, ErrorReporter errorReporter) {
        this(pgSourceConfig, externalMetricConfig, schemaConfig);
        this.pgClient = pgClient;
        setErrorReporter(errorReporter);
        setMeterStatsManager(meterStatsManager);
    }

    /**
     * Instantiates a new Postgre async connector.
     *
     * @param pgSourceConfig       the pg source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     */
    public PgAsyncConnector(PgSourceConfig pgSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig) {
        super(Constants.PG_TYPE, pgSourceConfig, externalMetricConfig, schemaConfig);
        this.pgSourceConfig = pgSourceConfig;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily builds the Vert.x {@code PgPool} from the configured host, port, database, credentials and
     * timeouts when a client has not already been injected.
     */
    @Override
    protected void createClient() {
        if (pgClient == null) {
            PgConnectOptions connectOptions = new PgConnectOptions()
                    .setPort(pgSourceConfig.getPort())
                    .setHost(pgSourceConfig.getHost())
                    .setDatabase(pgSourceConfig.getDatabase())
                    .setUser(pgSourceConfig.getUser())
                    .setPassword(pgSourceConfig.getPassword())
                    .setConnectTimeout(pgSourceConfig.getConnectTimeout())
                    .setIdleTimeout(pgSourceConfig.getIdleTimeout());

            PoolOptions poolOptions = new PoolOptions()
                    .setMaxSize(pgSourceConfig.getCapacity());

            pgClient = pool(connectOptions, poolOptions);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the query variable values from the incoming row, validates them and, when valid, formats
     * the configured query pattern and executes it asynchronously. A {@link PgResponseHandler} completes the
     * {@code resultFuture} with the enriched row. A query that cannot be created is recorded as an
     * invalid-configuration metric and completes the future exceptionally.
     *
     * @param input the input {@link Row} carrying the values used to build the query
     * @param resultFuture the future completed with the enriched output row or an error
     */
    @Override
    public void process(Row input, ResultFuture<Row> resultFuture) {
        RowManager rowManager = new RowManager(input);

        Object[] queryVariablesValues = getEndpointHandler()
                .getVariablesValue(rowManager, Constants.ExternalPostProcessorVariableType.QUERY_VARIABLES, pgSourceConfig.getVariables(), resultFuture);
        if (getEndpointHandler().isQueryInvalid(resultFuture, rowManager, pgSourceConfig.getVariables(), queryVariablesValues)) {
            return;
        }

        String query = String.format(pgSourceConfig.getPattern(), queryVariablesValues);
        PgResponseHandler pgResponseHandler = new PgResponseHandler(pgSourceConfig, getMeterStatsManager(), rowManager,
                getColumnNameManager(), getOutputDescriptor(resultFuture), resultFuture, getErrorReporter(), new PostResponseTelemetry());

        pgResponseHandler.startTimer();
        Query<RowSet<io.vertx.sqlclient.Row>> executableQuery = pgClient.query(query);
        if (executableQuery == null) {
            getMeterStatsManager().markEvent(ExternalSourceAspects.INVALID_CONFIGURATION);
            Exception invalidConfigurationException = new InvalidConfigurationException(String.format("Query '%s' is invalid", query));
            reportAndThrowError(resultFuture, invalidConfigurationException);
        } else {
            executableQuery.execute(pgResponseHandler);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the Postgres connection pool, clears the reference, records a close-connection metric and
     * logs that the pool has been released.
     */
    @Override
    public void close() {
        pgClient.close();
        pgClient = null;
        getMeterStatsManager().markEvent(ExternalSourceAspects.CLOSE_CONNECTION_ON_EXTERNAL_CLIENT);
        LOGGER.info("DB Connector : Connection pool released");
    }

    /**
     * Creates a new Vert.x {@code PgPool} backed by a dedicated {@code Vertx} instance.
     *
     * <p>Fails when invoked from within an existing Vert.x context, and enables native transport when the
     * connection uses a domain socket.
     *
     * @param connectOptions the Postgres connection options describing host, port, database and credentials
     * @param poolOptions the pool options describing the maximum pool size
     * @return a newly created {@code PgPool} bound to a fresh Vert.x context
     * @throws IllegalStateException if called from within an existing Vert.x context
     */
    private PgPool pool(PgConnectOptions connectOptions, PoolOptions poolOptions) {
        if (Vertx.currentContext() != null) {
            throw new IllegalStateException("Running in a Vertx context => use PgPool#pool(Vertx, PgConnectOptions, PoolOptions) instead");
        }
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setMaxEventLoopExecuteTime(Constants.MAX_EVENT_LOOP_EXECUTE_TIME_DEFAULT);
        vertxOptions.setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS);
        if (connectOptions.isUsingDomainSocket()) {
            vertxOptions.setPreferNativeTransport(true);
        }
        Vertx vertx = Vertx.vertx(vertxOptions);
        return new PgPoolImpl(vertx.getOrCreateContext(), true, connectOptions, poolOptions);
    }

    /**
     * Gets postgre client.
     *
     * @return the pg client
     */
    Object getPgClient() {
        return pgClient;
    }
}
