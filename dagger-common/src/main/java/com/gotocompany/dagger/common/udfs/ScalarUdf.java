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

    private GaugeStatsManager gaugeStatsManager;

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
