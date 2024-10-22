package com.gotocompany.dagger.functions.udfs.scalar.dart;

import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.common.udfs.ScalarUdf;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.table.functions.FunctionContext;

import static com.gotocompany.dagger.common.core.Constants.UDF_TELEMETRY_GROUP_KEY;

public abstract class DartScalarUdf extends ScalarUdf {

    @Getter
    @Setter // For testing purpose only
    private MeterStatsManager meterStatsManager;

    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
        meterStatsManager = new MeterStatsManager(context.getMetricGroup(), true);
        meterStatsManager.register(UDF_TELEMETRY_GROUP_KEY, this.getName(), DartAspects.values());
    }
}
