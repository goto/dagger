package com.gotocompany.dagger.common.udfs;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.table.functions.FunctionContext;

import static com.gotocompany.dagger.common.core.Constants.GAUGE_ASPECT_NAME;
import static com.gotocompany.dagger.common.core.Constants.UDF_TELEMETRY_GROUP_KEY;

/**
 * This class will not publish the UDF telemetry.
 * Because for AggregatedFunction it can not be done due to this bug in flink
 * ISSUE : https://issues.apache.org/jira/browse/FLINK-15040
 *
 * @param <T>   the type parameter
 * @param <ACC> the type parameter
 */
public abstract class AggregateUdf<T, ACC> extends AggregateFunction<T, ACC> {

    /** Telemetry helper used to register the UDF usage gauge against the Flink metric group. */
    private GaugeStatsManager gaugeStatsManager;

    /**
     * Initializes the aggregate function and attempts to register its UDF telemetry gauge.
     *
     * <p>Delegates to the superclass {@code open}, then builds a {@link GaugeStatsManager} from
     * the {@link FunctionContext} metric group and registers an integer gauge keyed by
     * {@code UDF_TELEMETRY_GROUP_KEY}. As noted on this class, the gauge is not actually published
     * for aggregate functions due to the referenced Flink issue.
     *
     * @param context the Flink function context exposing the runtime metric group
     * @throws Exception if the superclass {@code open} call fails
     */
    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
        gaugeStatsManager = new GaugeStatsManager(context.getMetricGroup(), true);
        gaugeStatsManager.registerInteger(UDF_TELEMETRY_GROUP_KEY, getName(), GAUGE_ASPECT_NAME, 1);
    }

    /**
     * Gets aggregate udf name.
     *
     * @return the name
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Gets gauge stats manager.
     *
     * @return the gauge stats manager
     */
    public GaugeStatsManager getGaugeStatsManager() {
        return gaugeStatsManager;
    }
}
