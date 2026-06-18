package com.gotocompany.dagger.core.metrics.reporters.statsd.tags;

/**
 * Holder for global tag keys that are applied to every metric emitted by a Dagger job.
 *
 * <p>Unlike component-specific tags, these dimensions are attached to all measurements regardless of
 * which subsystem produces them. They are registered as extra tags when the shared StatsD reporter
 * is built in {@code DaggerStatsDReporter}.
 */
public class GlobalTags {
    /** Tag key carrying the Flink job id, used to distinguish metrics across jobs. */
    public static final String JOB_ID = "job_id";
}
