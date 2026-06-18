package com.gotocompany.dagger.core.metrics.aspects;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;

/**
 * The enum Longbow writer aspects.
 */
public enum LongbowWriterAspects implements Aspects {
    /**
     * Counts successful BigTable table creations by the Longbow writer.
     */
    SUCCESS_ON_CREATE_BIGTABLE("success_on_create_bigtable", AspectType.Metric),
    /**
     * Records the response-time distribution for successful BigTable creations.
     */
    SUCCESS_ON_CREATE_BIGTABLE_RESPONSE_TIME("success_on_create_bigtable_response_time", AspectType.Histogram),
    /**
     * Counts failed BigTable table creations by the Longbow writer.
     */
    FAILURES_ON_CREATE_BIGTABLE("failed_on_create_bigtable", AspectType.Metric),
    /**
     * Records the response-time distribution for failed BigTable creations.
     */
    FAILURES_ON_CREATE_BIGTABLE_RESPONSE_TIME("failed_on_create_bigtable_response_time", AspectType.Histogram),
    /**
     * Counts the number of Longbow writer operations that timed out.
     */
    TIMEOUTS_ON_WRITER("timeouts_on_writer", AspectType.Metric),
    /**
     * Counts how often the Longbow writer connection is closed.
     */
    CLOSE_CONNECTION_ON_WRITER("close_connection_on_writer", AspectType.Metric),
    /**
     * Counts documents successfully written by the Longbow writer.
     */
    SUCCESS_ON_WRITE_DOCUMENT("success_on_write_document", AspectType.Metric),
    /**
     * Records the response-time distribution for successful Longbow write operations.
     */
    SUCCESS_ON_WRITE_DOCUMENT_RESPONSE_TIME("success_on_write_document_response_time", AspectType.Histogram),
    /**
     * Counts documents that failed to be written by the Longbow writer.
     */
    FAILED_ON_WRITE_DOCUMENT("failed_on_write_document", AspectType.Metric),
    /**
     * Records the response-time distribution for failed Longbow write operations.
     */
    FAILED_ON_WRITE_DOCUMENT_RESPONSE_TIME("failed_on_write_document_response_time", AspectType.Histogram);

    /**
     * The metric identifier reported for this aspect.
     */
    private String value;
    /**
     * The metric type this aspect represents.
     */
    private AspectType aspectType;

    /**
     * Instantiates a new Longbow writer aspect.
     *
     * @param value      the metric identifier for this aspect
     * @param aspectType the metric type this aspect represents
     */
    LongbowWriterAspects(String value, AspectType aspectType) {
        this.value = value;
        this.aspectType = aspectType;
    }

    /**
     * Returns the metric identifier reported for this aspect.
     *
     * @return the metric identifier configured for this aspect
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Returns the metric type associated with this aspect.
     *
     * @return the metric type configured for this aspect
     */
    @Override
    public AspectType getAspectType() {
        return aspectType;
    }
}
