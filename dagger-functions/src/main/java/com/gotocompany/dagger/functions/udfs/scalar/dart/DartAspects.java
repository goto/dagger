package com.gotocompany.dagger.functions.udfs.scalar.dart;

import com.gotocompany.dagger.common.metrics.aspects.AspectType;
import com.gotocompany.dagger.common.metrics.aspects.Aspects;

import static com.gotocompany.dagger.common.metrics.aspects.AspectType.Gauge;
import static com.gotocompany.dagger.common.metrics.aspects.AspectType.Metric;

/**
 * The enum Dart aspects.
 */
public enum DartAspects implements Aspects {

    /**
     * Dart gcs path dart aspects.
     */
    DART_GCS_PATH("dart_bucket_path", Gauge),
    /**
     * Dart gcs fetch failures dart aspects.
     */
    DART_GCS_FETCH_FAILURES("dart_gcs_bucket_fetch_failure", Metric),
    /**
     * Dart gcs fetch success dart aspects.
     */
    DART_GCS_FETCH_SUCCESS("dart_gcs_bucket_fetch_success", Metric),
    /**
     * Dart cache hit dart aspects.
     */
    DART_CACHE_HIT("dart_cache_fetch_success", Metric),
    /**
     * Dart cache miss dart aspects.
     */
    DART_CACHE_MISS("dart_cache_fetch_failure", Metric),
    /**
     * Dart gcs file size dart aspects.
     */
    DART_GCS_FILE_SIZE("dart_gcs_file_size", Gauge);

    /**
     * The metric name reported to StatsD for this aspect.
     */
    private String value;
    /**
     * The {@link AspectType} that determines how this aspect is reported (e.g. as a gauge or a metric).
     */
    private AspectType aspectType;

    /**
     * Instantiates a new Dart aspect.
     *
     * @param value      the metric name reported to StatsD for this aspect
     * @param aspectType the type that controls how the aspect is published
     */
    DartAspects(String value, AspectType aspectType) {
        this.value = value;
        this.aspectType = aspectType;
    }

    /**
     * Returns the metric name associated with this Dart aspect.
     *
     * @return the metric name reported to StatsD
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Returns the reporting category for this Dart aspect.
     *
     * @return the {@link AspectType} describing how this aspect is published
     */
    @Override
    public AspectType getAspectType() {
        return aspectType;
    }

}
