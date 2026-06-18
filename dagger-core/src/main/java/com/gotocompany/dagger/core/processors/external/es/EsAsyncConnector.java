package com.gotocompany.dagger.core.processors.external.es;

import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.processors.common.PostResponseTelemetry;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.external.ExternalMetricConfig;
import com.gotocompany.dagger.core.processors.external.AsyncConnector;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The ElasticSearch async connector.
 */
public class EsAsyncConnector extends AsyncConnector {

    /**
     * The Elasticsearch source configuration backing this connector's lookups.
     */
    private final EsSourceConfig esSourceConfig;
    /**
     * The low-level Elasticsearch REST client used to issue asynchronous search requests.
     */
    private RestClient esClient;

    /**
     * Instantiates a new ElasticSearch async connector with specified elasticsearch client.
     *
     * @param esSourceConfig       the es source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     * @param esClient             the es client
     * @param errorReporter        the error reporter
     * @param meterStatsManager    the meter stats manager
     */
    EsAsyncConnector(EsSourceConfig esSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig,
                     RestClient esClient, ErrorReporter errorReporter, MeterStatsManager meterStatsManager) {
        this(esSourceConfig, externalMetricConfig, schemaConfig);
        this.esClient = esClient;
        setErrorReporter(errorReporter);
        setMeterStatsManager(meterStatsManager);
    }

    /**
     * Instantiates a new ElasticSearch async connector.
     *
     * @param esSourceConfig       the es source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     */
    public EsAsyncConnector(EsSourceConfig esSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig) {
        super(Constants.ES_TYPE, esSourceConfig, externalMetricConfig, schemaConfig);
        this.esSourceConfig = esSourceConfig;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily builds the Elasticsearch {@link RestClient} from the configured hosts, attaching the
     * credentials provider, request timeouts, and maximum retry timeout, unless a client was already injected.
     */
    @Override
    protected void createClient() {
        if (esClient == null) {
            esClient = RestClient.builder(
                    getHttpHosts()
            ).setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder
                            .setDefaultCredentialsProvider(getCredentialsProvider())
                            .setDefaultRequestConfig(getRequestConfig())).setMaxRetryTimeoutMillis(esSourceConfig.getRetryTimeout()).build();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the endpoint variables from the input row, validates the query, formats the configured
     * endpoint pattern, and issues an asynchronous {@code GET} request whose response is handled by an
     * {@link EsResponseHandler}.
     *
     * @param input        the incoming row supplying the endpoint variable values
     * @param resultFuture the future completed with the enriched row or an error
     */
    @Override
    protected void process(Row input, ResultFuture<Row> resultFuture) {
        RowManager rowManager = new RowManager(input);
        Object[] endpointVariablesValues = getEndpointHandler()
                .getVariablesValue(rowManager, Constants.ExternalPostProcessorVariableType.ENDPOINT_VARIABLE, esSourceConfig.getVariables(), resultFuture);
        if (getEndpointHandler().isQueryInvalid(resultFuture, rowManager, esSourceConfig.getVariables(), endpointVariablesValues)) {
            return;
        }
        String esEndpoint = String.format(esSourceConfig.getPattern(), endpointVariablesValues);
        Request esRequest = new Request("GET", esEndpoint);
        EsResponseHandler esResponseHandler = new EsResponseHandler(esSourceConfig, getMeterStatsManager(), rowManager,
                getColumnNameManager(), getOutputDescriptor(resultFuture), resultFuture, getErrorReporter(), new PostResponseTelemetry());
        esResponseHandler.startTimer();
        esClient.performRequestAsync(esRequest, esResponseHandler);
    }

    /**
     * Parses the comma-separated host configuration into an array of {@link HttpHost} entries.
     *
     * @return the Elasticsearch hosts to connect to, each bound to the configured port
     */
    private HttpHost[] getHttpHosts() {
        List<String> hosts = Arrays.asList(esSourceConfig.getHost().split(","));
        ArrayList<HttpHost> httpHosts = new ArrayList<>();
        hosts.forEach(s -> httpHosts.add(new HttpHost(s, esSourceConfig.getPort())));
        return httpHosts.toArray(new HttpHost[0]);
    }

    /**
     * Builds the request configuration carrying the configured connect and socket timeouts.
     *
     * @return the {@link RequestConfig} applied to each Elasticsearch request
     */
    private RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(esSourceConfig.getConnectTimeout())
                .setSocketTimeout(esSourceConfig.getSocketTimeout()).build();
    }

    /**
     * Builds a credentials provider seeded with the configured username and password for basic auth.
     *
     * @return the {@link CredentialsProvider} used to authenticate Elasticsearch requests
     */
    private CredentialsProvider getCredentialsProvider() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(esSourceConfig.getUser(), esSourceConfig.getPassword()));
        return credentialsProvider;
    }
}
