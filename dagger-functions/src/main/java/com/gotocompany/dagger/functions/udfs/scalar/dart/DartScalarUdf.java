package com.gotocompany.dagger.functions.udfs.scalar.dart;

import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.common.udfs.ScalarUdf;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.table.functions.FunctionContext;

import static com.gotocompany.dagger.common.core.Constants.UDF_TELEMETRY_GROUP_KEY;

/**
 * Base class for the "dart" family of Dagger scalar UDFs that enrich rows by looking up values in an
 * external object store (GCS, OSS or COS).
 *
 * <p>The concrete subclasses {@code DartGet} and {@code DartContains} are registered into Flink SQL
 * and, on every {@code eval(...)} call, resolve a key against a JSON "dart" file cached from the
 * store. This base class extends {@link ScalarUdf} to layer dart-specific telemetry on top of the
 * generic UDF presence gauge: it owns a {@link MeterStatsManager} configured with all
 * {@link DartAspects} so the subclasses can mark cache hits/misses and store fetch outcomes.
 */
public abstract class DartScalarUdf extends ScalarUdf {

    /**
     * Per-subtask meter manager used by dart UDFs to publish their {@link DartAspects} telemetry
     * (cache hits/misses, store fetch success/failure).
     *
     * <p>It is created and registered in {@link #open(FunctionContext)} and read through its
     * Lombok-generated getter; the generated setter is provided purely so tests can inject a mock
     * without a running Flink runtime.
     */
    @Getter
    @Setter // For testing purpose only
    private MeterStatsManager meterStatsManager;

    /**
     * {@inheritDoc}
     *
     * <p>After the base {@link ScalarUdf#open(FunctionContext)} sets up the generic UDF gauge, this
     * implementation builds a {@link MeterStatsManager} from the function's metric group and
     * registers every {@link DartAspects} constant under the UDF telemetry group, keyed by this
     * function's {@link #getName() name}. This makes the dart metric meters available before the
     * first lookup is evaluated.
     *
     * @param context the Flink function context, used to obtain the metric group
     * @throws Exception if the superclass initialization fails
     */
    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
        meterStatsManager = new MeterStatsManager(context.getMetricGroup(), true);
        meterStatsManager.register(UDF_TELEMETRY_GROUP_KEY, this.getName(), DartAspects.values());
    }
}
