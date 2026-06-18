package com.gotocompany.dagger.core.metrics.reporters.statsd.tags;

import org.apache.flink.util.Preconditions;

/**
 * Immutable representation of a single StatsD tag, either a bare {@code key} or a {@code key=value}
 * pair.
 *
 * <p>Dagger attaches these tags to every metric it publishes so measurements can be sliced by
 * dimensions such as component, job id, or exception type. The tag key is mandatory; a missing or
 * empty value is normalized to an internal sentinel so the tag renders as a bare key. Call
 * {@link #getFormattedTag()} to obtain the StatsD wire representation.
 */
public class StatsDTag {
    /** The tag key; guaranteed non-null and non-empty. */
    private final String tagKey;
    /** The tag value, or {@link #NIL_TAG_VALUE} when no meaningful value was supplied. */
    private final String tagValue;
    /** Sentinel marking the absence of a value, which causes the tag to format as a bare key. */
    private static final String NIL_TAG_VALUE = "NIL_TAG_VALUE";

    /**
     * Creates a tag with the given key and value.
     *
     * @param key   the tag key; must be non-null and non-empty
     * @param value the tag value; a {@code null} or empty value is treated as "no value", causing
     *              {@link #getFormattedTag()} to emit just the key
     * @throws IllegalArgumentException if {@code key} is {@code null} or empty
     */
    public StatsDTag(String key, String value) {
        Preconditions.checkArgument(key != null && !key.isEmpty(), "Tag key cannot be null or empty");
        this.tagKey = key;
        this.tagValue = (value != null && !value.isEmpty()) ? value : NIL_TAG_VALUE;
    }

    /**
     * Creates a value-less tag consisting of only a key.
     *
     * <p>The resulting tag is rendered as the bare key by {@link #getFormattedTag()}.
     *
     * @param tagName the tag key; must be non-null and non-empty
     * @throws IllegalArgumentException if {@code tagName} is {@code null} or empty
     */
    public StatsDTag(String tagName) {
        this(tagName, NIL_TAG_VALUE);
    }

    /**
     * Renders this tag in StatsD wire format.
     *
     * @return {@code key=value} when a value is present, or just {@code key} for a value-less tag
     */
    public String getFormattedTag() {
        if (tagValue.equals(NIL_TAG_VALUE)) {
            return tagKey;
        } else {
            return String.format("%s=%s", tagKey, tagValue);
        }
    }
}
