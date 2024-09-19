package com.gotocompany.dagger.core.sink.http;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.depot.config.HttpSinkConfig;
import com.gotocompany.depot.http.enums.HttpRequestMethodType;
import org.aeonbits.owner.ConfigFactory;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.api.connector.sink.Committer;
import org.apache.flink.api.connector.sink.GlobalCommitter;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncHttpSink implements Sink<Row, Void, Void, Void> {
    private final HttpSink httpSink;
    private final HttpSinkConfig httpSinkConfig;
    private final ExecutorService executorService;
    private final AsyncHttpClient httpClient;
    private final MetricsCollector metricsCollector;

    public AsyncHttpSink(Configuration configuration, ProtoSerializer protoSerializer, DaggerStatsDReporter daggerStatsDReporter) {
        this.httpSink = new HttpSink(configuration, protoSerializer, daggerStatsDReporter);
        this.httpSinkConfig = ConfigFactory.create(HttpSinkConfig.class, configuration.getParam().toMap());
        this.executorService = Executors.newFixedThreadPool(httpSinkConfig.getMaxConcurrentRequests());
        this.httpClient = AsyncHttpClient.getInstance();
        this.metricsCollector = new MetricsCollector(daggerStatsDReporter);
    }

    @Override
    public SinkWriter<Row, Void, Void> createWriter(InitContext context, List<Void> states) {
        SinkWriter<Row, Void, Void> httpSinkWriter = httpSink.createWriter(context, states);
        return new AsyncHttpSinkWriter(httpSinkWriter, executorService, httpClient, httpSinkConfig, metricsCollector);
    }

    @Override
    public Optional<SimpleVersionedSerializer<Void>> getWriterStateSerializer() {
        return Optional.empty();
    }

    @Override
    public Optional<Committer<Void>> createCommitter() throws IOException {
        return Optional.empty();
    }
    private static class AsyncHttpSinkWriter implements SinkWriter<Row, Void, Void> {
        private final SinkWriter<Row, Void, Void> httpSinkWriter;
        private final ExecutorService executorService;
        private final AsyncHttpClient httpClient;
        private final HttpSinkConfig httpSinkConfig;
        private final MetricsCollector metricsCollector;
        private final CircularBuffer<CompletableFuture<Void>> pendingWrites;

        public AsyncHttpSinkWriter(SinkWriter<Row, Void, Void> httpSinkWriter, ExecutorService executorService,
                                   AsyncHttpClient httpClient, HttpSinkConfig httpSinkConfig, MetricsCollector metricsCollector) {
            this.httpSinkWriter = httpSinkWriter;
            this.executorService = executorService;
            this.httpClient = httpClient;
            this.httpSinkConfig = httpSinkConfig;
            this.metricsCollector = metricsCollector;
            this.pendingWrites = new CircularBuffer<>(httpSinkConfig.getMaxConcurrentRequests());
        }

        @Override
        public void write(Row element, Context context) throws IOException, InterruptedException {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    httpSinkWriter.write(element, context);
                    metricsCollector.recordWriteTime(System.currentTimeMillis() - startTime);
                } catch (Exception e) {
                    metricsCollector.recordWriteError();
                    throw new RuntimeException(e);
                }
            }, executorService).thenCompose(v -> sendHttpRequest(element))
              .exceptionally(this::handleException);

            pendingWrites.add(future);

            if (pendingWrites.size() >= httpSinkConfig.getMaxConcurrentRequests()) {
                awaitAndClearPendingWrites();
            }
        }

        private CompletableFuture<Void> sendHttpRequest(Row element) {
            String data = convertRowToHttpData(element);
            HttpRequestMethodType method = httpSinkConfig.getSinkHttpRequestMethod();
            Map<String, String> headers = httpSinkConfig.getSinkHttpHeaders();
            return httpClient.sendAsync(httpSinkConfig.getSinkHttpServiceUrl(), method, headers, data);
        }

        private String convertRowToHttpData(Row element) {
            return element.toString();
        }

        private Void handleException(Throwable throwable) {
            metricsCollector.recordHttpError();
            return null;
        }

        private void awaitAndClearPendingWrites() {
            CompletableFuture.allOf(pendingWrites.toArray(new CompletableFuture[0])).join();
            pendingWrites.clear();
        }

        @Override
        public List<Void> prepareCommit(boolean flush) throws IOException, InterruptedException {
            awaitAndClearPendingWrites();
            return httpSinkWriter.prepareCommit(flush);
        }

        @Override
        public List<Void> snapshotState() throws IOException {
            return httpSinkWriter.snapshotState();
        }

        @Override
        public void close() throws Exception {
            awaitAndClearPendingWrites();
            httpSinkWriter.close();
        }
    }
}
