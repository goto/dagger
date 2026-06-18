package com.gotocompany.dagger.core.metrics.reporters.statsd;

import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.GlobalTags;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.StatsDTag;
import com.gotocompany.depot.metrics.StatsDReporter;
import com.gotocompany.depot.metrics.StatsDReporterBuilder;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;
import java.util.Arrays;

import static com.gotocompany.dagger.core.utils.Constants.FLINK_JOB_ID_DEFAULT;
import static com.gotocompany.dagger.core.utils.Constants.FLINK_JOB_ID_KEY;

/**
 * Default {@link SerializedStatsDReporterSupplier} that lazily builds and shares a single depot
 * {@link StatsDReporter} for the whole Dagger job.
 *
 * <p>Because the supplier is serialized into the Flink job graph, only the two configuration objects
 * it needs are kept as fields; the actual reporter is created on demand in
 * {@link #buildStatsDReporter()}. The reporter is held in a {@code static} field so that every task
 * running in the same JVM/TaskManager reuses one instance, configured with a {@link DaggerMetricsConfig}
 * and the job's global tags (currently the Flink job id). Instances of this supplier are created
 * through the nested {@link Provider} factory.
 */
public class DaggerStatsDReporter implements SerializedStatsDReporterSupplier {
    /** Process-wide, lazily-initialized reporter shared by every task in the JVM. */
    private static StatsDReporter statsDReporter;
    /** Flink configuration used to resolve the StatsD host/port via {@link DaggerMetricsConfig}. */
    private final Configuration flinkConfiguration;
    /** Dagger configuration used to derive global tag values such as the job id. */
    private final com.gotocompany.dagger.common.configuration.Configuration daggerConfiguration;

    /**
     * Creates a supplier bound to the given Flink and Dagger configurations.
     *
     * @param flinkConfiguration  Flink configuration providing the StatsD host and port
     * @param daggerConfiguration Dagger configuration providing global tag values such as the job id
     */
    private DaggerStatsDReporter(Configuration flinkConfiguration, com.gotocompany.dagger.common.configuration.Configuration daggerConfiguration) {
        this.flinkConfiguration = flinkConfiguration;
        this.daggerConfiguration = daggerConfiguration;
    }

    /**
     * Builds the global tags attached to every metric emitted through the shared reporter.
     *
     * <p>Currently this is just the {@link GlobalTags#JOB_ID} tag, populated from the Dagger
     * configuration (falling back to {@code FLINK_JOB_ID_DEFAULT} when unset) and rendered into the
     * StatsD {@code key=value} form via {@link StatsDTag#getFormattedTag()}.
     *
     * @return the formatted global tag strings to register as extra tags on the reporter
     */
    private String[] generateGlobalTags() {
        StatsDTag[] globalTags = new StatsDTag[]{
                new StatsDTag(GlobalTags.JOB_ID, daggerConfiguration.getString(FLINK_JOB_ID_KEY, FLINK_JOB_ID_DEFAULT))};
        return Arrays.stream(globalTags)
                .map(StatsDTag::getFormattedTag)
                .toArray(String[]::new);
    }

    /**
     * {@inheritDoc}
     *
     * <p>On the first call this constructs the shared reporter from a {@link DaggerMetricsConfig} and
     * the {@linkplain #generateGlobalTags() global tags}; subsequent calls return the cached static
     * instance. The check is not synchronized, so concurrent first calls may briefly race to build
     * the reporter.
     *
     * @return the process-wide depot {@link StatsDReporter}
     */
    @Override
    public StatsDReporter buildStatsDReporter() {
        if (statsDReporter == null) {
            DaggerMetricsConfig daggerMetricsConfig = new DaggerMetricsConfig(flinkConfiguration);
            String[] globalTags = generateGlobalTags();
            statsDReporter = StatsDReporterBuilder
                    .builder()
                    .withMetricConfig(daggerMetricsConfig)
                    .withExtraTags(globalTags)
                    .build();
        }
        return statsDReporter;
    }

    /**
     * Closes the shared reporter and clears the cached instance so it can be rebuilt later.
     *
     * <p>Package/subclass-visible hook used to release the underlying StatsD resources, for example
     * during job teardown or between tests. Safe to call when no reporter has been built yet.
     *
     * @throws IOException if the underlying reporter fails to close
     */
    protected static void close() throws IOException {
        if (statsDReporter != null) {
            statsDReporter.close();
            statsDReporter = null;
        }
    }

    /**
     * Factory for {@link DaggerStatsDReporter} instances, exposing the otherwise private constructor.
     */
    public static class Provider {
        /**
         * Creates a new supplier for the given configurations.
         *
         * @param flinkConfiguration  Flink configuration providing the StatsD host and port
         * @param daggerConfiguration Dagger configuration providing global tag values such as the job id
         * @return a new {@link DaggerStatsDReporter}
         */
        public static DaggerStatsDReporter provide(Configuration flinkConfiguration, com.gotocompany.dagger.common.configuration.Configuration daggerConfiguration) {
            return new DaggerStatsDReporter(flinkConfiguration, daggerConfiguration);
        }
    }
}
