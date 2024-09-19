package com.gotocompany.dagger.core.sink.http;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporterFactory;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.depot.http.HttpSinkFactory;
import com.gotocompany.depot.config.HttpSinkConfig;
import com.gotocompany.depot.error.ErrorType;
import org.aeonbits.owner.ConfigFactory;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.api.connector.sink.Committer;
import org.apache.flink.api.connector.sink.GlobalCommitter;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HttpSink implements Sink<Row, Void, Void, Void> {
    private final ProtoSerializer protoSerializer;
    private final Configuration configuration;
    private final DaggerStatsDReporter daggerStatsDReporter;
    private transient HttpSinkFactory sinkFactory;

    public HttpSink(Configuration configuration, ProtoSerializer protoSerializer, DaggerStatsDReporter daggerStatsDReporter) {
        this(configuration, protoSerializer, null, daggerStatsDReporter);
    }

    protected HttpSink(Configuration configuration, ProtoSerializer protoSerializer, HttpSinkFactory sinkFactory, DaggerStatsDReporter daggerStatsDReporter) {
        this.configuration = configuration;
        this.protoSerializer = protoSerializer;
        this.sinkFactory = sinkFactory;
        this.daggerStatsDReporter = daggerStatsDReporter;
    }

    @Override
    public SinkWriter<Row, Void, Void> createWriter(InitContext context, List<Void> states) {
        ErrorReporter errorReporter = ErrorReporterFactory.getErrorReporter(context.metricGroup(), configuration);
        if (sinkFactory == null) {
            HttpSinkConfig sinkConfig = ConfigFactory.create(HttpSinkConfig.class, configuration.getParam().toMap());
            sinkFactory = new HttpSinkFactory(sinkConfig, daggerStatsDReporter.buildStatsDReporter());
            try {
                sinkFactory.init();
            } catch (Exception e) {
                errorReporter.reportFatalException(e);
                throw e;
            }
        }
        com.gotocompany.depot.Sink sink = sinkFactory.create();
        int batchSize = configuration.getInteger(
                Constants.SINK_HTTP_BATCH_SIZE,
                Constants.SINK_HTTP_BATCH_SIZE_DEFAULT);
        Set<ErrorType> errorTypesForFailing = getErrorTypesForFailing();
        return new HttpSinkWriter(protoSerializer, sink, batchSize, errorReporter, errorTypesForFailing);
    }

    private Set<ErrorType> getErrorTypesForFailing() {
        String errorsForFailing = configuration.getString(
                Constants.SINK_ERROR_TYPES_FOR_FAILURE,
                Constants.SINK_ERROR_TYPES_FOR_FAILURE_DEFAULT);
        Set<ErrorType> errorTypesForFailing = new HashSet<>();
        for (String s : errorsForFailing.split(",")) {
            errorTypesForFailing.add(ErrorType.valueOf(s.trim()));
        }
        return errorTypesForFailing;
    }

    @Override
    public Optional<SimpleVersionedSerializer<Void>> getWriterStateSerializer() {
        return Optional.empty();
    }

    @Override
    public Optional<Committer<Void>> createCommitter() throws IOException {
        return Optional.empty();
    }

    @Override
    public Optional<GlobalCommitter<Void, Void>> createGlobalCommitter() throws IOException {
        return Optional.empty();
    }

    @Override
    public Optional<SimpleVersionedSerializer<Void>> getCommittableSerializer() {
        return Optional.empty();
    }

    @Override
    public Optional<SimpleVersionedSerializer<Void>> getGlobalCommittableSerializer() {
        return Optional.empty();
    }
}
