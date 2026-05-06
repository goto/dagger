package com.gotocompany.dagger.core.sink.influx;

import com.google.common.base.Strings;

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
public final class InfluxSinkOverrides {

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

    public static InfluxSinkOverrides withMeasurementName(String measurementName) {
        return new InfluxSinkOverrides(measurementName, null);
    }

    public static InfluxSinkOverrides withRetentionPolicy(String retentionPolicy) {
        return new InfluxSinkOverrides(null, retentionPolicy);
    }

    public static InfluxSinkOverrides of(String measurementName, String retentionPolicy) {
        return new InfluxSinkOverrides(measurementName, retentionPolicy);
    }

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

    public boolean hasMeasurementName() {
        return !Strings.isNullOrEmpty(measurementName);
    }

    public boolean hasRetentionPolicy() {
        return !Strings.isNullOrEmpty(retentionPolicy);
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(measurementName, retentionPolicy);
    }

    @Override
    public String toString() {
        return "InfluxSinkOverrides{measurementName='" + measurementName
                + "', retentionPolicy='" + retentionPolicy + "'}";
    }

    /** Fluent builder for {@link InfluxSinkOverrides}. */
    public static final class Builder {
        private String measurementName;
        private String retentionPolicy;

        private Builder() {
        }

        public Builder measurementName(String name) {
            this.measurementName = name;
            return this;
        }

        public Builder retentionPolicy(String policy) {
            this.retentionPolicy = policy;
            return this;
        }

        public InfluxSinkOverrides build() {
            if (measurementName == null && retentionPolicy == null) {
                return NONE;
            }
            return new InfluxSinkOverrides(measurementName, retentionPolicy);
        }
    }
}

