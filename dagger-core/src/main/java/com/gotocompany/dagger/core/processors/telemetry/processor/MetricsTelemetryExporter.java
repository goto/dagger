package com.gotocompany.dagger.core.processors.telemetry.processor;

import com.gotocompany.dagger.core.metrics.aspects.TelemetryAspects;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetryPublisher;
import com.gotocompany.dagger.core.metrics.telemetry.TelemetrySubscriber;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Metrics telemetry exporter.
 */
public class MetricsTelemetryExporter extends RichMapFunction<Row, Row> implements TelemetrySubscriber {
    /** Logger used to report the metrics registered with the underlying stats manager. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsTelemetryExporter.class.getName());
    /** Manages gauge metrics; lazily created from the runtime metric group when not injected. */
    private GaugeStatsManager gaugeStatsManager;
    /** Constant gauge value reported for every registered telemetry aspect. */
    private Integer gaugeValue = 1;
    /** Accumulated telemetry, mapping each metric group key to the set of values seen for it. */
    private Map<String, Set<String>> metrics = new HashMap<>();

    /**
     * Instantiates a new Metrics telemetry exporter with specified gauge stats manager.
     *
     * @param gaugeStatsManager the gauge stats manager
     */
    public MetricsTelemetryExporter(GaugeStatsManager gaugeStatsManager) {
        this.gaugeStatsManager = gaugeStatsManager;
    }

    /**
     * Instantiates a new Metrics telemetry exporter.
     */
    public MetricsTelemetryExporter() {
    }

    /**
     * Initialises the gauge stats manager and registers any pending metric groups.
     *
     * <p>Called by Flink when the function is opened. When no {@link GaugeStatsManager} was injected
     * one is created from the runtime metric group, and previously collected metrics are registered.
     *
     * @param parameters the Flink job/runtime configuration
     * @throws Exception if the underlying {@link RichMapFunction} initialisation fails
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        if (gaugeStatsManager == null) {
            gaugeStatsManager = new GaugeStatsManager(getRuntimeContext().getMetricGroup(), true);
        }
        if (metrics != null) {
            registerGroups(gaugeStatsManager);
        }
    }

    /**
     * Passes each record through unchanged.
     *
     * <p>This exporter only collects telemetry as a side effect of stream setup and does not modify
     * the data flowing through it.
     *
     * @param inputRow the incoming record
     * @return the same {@code inputRow}, unmodified
     * @throws Exception if record handling fails
     */
    @Override
    public Row map(Row inputRow) throws Exception {
        return inputRow;
    }

    /**
     * Receives telemetry from a publisher and registers the merged metrics.
     *
     * <p>Invoked when a subscribed {@link TelemetryPublisher} announces new telemetry; the values are
     * merged into the accumulated metrics and, when a stats manager is available, registered.
     *
     * @param publisher the publisher whose telemetry is merged into this exporter
     */
    @Override
    public void updated(TelemetryPublisher publisher) {
        mergeMetrics(publisher.getTelemetry());
        if (gaugeStatsManager != null) {
            registerGroups(gaugeStatsManager);
        }
    }

    /**
     * Merges telemetry from a publisher into the accumulated metric groups.
     *
     * <p>Each value is added to the set stored under its group key, creating the set on first use so
     * duplicate values are ignored.
     *
     * @param metricsFromPublisher the per-group metric values reported by a publisher
     */
    private void mergeMetrics(Map<String, List<String>> metricsFromPublisher) {
        metricsFromPublisher.forEach((key, value) -> {
                    metrics.computeIfAbsent(key, x -> new HashSet<>()).addAll(value);
                }
        );
    }

    /**
     * Register groups.
     *
     * @param manager the manager
     */
    protected void registerGroups(GaugeStatsManager manager) {
        metrics.forEach((groupKey, groupValues) -> groupValues
                .forEach(groupValue -> manager.registerAspects(groupKey, groupValue, TelemetryAspects.values(), gaugeValue)));
        LOGGER.info("Sending Metrics: " + metrics);
        metrics.clear();
    }
}
