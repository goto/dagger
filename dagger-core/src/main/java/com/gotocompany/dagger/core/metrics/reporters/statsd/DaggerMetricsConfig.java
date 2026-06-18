package com.gotocompany.dagger.core.metrics.reporters.statsd;

import com.gotocompany.depot.config.MetricsConfig;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;

/**
 * {@link MetricsConfig} implementation that sources StatsD connection settings from the Flink job
 * {@link Configuration}.
 *
 * <p>Dagger publishes its metrics over StatsD through the depot reporter, which is configured by
 * this adapter. The StatsD host and port are read from the Flink configuration keys
 * {@code metrics.reporter.stsd.host} and {@code metrics.reporter.stsd.port}, falling back to
 * {@code localhost} and {@code 8125} respectively when they are not set. {@link #getMetricStatsDTags()}
 * returns an empty string because Dagger's global/extra tags are supplied separately when the
 * reporter is built.
 *
 * @see DaggerStatsDReporter
 */
public class DaggerMetricsConfig implements MetricsConfig {
    /** Flink configuration key holding the StatsD host name. */
    private static final String FLINK_STATSD_HOST_CONFIG_KEY = "metrics.reporter.stsd.host";
    /** Host used when the StatsD host key is absent from the Flink configuration. */
    private static final String DEFAULT_STATSD_HOST_VALUE = "localhost";
    /** Flink configuration key holding the StatsD port. */
    private static final String FLINK_STATSD_PORT_CONFIG_KEY = "metrics.reporter.stsd.port";
    /** Port used when the StatsD port key is absent from the Flink configuration. */
    private static final int DEFAULT_STATSD_PORT_VALUE = 8125;
    /** Resolved StatsD host name. */
    private final String hostName;
    /** Resolved StatsD port. */
    private final int port;

    /**
     * Resolves the StatsD host and port from the supplied Flink configuration, applying the defaults
     * when the keys are missing.
     *
     * @param flinkConfiguration the Flink job configuration to read the StatsD host and port from
     */
    public DaggerMetricsConfig(Configuration flinkConfiguration) {
        ConfigOption<String> hostConfigOption = ConfigOptions
                .key(FLINK_STATSD_HOST_CONFIG_KEY)
                .stringType()
                .defaultValue(DEFAULT_STATSD_HOST_VALUE);
        ConfigOption<Integer> portConfigOption = ConfigOptions
                .key(FLINK_STATSD_PORT_CONFIG_KEY)
                .intType()
                .defaultValue(DEFAULT_STATSD_PORT_VALUE);
        this.hostName = flinkConfiguration.getString(hostConfigOption);
        this.port = flinkConfiguration.getInteger(portConfigOption);
    }

    /**
     * Returns the StatsD host name to which metrics are sent.
     *
     * @return the configured host, or {@code localhost} when unset
     */
    @Override
    public String getMetricStatsDHost() {
        return hostName;
    }

    /**
     * Returns the StatsD port to which metrics are sent.
     *
     * @return the configured port, or {@code 8125} when unset
     */
    @Override
    public Integer getMetricStatsDPort() {
        return port;
    }

    /**
     * Returns the static tags configured directly on the metrics config.
     *
     * <p>Always an empty string for Dagger; global tags such as the job id are instead attached as
     * extra tags when the reporter is constructed in {@link DaggerStatsDReporter}.
     *
     * @return an empty string, indicating no statically-configured tags
     */
    @Override
    public String getMetricStatsDTags() {
        return "";
    }
}
