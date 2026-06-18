package com.gotocompany.dagger.common.udfs;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import static com.gotocompany.dagger.common.core.Constants.GAUGE_ASPECT_NAME;
import static com.gotocompany.dagger.common.core.Constants.UDF_TELEMETRY_GROUP_KEY;

/**
 * Abstract class for Scalar udf.
 */
public abstract class ScalarUdf extends ScalarFunction {

    /** Telemetry helper used to register the UDF usage gauge against the Flink metric group. */
    private GaugeStatsManager gaugeStatsManager;

    /**
     * Initializes this scalar function and registers its UDF telemetry gauge.
     *
     * <p>Delegates to the superclass {@code open}, then builds a {@link GaugeStatsManager} from
     * the {@link FunctionContext} metric group and registers an integer gauge keyed by
     * {@code UDF_TELEMETRY_GROUP_KEY} so that usage of this UDF is observable in metrics.
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
     * Gets scalar udf name.
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

    // For testing purpose only
    public void setGaugeStatsManager(GaugeStatsManager gaugeStatsManager) {
        this.gaugeStatsManager = gaugeStatsManager;
    }
}
