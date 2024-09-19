package com.gotocompany.dagger.core.sink.http;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.depot.config.HttpSinkConfig;
import com.gotocompany.depot.http.enums.HttpRequestMethodType;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.types.Row;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AsyncHttpSinkTest {

    @Mock
    private Configuration configuration;
    @Mock
    private ProtoSerializer protoSerializer;
    @Mock
    private DaggerStatsDReporter daggerStatsDReporter;
    @Mock
    private HttpSinkConfig httpSinkConfig;
    @Mock
    private AsyncHttpClient asyncHttpClient;
    @Mock
    private HttpSink httpSink;

    private AsyncHttpSink asyncHttpSink;

    @Before
    public void setUp() {
        when(configuration.getParam()).thenReturn(new Configuration.Param(new HashMap<>()));
        asyncHttpSink = new AsyncHttpSink(configuration, protoSerializer, daggerStatsDReporter);
    }

    @Test
    public void testCreateWriter() {
        Sink.InitContext initContext = mock(Sink.InitContext.class);
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        when(httpSink.createWriter(any(), any())).thenReturn(mockWriter);

        SinkWriter<Row, Void, Void> writer = asyncHttpSink.createWriter(initContext, Collections.emptyList());

        assertNotNull(writer);
        assertTrue(writer instanceof AsyncHttpSink.AsyncHttpSinkWriter);
    }

    @Test
    public void testGetWriterStateSerializer() {
        when(httpSink.getWriterStateSerializer()).thenReturn(Optional.empty());

        Optional<SimpleVersionedSerializer<Void>> serializer = asyncHttpSink.getWriterStateSerializer();

        assertTrue(serializer.isEmpty());
    }

    @Test
    public void testCreateCommitter() throws IOException {
        when(httpSink.createCommitter()).thenReturn(Optional.empty());

        Optional<Committer<Void>> committer = asyncHttpSink.createCommitter();

        assertTrue(committer.isEmpty());
    }

    @Test
    public void testCreateGlobalCommitter() throws IOException {
        when(httpSink.createGlobalCommitter()).thenReturn(Optional.empty());

        Optional<GlobalCommitter<Void, Void>> globalCommitter = asyncHttpSink.createGlobalCommitter();

        assertTrue(globalCommitter.isEmpty());
    }

    @Test
    public void testGetCommittableSerializer() {
        when(httpSink.getCommittableSerializer()).thenReturn(Optional.empty());

        Optional<SimpleVersionedSerializer<Void>> serializer = asyncHttpSink.getCommittableSerializer();

        assertTrue(serializer.isEmpty());
    }

    @Test
    public void testGetGlobalCommittableSerializer() {
        when(httpSink.getGlobalCommittableSerializer()).thenReturn(Optional.empty());

        Optional<SimpleVersionedSerializer<Void>> serializer = asyncHttpSink.getGlobalCommittableSerializer();

        assertTrue(serializer.isEmpty());
    }

    @Test
    public void testAsyncHttpSinkWriterWrite() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        Row row = mock(Row.class);
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(1);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        writer.write(row, context);

        verify(mockWriter, times(1)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(1)).sendAsync(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAsyncHttpSinkWriterPrepareCommit() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        when(mockWriter.prepareCommit(anyBoolean())).thenReturn(Collections.emptyList());

        List<Void> result = writer.prepareCommit(true);

        verify(mockWriter, times(1)).prepareCommit(eq(true));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAsyncHttpSinkWriterSnapshotState() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        when(mockWriter.snapshotState()).thenReturn(Collections.emptyList());

        List<Void> result = writer.snapshotState();

        verify(mockWriter, times(1)).snapshotState();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAsyncHttpSinkWriterClose() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        writer.close();

        verify(mockWriter, times(1)).close();
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithMaxConcurrentRequests() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newFixedThreadPool(2), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        Row row = mock(Row.class);
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(2);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        writer.write(row, context);
        writer.write(row, context);
        writer.write(row, context);

        verify(mockWriter, times(3)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(3)).sendAsync(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithHttpError() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        MetricsCollector metricsCollector = mock(MetricsCollector.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, metricsCollector);

        Row row = mock(Row.class);
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(1);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("HTTP Error")));

        writer.write(row, context);

        verify(mockWriter, times(1)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(1)).sendAsync(anyString(), any(), any(), anyString());
        verify(metricsCollector, times(1)).recordHttpError();
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithDifferentHttpMethods() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        Row row = mock(Row.class);
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(1);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        for (HttpRequestMethodType method : HttpRequestMethodType.values()) {
            when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(method);
            writer.write(row, context);
        }

        verify(mockWriter, times(HttpRequestMethodType.values().length)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(HttpRequestMethodType.values().length)).sendAsync(anyString(), any(), any(), anyString());
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithCustomHeaders() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        Row row = mock(Row.class);
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("X-Custom-Header", "CustomValue");

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(1);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(customHeaders);
        when(asyncHttpClient.sendAsync(anyString(), any(), eq(customHeaders), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        writer.write(row, context);

        verify(mockWriter, times(1)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(1)).sendAsync(anyString(), any(), eq(customHeaders), anyString());
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithRetry() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        MetricsCollector metricsCollector = mock(MetricsCollector.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, metricsCollector);

        Row row = mock(Row.class);
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(1);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("HTTP Error")))
                .thenReturn(CompletableFuture.completedFuture(null));

        writer.write(row, context);

        verify(mockWriter, times(1)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(2)).sendAsync(anyString(), any(), any(), anyString());
        verify(metricsCollector, times(1)).recordHttpError();
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithMaxRetries() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        MetricsCollector metricsCollector = mock(MetricsCollector.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, metricsCollector);

        Row row = mock(Row.class);
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(1);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("HTTP Error")));

        writer.write(row, context);

        verify(mockWriter, times(1)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(3)).sendAsync(anyString(), any(), any(), anyString());
        verify(metricsCollector, times(3)).recordHttpError();
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithTimeout() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        MetricsCollector metricsCollector = mock(MetricsCollector.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, metricsCollector);

        Row row = mock(Row.class);
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(1);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(new CompletableFuture<>());  // This future will never complete

        writer.write(row, context);

        verify(mockWriter, times(1)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(1)).sendAsync(anyString(), any(), any(), anyString());
        verify(metricsCollector, times(1)).recordHttpError();
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithLargePayload() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newSingleThreadExecutor(), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        Row row = mock(Row.class);
        when(row.toString()).thenReturn(new String(new char[1000000]).replace('\0', 'A'));  // 1MB payload
        Sink.WriteContext context = mock(Sink.WriteContext.class);

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(1);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        writer.write(row, context);

        verify(mockWriter, times(1)).write(eq(row), eq(context));
        verify(asyncHttpClient, times(1)).sendAsync(anyString(), any(), any(), argThat(arg -> arg.length() == 1000000));
    }

    @Test
    public void testAsyncHttpSinkWriterWriteWithConcurrency() throws Exception {
        SinkWriter<Row, Void, Void> mockWriter = mock(SinkWriter.class);
        AsyncHttpSink.AsyncHttpSinkWriter writer = new AsyncHttpSink.AsyncHttpSinkWriter(
                mockWriter, Executors.newFixedThreadPool(10), asyncHttpClient, httpSinkConfig, new MetricsCollector(daggerStatsDReporter));

        when(httpSinkConfig.getMaxConcurrentRequests()).thenReturn(10);
        when(httpSinkConfig.getSinkHttpServiceUrl()).thenReturn("http://example.com");
        when(httpSinkConfig.getSinkHttpRequestMethod()).thenReturn(HttpRequestMethodType.POST);
        when(httpSinkConfig.getSinkHttpHeaders()).thenReturn(Collections.emptyMap());
        when(asyncHttpClient.sendAsync(anyString(), any(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        int numWrites = 100;
        CountDownLatch latch = new CountDownLatch(numWrites);
        for (int i = 0; i < numWrites; i++) {
            Row row = mock(Row.class);
            Sink.WriteContext context = mock(Sink.WriteContext.class);
            CompletableFuture.runAsync(() -> {
                try {
                    writer.write(row, context);
                } catch (Exception e) {
                    fail("Exception should not be thrown");
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        verify(mockWriter, times(numWrites)).write(any(), any());
        verify(asyncHttpClient, times(numWrites)).sendAsync(anyString(), any(), any(), anyString());
    }
}
