package com.gotocompany.dagger.core.metrics.aspects;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;

/**
 * The enum Longbow reader aspects.
 */
public enum LongbowReaderAspects implements Aspects {
    /**
     * Counts the number of Longbow reader operations that timed out.
     */
    TIMEOUTS_ON_READER("timeouts_on_reader", AspectType.Metric),
    /**
     * Counts how often the Longbow reader connection is closed.
     */
    CLOSE_CONNECTION_ON_READER("close_connection_on_reader", AspectType.Metric),
    /**
     * Counts documents successfully read by the Longbow reader.
     */
    SUCCESS_ON_READ_DOCUMENT("success_on_read_document", AspectType.Metric),
    /**
     * Records the response-time distribution for successful Longbow read operations.
     */
    SUCCESS_ON_READ_DOCUMENT_RESPONSE_TIME("success_on_read_document_response_time", AspectType.Histogram),
    /**
     * Records the distribution of documents read per Longbow scan.
     */
    DOCUMENTS_READ_PER_SCAN("documents_read_per_scan", AspectType.Histogram),
    /**
     * Counts documents that failed to be read by the Longbow reader.
     */
    FAILED_ON_READ_DOCUMENT("failed_on_read_document", AspectType.Metric),
    /**
     * Records the response-time distribution for failed Longbow read operations.
     */
    FAILED_ON_READ_DOCUMENT_RESPONSE_TIME("failed_on_read_document_response_time", AspectType.Histogram),
    /**
     * Counts failures to read the last record during Longbow lookups.
     */
    FAILED_TO_READ_LAST_RECORD("failed_to_read_last_record", AspectType.Metric);

    /**
     * The metric identifier reported for this aspect.
     */
    private String value;
    /**
     * The metric type this aspect represents.
     */
    private AspectType aspectType;

    /**
     * Instantiates a new Longbow reader aspect.
     *
     * @param value      the metric identifier for this aspect
     * @param aspectType the metric type this aspect represents
     */
    LongbowReaderAspects(String value, AspectType aspectType) {
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
