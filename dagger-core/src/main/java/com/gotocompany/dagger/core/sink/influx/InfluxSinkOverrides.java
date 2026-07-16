package com.gotocompany.dagger.core.sink.influx;

import com.google.common.base.Strings;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable value object that carries optional overrides for an Influx sink:
 * <ul>
 *     <li>measurement name (overrides {@code SINK_INFLUX_MEASUREMENT_NAME})</li>
 *     <li>retention policy (overrides {@code SINK_INFLUX_RETENTION_POLICY})</li>
 *     <li>database name (overrides {@code SINK_INFLUX_DB_NAME})</li>
 * </ul>
 *
 * Any field may be {@code null} or blank, which means "fall back to the
 * value resolved from configuration". Using this object instead of multiple
 * positional {@code String} parameters keeps sink construction readable as
 * the number of overrides grows.
 *
 * <p>Typical usage from a custom job:
 * <pre>{@code
 * InfluxSinkOverrides overrides = InfluxSinkOverrides.builder()
 *         .measurementName(measurements[i])
 *         .retentionPolicy(retentionPolicies[i])
 *         .databaseName(databaseNames[i])
 *         .build();
 * Sink sink = sinkOrchestrator.getSink(configuration, columnNames,
 *         stencilClientOrchestrator, daggerStatsDReporter, influxOverrides);
 * }</pre>
 */
public final class InfluxSinkOverrides implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final InfluxSinkOverrides NONE = new InfluxSinkOverrides(null, null, null);

    private final String measurementName;
    private final String retentionPolicy;
    private final String databaseName;

    private InfluxSinkOverrides(String measurementName, String retentionPolicy, String databaseName) {
        this.measurementName = measurementName;
        this.retentionPolicy = retentionPolicy;
        this.databaseName = databaseName;
    }

    /** Returns an instance that applies no overrides (configuration values are used). */
    public static InfluxSinkOverrides none() {
        return NONE;
    }

    public static InfluxSinkOverrides withMeasurementName(String measurementName) {
        return new InfluxSinkOverrides(measurementName, null, null);
    }

    public static InfluxSinkOverrides withRetentionPolicy(String retentionPolicy) {
        return new InfluxSinkOverrides(null, retentionPolicy, null);
    }

    public static InfluxSinkOverrides withDatabaseName(String databaseName) {
        return new InfluxSinkOverrides(null, null, databaseName);
    }

    public static InfluxSinkOverrides of(String measurementName, String retentionPolicy, String databaseName) {
        return new InfluxSinkOverrides(measurementName, retentionPolicy, databaseName);
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

    /** May be {@code null} or empty, meaning "use the value from configuration". */
    public String getDatabaseName() {
        return databaseName;
    }

    public boolean hasMeasurementName() {
        return !Strings.isNullOrEmpty(measurementName);
    }

    public boolean hasRetentionPolicy() {
        return !Strings.isNullOrEmpty(retentionPolicy);
    }

    public boolean hasDatabaseName() {
        return !Strings.isNullOrEmpty(databaseName);
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
                && Objects.equals(retentionPolicy, that.retentionPolicy)
                && Objects.equals(databaseName, that.databaseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurementName, retentionPolicy, databaseName);
    }

    @Override
    public String toString() {
        return "InfluxSinkOverrides{measurementName='" + measurementName
                + "', retentionPolicy='" + retentionPolicy
                + "', databaseName='" + databaseName + "'}";
    }

    /** Fluent builder for {@link InfluxSinkOverrides}. */
    public static final class Builder {
        private String measurementName;
        private String retentionPolicy;
        private String databaseName;

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

        public Builder databaseName(String name) {
            this.databaseName = name;
            return this;
        }

        public InfluxSinkOverrides build() {
            if (measurementName == null && retentionPolicy == null && databaseName == null) {
                return NONE;
            }
            return new InfluxSinkOverrides(measurementName, retentionPolicy, databaseName);
        }
    }
}
