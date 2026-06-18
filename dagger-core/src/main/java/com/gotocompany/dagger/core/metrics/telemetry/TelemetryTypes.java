package com.gotocompany.dagger.core.metrics.telemetry;

/**
 * The enum Telemetry types.
 */
public enum TelemetryTypes {
    /**
     * Telemetry tag identifying the Kafka input topic.
     */
    INPUT_TOPIC("input_topic"),
    /**
     * Telemetry tag identifying the input Protobuf message class.
     */
    INPUT_PROTO("input_proto"),
    /**
     * Telemetry tag identifying the input stream name.
     */
    INPUT_STREAM("input_stream"),
    /**
     * Telemetry tag identifying the configured sink type.
     */
    SINK_TYPE("sink_type"),
    /**
     * Telemetry tag identifying the Kafka output topic.
     */
    OUTPUT_TOPIC("output_topic"),
    /**
     * Telemetry tag identifying the output Protobuf message class.
     */
    OUTPUT_PROTO("output_proto"),
    /**
     * Telemetry tag identifying the output stream name.
     */
    OUTPUT_STREAM("output_stream"),
    /**
     * Telemetry tag identifying the post-processor type in use.
     */
    POST_PROCESSOR_TYPE("post_processor_type"),
    /**
     * Telemetry tag identifying the pre-processor type in use.
     */
    PRE_PROCESSOR_TYPE("pre_processor_type"),
    /**
     * Telemetry tag identifying the source metric id.
     */
    SOURCE_METRIC_ID("source_metricId");

    /**
     * Gets telemetry type value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * The string tag reported for this telemetry type.
     */
    private String value;

    /**
     * Instantiates a new telemetry type.
     *
     * @param value the string tag for this telemetry type
     */
    TelemetryTypes(String value) {
        this.value = value;
    }
}
