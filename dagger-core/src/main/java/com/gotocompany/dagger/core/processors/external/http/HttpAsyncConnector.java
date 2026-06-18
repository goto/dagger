package com.gotocompany.dagger.core.processors.external.http;

import com.gotocompany.dagger.core.exception.InvalidHttpVerbException;
import com.gotocompany.dagger.core.metrics.aspects.ExternalSourceAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.processors.common.DescriptorManager;
import com.gotocompany.dagger.core.processors.common.PostResponseTelemetry;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.external.http.request.HttpRequestFactory;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.core.processors.external.AsyncConnector;
import com.gotocompany.dagger.core.processors.external.ExternalMetricConfig;
import io.netty.util.internal.StringUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

/**
 * The Http async connector.
 */
public class HttpAsyncConnector extends AsyncConnector {

    /**
     * Logger used to record connector lifecycle events such as connection closure.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpAsyncConnector.class.getName());
    /**
     * The asynchronous HTTP client used to execute the outbound enrichment requests.
     */
    private AsyncHttpClient httpClient;
    /**
     * Configuration describing the endpoint, verb, request/header patterns and output mapping for this connector.
     */
    private HttpSourceConfig httpSourceConfig;
    /**
     * The set of HTTP status codes excluded from triggering a fatal failure even when fail-on-errors is enabled.
     */
    private Set<Integer> failOnErrorsExclusionSet;

    /**
     * Instantiates a new Http async connector with specified http client.
     *
     * @param httpSourceConfig     the http source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     * @param httpClient           the http client
     * @param errorReporter        the error reporter
     * @param meterStatsManager    the meter stats manager
     * @param descriptorManager    the descriptor manager
     */
    public HttpAsyncConnector(HttpSourceConfig httpSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig,
                              AsyncHttpClient httpClient, ErrorReporter errorReporter, MeterStatsManager meterStatsManager, DescriptorManager descriptorManager) {
        this(httpSourceConfig, externalMetricConfig, schemaConfig);
        this.httpClient = httpClient;
        setErrorReporter(errorReporter);
        setMeterStatsManager(meterStatsManager);
        setDescriptorManager(descriptorManager);
    }

    /**
     * Instantiates a new Http async connector.
     *
     * @param httpSourceConfig     the http source config
     * @param externalMetricConfig the external metric config
     * @param schemaConfig         the schema config
     */
    public HttpAsyncConnector(HttpSourceConfig httpSourceConfig, ExternalMetricConfig externalMetricConfig, SchemaConfig schemaConfig) {
        super(Constants.HTTP_TYPE, httpSourceConfig, externalMetricConfig, schemaConfig);
        this.httpSourceConfig = httpSourceConfig;
    }

    /**
     * Gets http client.
     *
     * @return the http client
     */
    AsyncHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily creates the underlying {@link AsyncHttpClient} when one has not already been injected,
     * applying the connect timeout taken from the {@code HttpSourceConfig}.
     */
    @Override
    protected void createClient() {
        if (httpClient == null) {
            httpClient = asyncHttpClient(config().setConnectTimeout(httpSourceConfig.getConnectTimeout()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Opens the connector by delegating to the superclass and then computing the set of status codes
     * that are excluded from fail-on-errors handling from the configured code ranges.
     *
     * @param configuration the Flink {@code Configuration} supplied when the async operator is opened
     * @throws Exception if the superclass fails to initialise the connector
     */
    @Override
    public void open(Configuration configuration) throws Exception {
        super.open(configuration);
        setFailOnErrorsExclusionSet(httpSourceConfig.getExcludeFailOnErrorsCodeRange());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the underlying HTTP client, clears the reference, records a close-connection metric and
     * logs that the connector has been shut down.
     *
     * @throws Exception if closing the underlying HTTP client fails
     */
    @Override
    public void close() throws Exception {
        httpClient.close();
        httpClient = null;
        getMeterStatsManager().markEvent(ExternalSourceAspects.CLOSE_CONNECTION_ON_EXTERNAL_CLIENT);
        LOGGER.error("HTTP Connector : Connection closed");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the request, dynamic header and endpoint variable values from the incoming row, validates
     * them and, when valid, builds the request via {@link HttpRequestFactory} and executes it asynchronously.
     * A {@link HttpResponseHandler} completes the {@code resultFuture} once a response or error is received.
     * An unsupported HTTP verb is recorded as an invalid-configuration metric and completes the future
     * exceptionally.
     *
     * @param input the input {@link Row} carrying the values used to populate the request
     * @param resultFuture the future completed with the enriched output row or an error
     */
    @Override
    protected void process(Row input, ResultFuture<Row> resultFuture) {
        try {
            RowManager rowManager = new RowManager(input);

            Object[] requestVariablesValues = getEndpointHandler()
                    .getVariablesValue(rowManager, Constants.ExternalPostProcessorVariableType.REQUEST_VARIABLES, httpSourceConfig.getRequestVariables(), resultFuture);
            Object[] dynamicHeaderVariablesValues = getEndpointHandler()
                    .getVariablesValue(rowManager, Constants.ExternalPostProcessorVariableType.HEADER_VARIABLES, httpSourceConfig.getHeaderVariables(), resultFuture);
            Object[] endpointVariablesValues = getEndpointHandler()
                    .getVariablesValue(rowManager, Constants.ExternalPostProcessorVariableType.ENDPOINT_VARIABLE, httpSourceConfig.getEndpointVariables(), resultFuture);
            if (getEndpointHandler().isQueryInvalid(resultFuture, rowManager, httpSourceConfig.getRequestVariables(), requestVariablesValues) || getEndpointHandler().isQueryInvalid(resultFuture, rowManager, httpSourceConfig.getHeaderVariables(), dynamicHeaderVariablesValues)) {
                return;
            }
            BoundRequestBuilder request = HttpRequestFactory.createRequest(httpSourceConfig, httpClient, requestVariablesValues, dynamicHeaderVariablesValues, endpointVariablesValues);
            HttpResponseHandler httpResponseHandler = new HttpResponseHandler(httpSourceConfig, getFailOnErrorsExclusionSet(), getMeterStatsManager(),
                    rowManager, getColumnNameManager(), getOutputDescriptor(resultFuture), resultFuture, getErrorReporter(), new PostResponseTelemetry());
            httpResponseHandler.startTimer();
            request.execute(httpResponseHandler);
        } catch (InvalidHttpVerbException e) {
            getMeterStatsManager().markEvent(ExternalSourceAspects.INVALID_CONFIGURATION);
            resultFuture.completeExceptionally(e);
        }

    }

    /**
     * Returns the set of HTTP status codes excluded from fail-on-errors handling.
     *
     * @return the {@code Set<Integer>} of status codes for which the connector will not fail even when
     *         fail-on-errors is enabled
     */
    protected Set<Integer> getFailOnErrorsExclusionSet() {
        return failOnErrorsExclusionSet;
    }

    /**
     * Parses the configured comma-separated, hyphen-delimited status code ranges into the exclusion set.
     *
     * <p>For example {@code "500-502,504"} expands to the codes {@code 500}, {@code 501}, {@code 502} and
     * {@code 504}. A {@code null} or empty value leaves the exclusion set empty.
     *
     * @param excludeFailOnErrorsCodeRange the raw configuration string describing status code ranges,
     *                                      which may be {@code null} or empty
     */
    private void setFailOnErrorsExclusionSet(String excludeFailOnErrorsCodeRange) {
        failOnErrorsExclusionSet = new HashSet<Integer>();
        if (!StringUtil.isNullOrEmpty(excludeFailOnErrorsCodeRange)) {
            String[] ranges = excludeFailOnErrorsCodeRange.split(",");
            Arrays.stream(ranges).forEach(range -> {
                List<Integer> rangeList = Arrays.stream(range.split("-")).map(Integer::parseInt).collect(Collectors.toList());
                IntStream.rangeClosed(rangeList.get(0), rangeList.get(rangeList.size() - 1)).forEach(statusCode -> failOnErrorsExclusionSet.add(statusCode));
            });
        }
    }
}
