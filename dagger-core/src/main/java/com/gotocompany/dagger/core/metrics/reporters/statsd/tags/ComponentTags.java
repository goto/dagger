package com.gotocompany.dagger.core.metrics.reporters.statsd.tags;

/**
 * Provides shared, lazily-initialized {@link StatsDTag} arrays that identify individual Dagger
 * pipeline components.
 *
 * <p>These component tags (all keyed by {@code component}) are attached to metrics emitted by the
 * corresponding subsystems so dashboards can attribute measurements to a specific component, such as
 * the Parquet file reader or the source split assigner. Each array is cached after first use and
 * reused on subsequent calls.
 */
public class ComponentTags {
    /** Cached tags marking the Parquet reader component; built on first access. */
    private static StatsDTag[] parquetReaderTags;
    /** Cached tags marking the split-assigner component; built on first access. */
    private static StatsDTag[] splitAssignerTags;
    /** Tag key shared by all component tags. */
    private static final String COMPONENT_TAG_KEY = "component";

    /**
     * Returns the tags identifying the Parquet reader component.
     *
     * <p>The array is created on the first call and cached for reuse.
     *
     * @return a single-element array tagging metrics with {@code component=parquet_reader}
     */
    public static StatsDTag[] getParquetReaderTags() {
        if (parquetReaderTags == null) {
            parquetReaderTags = new StatsDTag[]{
                    new StatsDTag(COMPONENT_TAG_KEY, "parquet_reader"),
            };
        }
        return parquetReaderTags;
    }

    /**
     * Returns the tags identifying the source split-assigner component.
     *
     * <p>The array is created on the first call and cached for reuse.
     *
     * @return a single-element array tagging metrics with {@code component=split_assigner}
     */
    public static StatsDTag[] getSplitAssignerTags() {
        if (splitAssignerTags == null) {
            splitAssignerTags = new StatsDTag[]{
                    new StatsDTag(COMPONENT_TAG_KEY, "split_assigner"),
            };
        }
        return splitAssignerTags;
    }
}
