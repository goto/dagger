package com.gotocompany.dagger.core.processors.telemetry;

import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.core.processors.PostProcessorConfig;
import com.gotocompany.dagger.core.processors.telemetry.processor.MetricsTelemetryExporter;
import com.gotocompany.dagger.core.processors.types.PostProcessor;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

/**
 * The Telemetry processor.
 */
public class TelemetryProcessor implements PostProcessor {
    /** The map function that records and exports telemetry metrics for the stream. */
    private MetricsTelemetryExporter metricsTelemetryExporter;

    /**
     * Instantiates a new Telemetry processor.
     *
     * @param metricsTelemetryExporter the metrics telemetry exporter
     */
    public TelemetryProcessor(MetricsTelemetryExporter metricsTelemetryExporter) {
        this.metricsTelemetryExporter = metricsTelemetryExporter;
    }

    /**
     * Attaches the telemetry exporter to the stream as a map function.
     *
     * <p>Records pass through unchanged; the exporter side-effects metric registration, while the
     * column names are preserved on the returned {@link StreamInfo}.
     *
     * @param inputStreamInfo the upstream stream together with its column names
     * @return a new {@link StreamInfo} wrapping the instrumented stream and the unchanged column names
     */
    @Override
    public StreamInfo process(StreamInfo inputStreamInfo) {
        DataStream<Row> resultStream = inputStreamInfo.getDataStream().map(metricsTelemetryExporter);
        return new StreamInfo(resultStream, inputStreamInfo.getColumnNames());
    }

    /**
     * Indicates whether this post processor is applicable to the supplied configuration.
     *
     * <p>Telemetry is always collected, so this processor applies to every configuration.
     *
     * @param postProcessorConfig the post processor configuration (ignored)
     * @return {@code true} always
     */
    @Override
    public boolean canProcess(PostProcessorConfig postProcessorConfig) {
        return true;
    }
}
