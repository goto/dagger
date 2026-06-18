package com.gotocompany.dagger.core.sink.influx;

import com.google.common.base.Strings;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable value object that carries optional overrides for an Influx sink:
 * <ul>
 *     <li>measurement name (overrides {@code SINK_INFLUX_MEASUREMENT_NAME})</li>
 *     <li>retention policy (overrides {@code SINK_INFLUX_RETENTION_POLICY})</li>
 * </ul>
 *
 * Either field may be {@code null} or blank, which means "fall back to the
 * value resolved from configuration". Using this object instead of multiple
 * positional {@code String} parameters keeps sink construction readable as
 * the number of overrides grows.
 *
 * <p>Typical usage from a custom job:
 * <pre>{@code
 * InfluxSinkOverrides overrides = InfluxSinkOverrides.builder()
 *         .measurementName(measurements[i])
 *         .retentionPolicy(retentionPolicies[i])
 *         .build();
 * Sink sink = sinkOrchestrator.getSink(configuration, columnNames,
 *         stencilClientOrchestrator, daggerStatsDReporter, influxOverrides);
 * }</pre>
 */
public final class InfluxSinkOverrides implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final InfluxSinkOverrides NONE = new InfluxSinkOverrides(null, null);

    private final String measurementName;
    private final String retentionPolicy;

    private InfluxSinkOverrides(String measurementName, String retentionPolicy) {
        this.measurementName = measurementName;
        this.retentionPolicy = retentionPolicy;
    }

    /** Returns an instance that applies no overrides (configuration values are used). */
    public static InfluxSinkOverrides none() {
        return NONE;
    }

    /**
     * Creates overrides that set only the measurement name.
     *
     * @param measurementName the measurement name to use; {@code null}/blank means fall back to configuration
     * @return overrides carrying the given measurement name and no retention-policy override
     */
    public static InfluxSinkOverrides withMeasurementName(String measurementName) {
        return new InfluxSinkOverrides(measurementName, null);
    }

    /**
     * Creates overrides that set only the retention policy.
     *
     * @param retentionPolicy the retention policy to use; {@code null}/blank means fall back to configuration
     * @return overrides carrying the given retention policy and no measurement-name override
     */
    public static InfluxSinkOverrides withRetentionPolicy(String retentionPolicy) {
        return new InfluxSinkOverrides(null, retentionPolicy);
    }

    /**
     * Creates overrides that set both the measurement name and the retention policy.
     *
     * @param measurementName the measurement name to use; {@code null}/blank means fall back to configuration
     * @param retentionPolicy the retention policy to use; {@code null}/blank means fall back to configuration
     * @return overrides carrying both values
     */
    public static InfluxSinkOverrides of(String measurementName, String retentionPolicy) {
        return new InfluxSinkOverrides(measurementName, retentionPolicy);
    }

    /**
     * Returns a fluent {@link Builder} for assembling overrides.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** May be {@code null} or empty, meaning "use the value from configuration". */
    public String getMeasurementName() {
        return measurementName;
    }

    /** May be {@code null} or empty, meaning "use the value from configuration". */
    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    /**
     * Indicates whether a usable measurement-name override is present.
     *
     * @return {@code true} if the measurement name is non-null and non-empty
     */
    public boolean hasMeasurementName() {
        return !Strings.isNullOrEmpty(measurementName);
    }

    /**
     * Indicates whether a usable retention-policy override is present.
     *
     * @return {@code true} if the retention policy is non-null and non-empty
     */
    public boolean hasRetentionPolicy() {
        return !Strings.isNullOrEmpty(retentionPolicy);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Two instances are equal when both the measurement name and the retention policy are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InfluxSinkOverrides)) {
            return false;
        }
        InfluxSinkOverrides that = (InfluxSinkOverrides) o;
        return Objects.equals(measurementName, that.measurementName)
                && Objects.equals(retentionPolicy, that.retentionPolicy);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Derived from the measurement name and the retention policy.
     */
    @Override
    public int hashCode() {
        return Objects.hash(measurementName, retentionPolicy);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Renders the measurement name and retention policy for debugging and logging.
     */
    @Override
    public String toString() {
        return "InfluxSinkOverrides{measurementName='" + measurementName
                + "', retentionPolicy='" + retentionPolicy + "'}";
    }

    /** Fluent builder for {@link InfluxSinkOverrides}. */
    public static final class Builder {
        private String measurementName;
        private String retentionPolicy;

        /**
         * Creates an empty builder; use {@link InfluxSinkOverrides#builder()} to obtain instances.
         */
        private Builder() {
        }

        /**
         * Sets the measurement-name override.
         *
         * @param name the measurement name; {@code null}/blank means fall back to configuration
         * @return this builder, for chaining
         */
        public Builder measurementName(String name) {
            this.measurementName = name;
            return this;
        }

        /**
         * Sets the retention-policy override.
         *
         * @param policy the retention policy; {@code null}/blank means fall back to configuration
         * @return this builder, for chaining
         */
        public Builder retentionPolicy(String policy) {
            this.retentionPolicy = policy;
            return this;
        }

        /**
         * Builds the overrides, returning the shared {@link InfluxSinkOverrides#none()} instance when
         * neither value was set.
         *
         * @return the assembled overrides
         */
        public InfluxSinkOverrides build() {
            if (measurementName == null && retentionPolicy == null) {
                return NONE;
            }
            return new InfluxSinkOverrides(measurementName, retentionPolicy);
        }
    }
}
