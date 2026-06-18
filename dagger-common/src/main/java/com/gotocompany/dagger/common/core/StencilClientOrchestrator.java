package com.gotocompany.dagger.common.core;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.stencil.StencilClientFactory;
import com.gotocompany.stencil.cache.SchemaRefreshStrategy;
import com.gotocompany.stencil.client.StencilClient;
import com.gotocompany.stencil.config.StencilConfig;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.gotocompany.dagger.common.core.Constants.*;

/**
 * The Stencil client orchestrator for dagger.
 */
public class StencilClientOrchestrator implements Serializable {
    /**
     * The process-wide {@link StencilClient} cached and shared across orchestrator instances.
     */
    private static StencilClient stencilClient;
    /**
     * Logger used to report invalid header configuration and other diagnostics.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StencilClientOrchestrator.class);
    /**
     * The Dagger {@link Configuration} from which Stencil settings are read.
     */
    private Configuration configuration;
    /**
     * The de-duplicated set of Stencil (schema registry) URLs to fetch descriptors from.
     */
    private HashSet<String> stencilUrls;

    /**
     * Instantiates a new Stencil client orchestrator.
     *
     * @param configuration the configuration
     */
    public StencilClientOrchestrator(Configuration configuration) {
        this.configuration = configuration;
        this.stencilUrls = getStencilUrls();
    }

    /**
     * Builds a {@link StencilConfig} from the Dagger {@link Configuration}, wiring fetch headers,
     * timeouts, cache behaviour, refresh strategy and retry/backoff settings.
     *
     * @return the assembled Stencil configuration
     */
    public StencilConfig createStencilConfig() {
        return StencilConfig.builder()
                .fetchHeaders(getHeaders(configuration))
                .fetchTimeoutMs(configuration.getInteger(SCHEMA_REGISTRY_STENCIL_FETCH_TIMEOUT_MS, SCHEMA_REGISTRY_STENCIL_FETCH_TIMEOUT_MS_DEFAULT))
                .cacheAutoRefresh(configuration.getBoolean(SCHEMA_REGISTRY_STENCIL_CACHE_AUTO_REFRESH_KEY, SCHEMA_REGISTRY_STENCIL_CACHE_AUTO_REFRESH_DEFAULT))
                .cacheTtlMs(configuration.getLong(SCHEMA_REGISTRY_STENCIL_CACHE_TTL_MS_KEY, SCHEMA_REGISTRY_STENCIL_CACHE_TTL_MS_DEFAULT))
                .refreshStrategy(getSchemaRefreshStrategy(configuration.getString(SCHEMA_REGISTRY_STENCIL_REFRESH_STRATEGY_KEY, SCHEMA_REGISTRY_STENCIL_REFRESH_STRATEGY_DEFAULT)))
                .fetchBackoffMinMs(configuration.getLong(SCHEMA_REGISTRY_STENCIL_FETCH_BACKOFF_MIN_MS_KEY, SCHEMA_REGISTRY_STENCIL_FETCH_BACKOFF_MIN_MS_DEFAULT))
                .fetchRetries(configuration.getInteger(SCHEMA_REGISTRY_STENCIL_FETCH_RETRIES_KEY, SCHEMA_REGISTRY_STENCIL_FETCH_RETRIES_DEFAULT))
                .build();
    }

    /**
     * Resolves the {@link SchemaRefreshStrategy} to use for the Stencil cache.
     *
     * <p>Returns a version-based refresh strategy when {@code refreshStrategy} equals
     * {@code "VERSION_BASED_REFRESH"} (case-insensitive); otherwise it falls back to the
     * long-polling strategy, including when {@code refreshStrategy} is {@code null}.
     *
     * @param refreshStrategy the configured refresh-strategy name, may be {@code null}
     * @return the matching schema refresh strategy
     */
    private SchemaRefreshStrategy getSchemaRefreshStrategy(String refreshStrategy) {
        if (refreshStrategy == null) {
            return SchemaRefreshStrategy.longPollingStrategy();
        }
        if (refreshStrategy.equalsIgnoreCase("VERSION_BASED_REFRESH")) {
            return SchemaRefreshStrategy.versionBasedRefresh();
        }
        return SchemaRefreshStrategy.longPollingStrategy();

    }

    /**
     * Reads the configured fetch-header string and parses it into HTTP headers.
     *
     * @param config the configuration to read the header string from
     * @return the parsed list of {@code Header} objects; empty when none are configured
     */
    private List<Header> getHeaders(Configuration config) {
        String headerString = config.getString(SCHEMA_REGISTRY_STENCIL_FETCH_HEADERS_KEY, SCHEMA_REGISTRY_STENCIL_FETCH_HEADERS_DEFAULT);
        return parseHeaders(headerString);
    }

    /**
     * Gets stencil client.
     *
     * @return the stencil client
     */
    public StencilClient getStencilClient() {

        if (stencilClient != null) {
            return stencilClient;
        }

        stencilClient = initStencilClient(new ArrayList<>(stencilUrls));
        return stencilClient;
    }

    /**
     * Enrich stencil client.
     *
     * @param additionalStencilUrls the additional stencil urls
     * @return the stencil client
     */
    public StencilClient enrichStencilClient(List<String> additionalStencilUrls) {
        if (additionalStencilUrls.isEmpty()) {
            return stencilClient;
        }

        stencilUrls.addAll(additionalStencilUrls);
        stencilClient = initStencilClient(new ArrayList<>(stencilUrls));
        return stencilClient;
    }

    /**
     * Creates a {@link StencilClient} for the given URLs.
     *
     * <p>When remote Stencil is enabled in the configuration a registry-backed client is built
     * from {@code urls} and the {@link StencilConfig}; otherwise a default in-classpath client
     * is returned.
     *
     * @param urls the Stencil registry URLs to fetch descriptors from
     * @return the initialized Stencil client
     */
    private StencilClient initStencilClient(List<String> urls) {
        StencilConfig stencilConfig = createStencilConfig();
        boolean enableRemoteStencil = configuration.getBoolean(SCHEMA_REGISTRY_STENCIL_ENABLE_KEY, SCHEMA_REGISTRY_STENCIL_ENABLE_DEFAULT);
        return enableRemoteStencil
                ? StencilClientFactory.getClient(urls, stencilConfig)
                : StencilClientFactory.getClient();
    }

    /**
     * Splits a comma-separated header string into individual, validated HTTP headers.
     *
     * @param headersString the raw {@code key:value} header pairs separated by commas,
     *                       treated as empty when {@code null}
     * @return the list of valid parsed headers; invalid entries are skipped
     */
    private List<Header> parseHeaders(String headersString) {
        headersString = headersString == null ? "" : headersString;
        return Arrays.stream(headersString.split(","))
                .map(String::trim)
                .filter(this::isValidHeader)
                .map(this::parseHeader)
                .collect(Collectors.toList());
    }

    /**
     * Checks whether a single header entry is well-formed, i.e. it contains exactly one
     * non-empty key and one non-empty value separated by a colon.
     *
     * <p>A non-empty but malformed entry is logged and treated as invalid.
     *
     * @param headerString the trimmed {@code key:value} entry to validate
     * @return {@code true} if the entry has a valid key and value, otherwise {@code false}
     */
    private Boolean isValidHeader(String headerString) {
        Boolean isValid = Arrays.stream(headerString.split(":")).map(String::trim).filter(a -> !a.isEmpty()).count() == 2;
        if (!isValid && !headerString.isEmpty()) {
            LOGGER.error("Invalid header {}. This will be ignored", headerString);
        }
        return isValid;
    }

    /**
     * Converts a single {@code key:value} entry into a {@link BasicHeader}.
     *
     * @param headerString the entry to parse; expected to contain a colon separator
     * @return the header built from the trimmed key and value
     */
    private BasicHeader parseHeader(String headerString) {
        String[] split = headerString.split(":");
        return new BasicHeader(split[0].trim(), split[1].trim());
    }

    /**
     * Reads the configured Stencil URLs and collects them into a de-duplicated set.
     *
     * @return the set of trimmed, unique Stencil registry URLs
     */
    private HashSet<String> getStencilUrls() {
        stencilUrls = Arrays.stream(configuration.getString(SCHEMA_REGISTRY_STENCIL_URLS_KEY, SCHEMA_REGISTRY_STENCIL_URLS_DEFAULT).split(","))
                .map(String::trim)
                .collect(Collectors.toCollection(HashSet::new));
        return stencilUrls;
    }
}
