package com.gotocompany.dagger.core.processors.external.http.request;

import com.google.gson.Gson;
import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import io.netty.util.internal.StringUtil;
import com.gotocompany.dagger.core.processors.external.http.HttpSourceConfig;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UnknownFormatConversionException;

/**
 * The Http post request handler.
 */
public class HttpPostRequestHandler implements HttpRequestHandler {
    /**
     * Configuration describing the endpoint, verb, patterns and headers for the request.
     */
    private HttpSourceConfig httpSourceConfig;
    /**
     * The asynchronous HTTP client used to prepare the request builder.
     */
    private AsyncHttpClient httpClient;
    /**
     * Resolved values substituted into the request pattern that forms the request body.
     */
    private Object[] requestVariablesValues;
    /**
     * Resolved values substituted into the dynamic header pattern.
     */
    private Object[] dynamicHeaderVariablesValues;
    /**
     * Resolved values substituted into the endpoint placeholders.
     */
    private Object[] endpointVariablesValues;
    /**
     * Instantiates a new Http post request handler.
     *
     * @param httpSourceConfig        the http source config
     * @param httpClient              the http client
     * @param requestVariablesValues  the request variables values
     * @param endpointVariablesValues the endpoint variables values
     */
    public HttpPostRequestHandler(HttpSourceConfig httpSourceConfig, AsyncHttpClient httpClient, Object[] requestVariablesValues, Object[] dynamicHeaderVariablesValues, Object[] endpointVariablesValues) {
        this.httpSourceConfig = httpSourceConfig;
        this.httpClient = httpClient;
        this.requestVariablesValues = requestVariablesValues;
        this.dynamicHeaderVariablesValues = dynamicHeaderVariablesValues;
        this.endpointVariablesValues = endpointVariablesValues;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds a POST request by formatting the request body and endpoint from their variable values and
     * applying the static headers together with any dynamic headers produced from the header pattern.
     *
     * @return the prepared {@code BoundRequestBuilder} for the POST request
     * @throws InvalidConfigurationException if the header pattern is invalid or incompatible with the
     *                                       configured header variables
     */
    @Override
    public BoundRequestBuilder create() {
        String requestBody = String.format(httpSourceConfig.getPattern(), requestVariablesValues);
        String endpoint = String.format(httpSourceConfig.getEndpoint(), endpointVariablesValues);

        BoundRequestBuilder postRequest = httpClient
                .preparePost(endpoint)
                .setBody(requestBody);
        Map<String, String> headers = httpSourceConfig.getHeaders();
        if (!StringUtil.isNullOrEmpty(httpSourceConfig.getHeaderPattern())) {
            try {
                String dynamicHeader = String.format(httpSourceConfig.getHeaderPattern(), dynamicHeaderVariablesValues);
                headers.putAll(new Gson().fromJson(dynamicHeader, HashMap.class));
            } catch (UnknownFormatConversionException e) {
                throw new InvalidConfigurationException(String.format("pattern config '%s' is invalid", httpSourceConfig.getHeaderPattern()));
            } catch (IllegalArgumentException e) {
                throw new InvalidConfigurationException(String.format("pattern config '%s' is incompatible with the variable config '%s'", httpSourceConfig.getHeaderPattern(), httpSourceConfig.getHeaderVariables()));
            }
        }
        return addHeaders(postRequest, headers);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This handler can create the request when the configured verb is {@code POST}.
     *
     * @return {@code true} when the configured verb is {@code POST} (case-insensitive), {@code false} otherwise
     */
    @Override
    public boolean canCreate() {
        return httpSourceConfig.getVerb().equalsIgnoreCase("post");
    }
}
