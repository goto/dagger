package com.gotocompany.dagger.core.utils;

/**
 * Centralized constant definitions for the Dagger core module.
 *
 * <p>Holds the configuration keys (and their default values) that drive Dagger's pre/post
 * processors, Longbow BigTable lookups, Flink runtime tuning, Kafka/InfluxDB/BigQuery sinks,
 * Kafka source consumers, telemetry, and the internal identifiers used while building the job
 * pipeline. This is a constant holder and is not intended to be instantiated.
 */
public class Constants {

    /** Configuration key toggling the pre-processor stage. */
    public static final String PROCESSOR_PREPROCESSOR_ENABLE_KEY = "PROCESSOR_PREPROCESSOR_ENABLE";
    /** Default for {@link #PROCESSOR_PREPROCESSOR_ENABLE_KEY}: pre-processing disabled. */
    public static final boolean PROCESSOR_PREPROCESSOR_ENABLE_DEFAULT = false;
    /** Configuration key holding the JSON definition of the pre-processor chain. */
    public static final String PROCESSOR_PREPROCESSOR_CONFIG_KEY = "PROCESSOR_PREPROCESSOR_CONFIG";
    /** Configuration key toggling the post-processor stage. */
    public static final String PROCESSOR_POSTPROCESSOR_ENABLE_KEY = "PROCESSOR_POSTPROCESSOR_ENABLE";
    /** Default for {@link #PROCESSOR_POSTPROCESSOR_ENABLE_KEY}: post-processing disabled. */
    public static final boolean PROCESSOR_POSTPROCESSOR_ENABLE_DEFAULT = false;
    /** Configuration key holding the JSON definition of the post-processor chain. */
    public static final String PROCESSOR_POSTPROCESSOR_CONFIG_KEY = "PROCESSOR_POSTPROCESSOR_CONFIG";

    /** Column/configuration key naming the Longbow lookup duration. */
    public static final String LONGBOW_DURATION_KEY = "longbow_duration";
    /** Column key marking the latest bound of a Longbow range lookup. */
    public static final String LONGBOW_LATEST_KEY = "longbow_latest";
    /** Column key marking the earliest bound of a Longbow range lookup. */
    public static final String LONGBOW_EARLIEST_KEY = "longbow_earliest";
    /** Configuration key for how long Longbow documents are retained. */
    public static final String PROCESSOR_LONGBOW_DOCUMENT_DURATION_KEY = "PROCESSOR_LONGBOW_DOCUMENT_DURATION";
    /** Default for {@link #PROCESSOR_LONGBOW_DOCUMENT_DURATION_KEY}: {@code "90d"}. */
    public static final String PROCESSOR_LONGBOW_DOCUMENT_DURATION_DEFAULT = "90d";
    /** Delimiter used when composing Longbow row keys. */
    public static final String LONGBOW_DELIMITER = "#";
    /** Column key prefix identifying Longbow data columns. */
    public static final String LONGBOW_DATA_KEY = "longbow_data";
    /** Column key under which the serialized protobuf payload is stored in Longbow. */
    public static final String LONGBOW_PROTO_DATA_KEY = "proto_data";
    /** Configuration key for the GCP project hosting the Longbow BigTable instance. */
    public static final String PROCESSOR_LONGBOW_GCP_PROJECT_ID_KEY = "PROCESSOR_LONGBOW_GCP_PROJECT_ID";
    /** Default for {@link #PROCESSOR_LONGBOW_GCP_PROJECT_ID_KEY}: {@code "default-gcp-project"}. */
    public static final String PROCESSOR_LONGBOW_GCP_PROJECT_ID_DEFAULT = "default-gcp-project";
    /** Configuration key for the BigTable instance backing Longbow. */
    public static final String PROCESSOR_LONGBOW_GCP_INSTANCE_ID_KEY = "PROCESSOR_LONGBOW_GCP_INSTANCE_ID";
    /** Default for {@link #PROCESSOR_LONGBOW_GCP_INSTANCE_ID_KEY}: {@code "default-gcp-project"}. */
    public static final String PROCESSOR_LONGBOW_GCP_INSTANCE_ID_DEFAULT = "default-gcp-project";
    /** Configuration key for the BigTable table backing Longbow. */
    public static final String PROCESSOR_LONGBOW_GCP_TABLE_ID_KEY = "PROCESSOR_LONGBOW_GCP_TABLE_ID";
    /** Default BigTable column family used by Longbow: {@code "ts"}. */
    public static final String LONGBOW_COLUMN_FAMILY_DEFAULT = "ts";
    /** Default BigTable column qualifier used by Longbow: {@code "proto"}. */
    public static final String LONGBOW_QUALIFIER_DEFAULT = "proto";
    /** Default Longbow asynchronous lookup timeout: {@code 15000} ms. */
    public static final Long PROCESSOR_LONGBOW_ASYNC_TIMEOUT_DEFAULT = 15000L;
    /** Configuration key for the Longbow asynchronous lookup timeout, in milliseconds. */
    public static final String PROCESSOR_LONGBOW_ASYNC_TIMEOUT_KEY = "PROCESSOR_LONGBOW_ASYNC_TIMEOUT";
    /** Default capacity of the Longbow asynchronous lookup thread pool: {@code 30}. */
    public static final Integer PROCESSOR_LONGBOW_THREAD_CAPACITY_DEFAULT = 30;
    /** Configuration key for the Longbow asynchronous lookup thread-pool capacity. */
    public static final String PROCESSOR_LONGBOW_THREAD_CAPACITY_KEY = "PROCESSOR_LONGBOW_THREAD_CAPACITY";
    /** Configuration key carrying the Dagger/Flink job name. */
    public static final String DAGGER_NAME_KEY = "FLINK_JOB_ID";
    /** Default for {@link #DAGGER_NAME_KEY}: {@code "SQL Flink Job"}. */
    public static final String DAGGER_NAME_DEFAULT = "SQL Flink Job";
    /** Reserved field name carrying a record's event timestamp. */
    public static final String EVENT_TIMESTAMP = "event_timestamp";
    /** Suffix denoting a duration expressed in minutes. */
    public static final String MINUTE_UNIT = "m";
    /** Suffix denoting a duration expressed in hours. */
    public static final String HOUR_UNIT = "h";
    /** Suffix denoting a duration expressed in days. */
    public static final String DAY_UNIT = "d";
    /** Configuration key holding the Flink SQL query to execute. */
    public static final String FLINK_SQL_QUERY_KEY = "FLINK_SQL_QUERY";
    /** Default for {@link #FLINK_SQL_QUERY_KEY}: an empty query. */
    public static final String FLINK_SQL_QUERY_DEFAULT = "";

    /** Default Flink job parallelism: {@code 1}. */
    public static final int FLINK_PARALLELISM_DEFAULT = 1;
    /** Configuration key for the Flink job parallelism. */
    public static final String FLINK_PARALLELISM_KEY = "FLINK_PARALLELISM";
    /** Default maximum Flink parallelism (used when rescaling): {@code 50}. */
    public static final int FLINK_PARALLELISM_MAX_DEFAULT = 50;
    /** Configuration key for the maximum Flink parallelism used when rescaling. */
    public static final String FLINK_PARALLELISM_MAX_KEY = "FLINK_PARALLELISM_MAX";
    /** Default auto-watermark emission interval: {@code 10000} ms. */
    public static final int FLINK_WATERMARK_INTERVAL_MS_DEFAULT = 10000;
    /** Configuration key for the auto-watermark emission interval, in milliseconds. */
    public static final String FLINK_WATERMARK_INTERVAL_MS_KEY = "FLINK_WATERMARK_INTERVAL_MS";
    /** Default checkpointing interval: {@code 30000} ms. */
    public static final long FLINK_CHECKPOINT_INTERVAL_MS_DEFAULT = 30000;
    /** Configuration key for the checkpointing interval, in milliseconds. */
    public static final String FLINK_CHECKPOINT_INTERVAL_MS_KEY = "FLINK_CHECKPOINT_INTERVAL_MS";
    /** Default checkpoint completion timeout: {@code 900000} ms (15 minutes). */
    public static final long FLINK_CHECKPOINT_TIMEOUT_MS_DEFAULT = 900000;
    /** Configuration key for the checkpoint completion timeout, in milliseconds. */
    public static final String FLINK_CHECKPOINT_TIMEOUT_MS_KEY = "FLINK_CHECKPOINT_TIMEOUT_MS";
    /** Default minimum pause between checkpoints: {@code 5000} ms. */
    public static final long FLINK_CHECKPOINT_MIN_PAUSE_MS_DEFAULT = 5000;
    /** Configuration key for the minimum pause between checkpoints, in milliseconds. */
    public static final String FLINK_CHECKPOINT_MIN_PAUSE_MS_KEY = "FLINK_CHECKPOINT_MIN_PAUSE_MS";
    /** Default number of concurrent checkpoints: {@code 1}. */
    public static final int FLINK_CHECKPOINT_MAX_CONCURRENT_DEFAULT = 1;
    /** Configuration key for the maximum number of concurrent checkpoints. */
    public static final String FLINK_CHECKPOINT_MAX_CONCURRENT_KEY = "FLINK_CHECKPOINT_MAX_CONCURRENT";
    /** Default idle-state retention: {@code 10} minutes. */
    public static final int FLINK_RETENTION_IDLE_STATE_MINUTE_DEFAULT = 10;
    /** Configuration key for the idle-state retention time, in minutes. */
    public static final String FLINK_RETENTION_IDLE_STATE_MINUTE_KEY = "FLINK_RETENTION_IDLE_STATE_MINUTE";
    /** Default watermark delay (allowed lateness): {@code 10000} ms. */
    public static final long FLINK_WATERMARK_DELAY_MS_DEFAULT = 10000;
    /** Configuration key for the watermark delay (allowed lateness), in milliseconds. */
    public static final String FLINK_WATERMARK_DELAY_MS_KEY = "FLINK_WATERMARK_DELAY_MS";
    /** Default for {@link #FLINK_ROWTIME_ATTRIBUTE_NAME_KEY}: no rowtime attribute configured. */
    public static final String FLINK_ROWTIME_ATTRIBUTE_NAME_DEFAULT = "";
    /** Configuration key naming the rowtime (event-time) attribute column. */
    public static final String FLINK_ROWTIME_ATTRIBUTE_NAME_KEY = "FLINK_ROWTIME_ATTRIBUTE_NAME";
    /** Default for {@link #FLINK_WATERMARK_PER_PARTITION_ENABLE_KEY}: per-partition watermarks disabled. */
    public static final boolean FLINK_WATERMARK_PER_PARTITION_ENABLE_DEFAULT = false;
    /** Configuration key toggling per-partition watermark generation. */
    public static final String FLINK_WATERMARK_PER_PARTITION_ENABLE_KEY = "FLINK_WATERMARK_PER_PARTITION_ENABLE";
    /** Default for {@link #FLINK_JOB_ID_KEY}: {@code "SQL Flink job"}. */
    public static final String FLINK_JOB_ID_DEFAULT = "SQL Flink job";
    /** Configuration key carrying the Flink job identifier. */
    public static final String FLINK_JOB_ID_KEY = "FLINK_JOB_ID";

    /** Synchronizer configuration key for the target BigTable table id. */
    public static final String SYNCHRONIZER_BIGTABLE_TABLE_ID_KEY = "bigtable_table_id";
    /** Synchronizer configuration key for the input proto class name. */
    public static final String SYNCHRONIZER_INPUT_CLASSNAME_KEY = "input_class_name";
    /** Synchronizer configuration key identifying the Longbow read key. */
    public static final String SYNCHRONIZER_LONGBOW_READ_KEY = "longbow_read_key";

    /** Configuration key for the Kafka sink topic. */
    public static final String SINK_KAFKA_TOPIC_KEY = "SINK_KAFKA_TOPIC";
    /** Configuration key for the Kafka sink bootstrap brokers. */
    public static final String SINK_KAFKA_BROKERS_KEY = "SINK_KAFKA_BROKERS";
    /** Configuration key for the protobuf class used for the Kafka sink message key. */
    public static final String SINK_KAFKA_PROTO_KEY = "SINK_KAFKA_PROTO_KEY";
    /** Configuration key for the protobuf class used for the Kafka sink message value. */
    public static final String SINK_KAFKA_PROTO_MESSAGE_KEY = "SINK_KAFKA_PROTO_MESSAGE";
    /** Configuration key naming the Kafka sink stream. */
    public static final String SINK_KAFKA_STREAM_KEY = "SINK_KAFKA_STREAM";
    /** Configuration key holding the JSON schema for the Kafka sink. */
    public static final String SINK_KAFKA_JSON_SCHEMA_KEY = "SINK_KAFKA_JSON_SCHEMA";
    /** Configuration key selecting the Kafka sink data type, such as PROTO or JSON. */
    public static final String SINK_KAFKA_DATA_TYPE = "SINK_KAFKA_DATA_TYPE";
    /** Configuration key toggling large-message production tuning for the Kafka sink. */
    public static final String SINK_KAFKA_PRODUCE_LARGE_MESSAGE_ENABLE_KEY = "SINK_KAFKA_PRODUCE_LARGE_MESSAGE_ENABLE";
    /** Configuration key for the Kafka producer {@code linger.ms} on the sink. */
    public static final String SINK_KAFKA_LINGER_MS_KEY = "SINK_KAFKA_LINGER_MS";
    /** Default for {@link #SINK_KAFKA_PRODUCE_LARGE_MESSAGE_ENABLE_KEY}: large-message tuning disabled. */
    public static final boolean SINK_KAFKA_PRODUCE_LARGE_MESSAGE_ENABLE_DEFAULT = false;
    /** Kafka producer property name for the sink compression type. */
    public static final String SINK_KAFKA_COMPRESSION_TYPE_KEY = "compression.type";
    /** Kafka producer property name for the sink linger time. */
    public static final String SINK_KAFKA_LINGER_MS_CONFIG_KEY = "linger.ms";
    /** Default Kafka sink compression type: {@code "snappy"}. */
    public static final String SINK_KAFKA_COMPRESSION_TYPE_DEFAULT = "snappy";
    /** Kafka producer property name for the sink maximum request size. */
    public static final String SINK_KAFKA_MAX_REQUEST_SIZE_KEY = "max.request.size";
    /** Default Kafka sink maximum request size: {@code 20971520} bytes (20 MB). */
    public static final String SINK_KAFKA_MAX_REQUEST_SIZE_DEFAULT = "20971520";
    /** Default Kafka sink linger time: {@code "0"} ms. */
    public static final String SINK_KAFKA_LINGER_MS_DEFAULT = "0";

    /** External post-processor type identifier for Elasticsearch. */
    public static final String ES_TYPE = "ES";
    /** External post-processor type identifier for HTTP. */
    public static final String HTTP_TYPE = "HTTP";
    /** External post-processor type identifier for PostgreSQL. */
    public static final String PG_TYPE = "PG";
    /** External post-processor type identifier for gRPC. */
    public static final String GRPC_TYPE = "GRPC";
    /** Sentinel selecting all columns in an external post-processor SQL path. */
    public static final String SQL_PATH_SELECT_ALL_CONFIG_VALUE = "*";

    /** Internal key identifying the Longbow writer post-processor. */
    public static final String LONGBOW_WRITER_PROCESSOR_KEY = "longbow_writer_processor";
    /** Internal key identifying the Longbow reader post-processor. */
    public static final String LONGBOW_READER_PROCESSOR_KEY = "longbow_reader_processor";
    /** Internal key identifying the transform processor. */
    public static final String TRANSFORM_PROCESSOR_KEY = "transform_processor";
    /** Class name of the built-in SQL transformer. */
    public static final String SQL_TRANSFORMER_CLASS = "SQLTransformer";

    /** Stream configuration key for the index of the event-timestamp field in the input schema. */
    public static final String STREAM_INPUT_SCHEMA_EVENT_TIMESTAMP_FIELD_INDEX_KEY = "INPUT_SCHEMA_EVENT_TIMESTAMP_FIELD_INDEX";
    /** Stream configuration key listing the Kafka source topic names. */
    public static final String STREAM_SOURCE_KAFKA_TOPIC_NAMES_KEY = "SOURCE_KAFKA_TOPIC_NAMES";
    /** Stream configuration key naming the Kafka source stream. */
    public static final String STREAM_INPUT_STREAM_NAME_KEY = "SOURCE_KAFKA_NAME";

    /** Stream configuration key holding the source-details JSON. */
    public static final String STREAM_SOURCE_DETAILS_KEY = "SOURCE_DETAILS";
    /** Source-details key selecting the source type. */
    public static final String STREAM_SOURCE_DETAILS_SOURCE_TYPE_KEY = "SOURCE_TYPE";
    /** Source-type value for a bounded (batch) source. */
    public static final String STREAM_SOURCE_DETAILS_SOURCE_TYPE_BOUNDED = "BOUNDED";
    /** Source-type value for an unbounded (streaming) source. */
    public static final String STREAM_SOURCE_DETAILS_SOURCE_TYPE_UNBOUNDED = "UNBOUNDED";
    /** Source-details key selecting the source implementation by name. */
    public static final String STREAM_SOURCE_DETAILS_SOURCE_NAME_KEY = "SOURCE_NAME";
    /** Source-name value for the Flink Kafka source connector. */
    public static final String STREAM_SOURCE_DETAILS_SOURCE_NAME_KAFKA = "KAFKA_SOURCE";
    /** Source-name value for the Parquet file source connector. */
    public static final String STREAM_SOURCE_DETAILS_SOURCE_NAME_PARQUET = "PARQUET_SOURCE";
    /** Source-name value for the legacy Flink Kafka consumer. */
    public static final String STREAM_SOURCE_DETAILS_SOURCE_NAME_KAFKA_CONSUMER = "KAFKA_CONSUMER";
    /** Stream configuration key listing the Parquet source file paths. */
    public static final String STREAM_SOURCE_PARQUET_FILE_PATHS_KEY = "SOURCE_PARQUET_FILE_PATHS";
    /** Stream configuration key selecting the Parquet read-order strategy. */
    public static final String STREAM_SOURCE_PARQUET_READ_ORDER_STRATEGY_KEY = "SOURCE_PARQUET_READ_ORDER_STRATEGY";
    /** Parquet read-order value processing earliest-timestamp file URLs first. */
    public static final String STREAM_SOURCE_PARQUET_READ_ORDER_STRATEGY_EARLIEST_TIME_URL_FIRST = "EARLIEST_TIME_URL_FIRST";
    /** Parquet read-order value processing the earliest file index first. */
    public static final String STREAM_SOURCE_PARQUET_READ_ORDER_STRATEGY_EARLIEST_INDEX_FIRST = "EARLIEST_INDEX_FIRST";
    /** Stream configuration key selecting the Parquet schema-match strategy. */
    public static final String STREAM_SOURCE_PARQUET_SCHEMA_MATCH_STRATEGY_KEY = "SOURCE_PARQUET_SCHEMA_MATCH_STRATEGY";
    /** Stream configuration key restricting Parquet source files to a date range. */
    public static final String STREAM_SOURCE_PARQUET_FILE_DATE_RANGE_KEY = "SOURCE_PARQUET_FILE_DATE_RANGE";
    /** Parquet schema-match value requiring an identical schema, failing on mismatch. */
    public static final String STREAM_SOURCE_PARQUET_SAME_SCHEMA_MATCH_STRATEGY = "SAME_SCHEMA_WITH_FAIL_ON_MISMATCH";
    /** Parquet schema-match value allowing backward-compatible schemas, failing on type mismatch. */
    public static final String STREAM_SOURCE_PARQUET_BACKWARD_COMPATIBLE_SCHEMA_MATCH_STRATEGY = "BACKWARD_COMPATIBLE_SCHEMA_WITH_FAIL_ON_TYPE_MISMATCH";


    /** Stream configuration key selecting the input data type, such as PROTO or JSON. */
    public static final String STREAM_INPUT_DATATYPE = "INPUT_DATATYPE";
    /** Stream configuration key naming the event-timestamp field for JSON input. */
    public static final String STREAM_INPUT_SCHEMA_JSON_EVENT_TIMESTAMP_FIELD_NAME_KEY = "INPUT_SCHEMA_JSON_EVENT_TIMESTAMP_FIELD_NAME";
    /** Stream configuration key holding the JSON schema for JSON input. */
    public static final String STREAM_INPUT_SCHEMA_JSON_SCHEMA_KEY = "INPUT_SCHEMA_JSON_SCHEMA";

    /** Configuration key for the Kafka consumer {@code auto.offset.reset} policy. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_AUTO_OFFSET_RESET_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_AUTO_OFFSET_RESET";
    /** Default Kafka consumer offset-reset policy: {@code "latest"}. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_AUTO_OFFSET_RESET_DEFAULT = "latest";

    /** Configuration key for the Kafka consumer {@code enable.auto.commit} flag. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_AUTO_COMMIT_ENABLE_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_AUTO_COMMIT_ENABLE";
    /** Configuration key for the Kafka consumer group id. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_GROUP_ID_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_GROUP_ID";
    /** Configuration key for the Kafka consumer bootstrap servers. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_BOOTSTRAP_SERVERS_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_BOOTSTRAP_SERVERS";
    /** Configuration key for the Kafka consumer security protocol. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL";
    /** Configuration key for the Kafka consumer SASL mechanism. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SASL_MECHANISM_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SASL_MECHANISM";
    /** Configuration key for the Kafka consumer SASL JAAS configuration. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SASL_JAAS_CONFIG_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SASL_JAAS_CONFIG";
    /** Configuration key prefix for additional raw Kafka consumer properties. */
    public static final String SOURCE_KAFKA_CONSUMER_ADDITIONAL_CONFIGURATIONS = "SOURCE_KAFKA_CONSUMER_ADDITIONAL_CONFIGURATIONS";
    /** Configuration key for the Kafka consumer SSL key password. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEY_PASSWORD_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEY_PASSWORD";
    /** Configuration key for the Kafka consumer SSL keystore location. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_LOCATION_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_LOCATION";
    /** Configuration key for the Kafka consumer SSL keystore password. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_PASSWORD_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_PASSWORD";
    /** Configuration key for the Kafka consumer SSL keystore type. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_TYPE_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SSL_KEYSTORE_TYPE";
    /** Configuration key for the Kafka consumer SSL truststore location. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_LOCATION_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_LOCATION";
    /** Configuration key for the Kafka consumer SSL truststore password. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_PASSWORD_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_PASSWORD";
    /** Configuration key for the Kafka consumer SSL truststore type. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_TYPE_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SSL_TRUSTSTORE_TYPE";
    /** Configuration key for the Kafka consumer SSL protocol. */
    public static final String SOURCE_KAFKA_CONSUMER_CONFIG_SSL_PROTOCOL_KEY = "SOURCE_KAFKA_CONSUMER_CONFIG_SSL_PROTOCOL";

    /** Configuration key toggling metric telemetry reporting. */
    public static final String METRIC_TELEMETRY_ENABLE_KEY = "METRIC_TELEMETRY_ENABLE";
    /** Default for {@link #METRIC_TELEMETRY_ENABLE_KEY}: telemetry enabled. */
    public static final boolean METRIC_TELEMETRY_ENABLE_VALUE_DEFAULT = true;
    /** Configuration key for the telemetry shutdown grace period, in milliseconds. */
    public static final String METRIC_TELEMETRY_SHUTDOWN_PERIOD_MS_KEY = "METRIC_TELEMETRY_SHUTDOWN_PERIOD_MS";
    /** Default telemetry shutdown grace period: {@code 10000} ms. */
    public static final long METRIC_TELEMETRY_SHUTDOWN_PERIOD_MS_DEFAULT = 10000;
    /** Metric group key under which fatal exceptions are reported. */
    public static final String FATAL_EXCEPTION_METRIC_GROUP_KEY = "fatal.exception";
    /** Metric group key under which non-fatal exceptions are reported. */
    public static final String NONFATAL_EXCEPTION_METRIC_GROUP_KEY = "non.fatal.exception";

    /** Configuration key listing the UDF function-factory classes to register. */
    public static final String FUNCTION_FACTORY_CLASSES_KEY = "FUNCTION_FACTORY_CLASSES";
    /** Default for {@link #FUNCTION_FACTORY_CLASSES_KEY}: {@code "FunctionFactory"}. */
    public static final String FUNCTION_FACTORY_CLASSES_DEFAULT = "FunctionFactory";

    /** Metric key counting records dropped by the InfluxDB sink for being late. */
    public static final String SINK_INFLUX_LATE_RECORDS_DROPPED_KEY = "influx.late.records.dropped";
    /** Configuration key for the InfluxDB database name. */
    public static final String SINK_INFLUX_DB_NAME_KEY = "SINK_INFLUX_DB_NAME";
    /** Default for {@link #SINK_INFLUX_DB_NAME_KEY}: empty. */
    public static final String SINK_INFLUX_DB_NAME_DEFAULT = "";
    /** Configuration key for the InfluxDB retention policy. */
    public static final String SINK_INFLUX_RETENTION_POLICY_KEY = "SINK_INFLUX_RETENTION_POLICY";
    /** Default for {@link #SINK_INFLUX_RETENTION_POLICY_KEY}: empty. */
    public static final String SINK_INFLUX_RETENTION_POLICY_DEFAULT = "";
    /** Configuration key for the InfluxDB measurement name. */
    public static final String SINK_INFLUX_MEASUREMENT_NAME_KEY = "SINK_INFLUX_MEASUREMENT_NAME";
    /** Default for {@link #SINK_INFLUX_MEASUREMENT_NAME_KEY}: empty. */
    public static final String SINK_INFLUX_MEASUREMENT_NAME_DEFAULT = "";

    // A custom job can use this configuration to get all Influx measurement names as a list
    // and configure them in the job builder pipeline accordingly.
    // The initial design assumed custom job authors would know the sink targets and hardcode them.
    // If measurement names need to change, they can now be updated through configuration without changing the code.
    public static final String SINK_INFLUX_MEASUREMENTS_LIST_KEY = "SINK_INFLUX_MEASUREMENTS_LIST";

    // A custom job can use this configuration to get all Influx retention policies as a list
    // (positionally aligned with SINK_INFLUX_MEASUREMENTS_LIST_KEY) and configure them in the
    // job builder pipeline accordingly. This allows retention policies to be updated through
    // configuration without changing the code.
    public static final String SINK_INFLUX_RETENTION_POLICY_LIST_KEY = "SINK_INFLUX_RETENTION_POLICY_LIST";

    /** Configuration key for the InfluxDB connection URL. */
    public static final String SINK_INFLUX_URL_KEY = "SINK_INFLUX_URL";
    /** Default for {@link #SINK_INFLUX_URL_KEY}: empty. */
    public static final String SINK_INFLUX_URL_DEFAULT = "";
    /** Configuration key for the InfluxDB username. */
    public static final String SINK_INFLUX_USERNAME_KEY = "SINK_INFLUX_USERNAME";
    /** Default for {@link #SINK_INFLUX_USERNAME_KEY}: empty. */
    public static final String SINK_INFLUX_USERNAME_DEFAULT = "";
    /** Configuration key for the InfluxDB password. */
    public static final String SINK_INFLUX_PASSWORD_KEY = "SINK_INFLUX_PASSWORD";
    /** Default for {@link #SINK_INFLUX_PASSWORD_KEY}: empty. */
    public static final String SINK_INFLUX_PASSWORD_DEFAULT = "";
    /** Configuration key for the InfluxDB write batch size. */
    public static final String SINK_INFLUX_BATCH_SIZE_KEY = "SINK_INFLUX_BATCH_SIZE";
    /** Default InfluxDB batch size: {@code 0} (batching disabled). */
    public static final int SINK_INFLUX_BATCH_SIZE_DEFAULT = 0;
    /** Configuration key for the InfluxDB flush interval, in milliseconds. */
    public static final String SINK_INFLUX_FLUSH_DURATION_MS_KEY = "SINK_INFLUX_FLUSH_DURATION_MS";
    /** Default InfluxDB flush interval: {@code 0} (flush disabled). */
    public static final int SINK_INFLUX_FLUSH_DURATION_MS_DEFAULT = 0;
    /** Configuration key toggling the InfluxDB writer that uses row field names. */
    public static final String SINK_INFLUX_USING_ROW_FIELD_NAMES_KEY = "SINK_INFLUX_WITH_ROW_NAMES_WRITER";
    /** Default for {@link #SINK_INFLUX_USING_ROW_FIELD_NAMES_KEY}: field-name writer disabled. */
    public static final boolean SINK_INFLUX_USING_ROW_FIELD_NAMES_DEFAULT = false;

    /** Configuration key toggling large-message consumption tuning for the Kafka source. */
    public static final String SOURCE_KAFKA_CONSUME_LARGE_MESSAGE_ENABLE_KEY = "SOURCE_KAFKA_CONSUME_LARGE_MESSAGE_ENABLE";
    /** Default for {@link #SOURCE_KAFKA_CONSUME_LARGE_MESSAGE_ENABLE_KEY}: large-message tuning disabled. */
    public static final boolean SOURCE_KAFKA_CONSUME_LARGE_MESSAGE_ENABLE_DEFAULT = false;
    /** Kafka consumer property name for the maximum bytes fetched per partition. */
    public static final String SOURCE_KAFKA_MAX_PARTITION_FETCH_BYTES_KEY = "max.partition.fetch.bytes";
    /** Default Kafka maximum partition fetch size: {@code 5242880} bytes (5 MB). */
    public static final String SOURCE_KAFKA_MAX_PARTITION_FETCH_BYTES_DEFAULT = "5242880";

    /** Lower bound of the HTTP client-error status range: {@code 400}. */
    public static final int CLIENT_ERROR_MIN_STATUS_CODE = 400;
    /** Upper bound of the HTTP client-error status range: {@code 499}. */
    public static final int CLIENT_ERROR_MAX_STATUS_CODE = 499;
    /** Lower bound of the HTTP server-error status range: {@code 500}. */
    public static final int SERVER_ERROR_MIN_STATUS_CODE = 500;
    /** Upper bound of the HTTP server-error status range: {@code 599}. */
    public static final int SERVER_ERROR_MAX_STATUS_CODE = 599;

    /** Default maximum event-loop execution time for async I/O: {@code 10000} ms. */
    public static final long MAX_EVENT_LOOP_EXECUTE_TIME_DEFAULT = 10000;
    /** Number of extra columns Longbow appends to each output row: {@code 3}. */
    public static final int LONGBOW_OUTPUT_ADDITIONAL_ARITY = 3;
    /**
     * Categories of variables an external post-processor can extract from a record when building
     * an outbound request.
     */
    public enum ExternalPostProcessorVariableType { REQUEST_VARIABLES, HEADER_VARIABLES, QUERY_VARIABLES, ENDPOINT_VARIABLE };

    /** Configuration key for the BigQuery sink write batch size. */
    public static final String SINK_BIGQUERY_BATCH_SIZE = "SINK_BIGQUERY_BATCH_SIZE";
    /** Default BigQuery sink batch size: {@code 500}. */
    public static final int SINK_BIGQUERY_BATCH_SIZE_DEFAULT = 500;
    // Comma seperated error types
    public static final String SINK_ERROR_TYPES_FOR_FAILURE = "SINK_ERROR_TYPES_FOR_FAILURE";
    /** Default for {@link #SINK_ERROR_TYPES_FOR_FAILURE}: no error types treated as failures. */
    public static final String SINK_ERROR_TYPES_FOR_FAILURE_DEFAULT = "";

    /** Kafka consumer security protocols supported by Dagger. */
    public static final String[] SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SECURITY_PROTOCOL = {"SASL_PLAINTEXT", "SASL_SSL", "SSL"};
    /** Kafka consumer SASL mechanisms supported by Dagger. */
    public static final String[] SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SASL_MECHANISM = {"PLAIN", "SCRAM-SHA-256", "SCRAM-SHA-512"};

    /** Kafka consumer SSL protocols supported by Dagger. */
    public static final String[] SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SSL_PROTOCOL = {"TLS", "TLSv1.1", "TLSv1.2", "TLSv1.3", "SSL", "SSLv2", "SSLv3"};
    /** Kafka consumer SSL keystore/truststore file types supported by Dagger. */
    public static final String[] SUPPORTED_SOURCE_KAFKA_CONSUMER_CONFIG_SSL_STORE_FILE_TYPE = {"JKS", "PKCS12", "PEM"};
}
