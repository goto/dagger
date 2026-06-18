package com.gotocompany.dagger.common.core;

/**
 * Centralized constants shared across Dagger's common module.
 *
 * <p>Holds the configuration keys (and their default values) for the Stencil schema-registry
 * client, a handful of telemetry/UDF identifiers, stream input-schema configuration keys, and the
 * internal column names Dagger appends to every deserialized {@link org.apache.flink.types.Row}.
 * This is a constant holder and is not intended to be instantiated.
 */
public class Constants {
    /** Configuration key toggling use of the remote Stencil schema registry. */
    public static final String SCHEMA_REGISTRY_STENCIL_ENABLE_KEY = "SCHEMA_REGISTRY_STENCIL_ENABLE";
    /** Default for {@link #SCHEMA_REGISTRY_STENCIL_ENABLE_KEY}: remote Stencil disabled. */
    public static final boolean SCHEMA_REGISTRY_STENCIL_ENABLE_DEFAULT = false;
    /** Configuration key holding the comma-separated Stencil registry URLs. */
    public static final String SCHEMA_REGISTRY_STENCIL_URLS_KEY = "SCHEMA_REGISTRY_STENCIL_URLS";
    /** Default for {@link #SCHEMA_REGISTRY_STENCIL_URLS_KEY}: no URLs configured. */
    public static final String SCHEMA_REGISTRY_STENCIL_URLS_DEFAULT = "";
    /** Configuration key for the Stencil descriptor fetch timeout, in milliseconds. */
    public static final String SCHEMA_REGISTRY_STENCIL_FETCH_TIMEOUT_MS = "SCHEMA_REGISTRY_STENCIL_FETCH_TIMEOUT_MS";
    /** Default Stencil fetch timeout: {@code 10000} ms (10 seconds). */
    public static final Integer SCHEMA_REGISTRY_STENCIL_FETCH_TIMEOUT_MS_DEFAULT = 10000;
    /** Configuration key for the comma-separated {@code name:value} HTTP headers sent to Stencil. */
    public static final String SCHEMA_REGISTRY_STENCIL_FETCH_HEADERS_KEY = "SCHEMA_REGISTRY_STENCIL_FETCH_HEADERS";
    /** Default for {@link #SCHEMA_REGISTRY_STENCIL_FETCH_HEADERS_KEY}: no headers. */
    public static final String SCHEMA_REGISTRY_STENCIL_FETCH_HEADERS_DEFAULT = "";
    /** Configuration key toggling automatic refresh of the Stencil descriptor cache. */
    public static final String SCHEMA_REGISTRY_STENCIL_CACHE_AUTO_REFRESH_KEY = "SCHEMA_REGISTRY_STENCIL_CACHE_AUTO_REFRESH";
    /** Default for {@link #SCHEMA_REGISTRY_STENCIL_CACHE_AUTO_REFRESH_KEY}: auto-refresh disabled. */
    public static final boolean SCHEMA_REGISTRY_STENCIL_CACHE_AUTO_REFRESH_DEFAULT = false;
    /** Configuration key for the Stencil descriptor cache time-to-live, in milliseconds. */
    public static final String SCHEMA_REGISTRY_STENCIL_CACHE_TTL_MS_KEY = "SCHEMA_REGISTRY_STENCIL_CACHE_TTL_MS";
    /** Default Stencil cache TTL: {@code 900000} ms (15 minutes). */
    public static final Long SCHEMA_REGISTRY_STENCIL_CACHE_TTL_MS_DEFAULT = 900000L;
    /** Configuration key selecting the Stencil schema refresh strategy. */
    public static final String SCHEMA_REGISTRY_STENCIL_REFRESH_STRATEGY_KEY = "SCHEMA_REGISTRY_STENCIL_REFRESH_STRATEGY";
    /** Default Stencil refresh strategy: {@code "LONG_POLLING"}. */
    public static final String SCHEMA_REGISTRY_STENCIL_REFRESH_STRATEGY_DEFAULT = "LONG_POLLING";
    /** Configuration key for the minimum back-off between Stencil fetch retries, in milliseconds. */
    public static final String SCHEMA_REGISTRY_STENCIL_FETCH_BACKOFF_MIN_MS_KEY = "SCHEMA_REGISTRY_STENCIL_FETCH_BACKOFF_MIN_MS";
    /** Default minimum Stencil fetch back-off: {@code 60000} ms (1 minute). */
    public static final Long SCHEMA_REGISTRY_STENCIL_FETCH_BACKOFF_MIN_MS_DEFAULT = 60000L;
    /** Configuration key for the number of times a failed Stencil fetch is retried. */
    public static final String SCHEMA_REGISTRY_STENCIL_FETCH_RETRIES_KEY = "SCHEMA_REGISTRY_STENCIL_FETCH_RETRIES";
    /** Default number of Stencil fetch retries: {@code 4}. */
    public static final Integer SCHEMA_REGISTRY_STENCIL_FETCH_RETRIES_DEFAULT = 4;

    /** Metric group key under which user-defined function (UDF) telemetry is reported. */
    public static final String UDF_TELEMETRY_GROUP_KEY = "udf";
    /** Aspect/field name used when publishing gauge metric values. */
    public static final String GAUGE_ASPECT_NAME = "value";

    /** Length of the sliding time window used when aggregating/reporting metrics. */
    public static final long SLIDING_TIME_WINDOW = 10;
    /** Stream configuration key naming the protobuf class for an input stream's schema. */
    public static final String STREAM_INPUT_SCHEMA_PROTO_CLASS = "INPUT_SCHEMA_PROTO_CLASS";
    /** Stream configuration key naming the table/alias for an input stream. */
    public static final String STREAM_INPUT_SCHEMA_TABLE = "INPUT_SCHEMA_TABLE";
    /** Configuration key holding the definition of the job's input streams. */
    public static final String INPUT_STREAMS = "STREAMS";

    /** Name of the internal boolean column Dagger appends to flag whether a record parsed successfully. */
    public static final String INTERNAL_VALIDATION_FIELD_KEY = "__internal_validation_field__";
    /** Default name of the event-time (rowtime) attribute column appended to every row. */
    public static final String ROWTIME = "rowtime";
}
