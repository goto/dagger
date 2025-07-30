package com.gotocompany.dagger.core.processors.longbow.processor;

import com.gotocompany.dagger.core.metrics.telemetry.TelemetryPublisher;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.types.Row;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LongbowTablestoreWriter extends RichAsyncFunction<Row, Row> implements TelemetryPublisher  {


    @Override
    public Map<String, List<String>> getTelemetry() {
        return Collections.emptyMap();
    }

    @Override
    public void asyncInvoke(Row input, ResultFuture<Row> resultFuture) throws Exception {

    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

    }
}
