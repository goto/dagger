import com.gotocompany.depot.config.HttpSinkConfig;
import com.gotocompany.depot.Sink;
import com.gotocompany.depot.SinkResponse;
import com.gotocompany.depot.message.Message;
import com.gotocompany.depot.exception.SinkException;
import com.gotocompany.depot.http.HttpSink;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PendingRequestTest {

    @Mock
    private HttpSinkConfig config;

    @Mock
    private HttpSink httpSink;

    @Mock
    private SinkResponse sinkResponse;

    private MeterRegistry meterRegistry;
    private List<Message> messages;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        meterRegistry = new SimpleMeterRegistry();
        messages = new ArrayList<>();
        messages.add(new Message("key".getBytes(), "value".getBytes()));
        when(config.getMaxRetries()).thenReturn(3);
        when(config.getBackoffMultiplier()).thenReturn(1000L);
        when(config.getJitterFactor()).thenReturn(0.1);
        when(config.getCircuitBreakerResetTimeout()).thenReturn(60000L);
    }

    @Test
    public void shouldCreatePendingRequest() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertNotNull(request);
    }

    @Test
    public void shouldReturnFuture() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertNotNull(request.getFuture());
    }

    @Test
    public void shouldReturnMessages() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertEquals(messages, request.getMessages());
    }

    @Test
    public void shouldHaveUniqueRequestId() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertNotNull(request.getRequestId());
    }

    @Test
    public void shouldHaveEmptyMetadataInitially() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertTrue(request.getMetadata().isEmpty());
    }

    @Test
    public void shouldTrackElapsedTime() throws InterruptedException {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        Thread.sleep(100);
        assertTrue(request.getElapsedTime() >= 100);
    }

    @Test
    public void shouldExecuteSuccessfully() throws Exception {
        when(httpSink.pushToSink(messages)).thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        assertEquals(sinkResponse, request.getFuture().get(1, TimeUnit.SECONDS));
    }

    @Test
    public void shouldHandleExecutionFailure() throws Exception {
        when(httpSink.pushToSink(messages)).thenThrow(new SinkException("Test exception"));
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        assertTrue(request.getFuture().isCompletedExceptionally());
    }

    @Test
    public void shouldRetryOnFailure() throws Exception {
        when(httpSink.pushToSink(messages))
                .thenThrow(new SinkException("Test exception"))
                .thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        assertEquals(sinkResponse, request.getFuture().get(5, TimeUnit.SECONDS));
    }

    @Test
    public void shouldFailAfterMaxRetries() throws Exception {
        when(httpSink.pushToSink(messages)).thenThrow(new SinkException("Test exception"));
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        assertTrue(request.getFuture().isCompletedExceptionally());
    }

    @Test
    public void shouldCancelRequest() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.cancel();
        assertTrue(request.isCancelled());
    }

    @Test
    public void shouldReportCancelledStatus() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertFalse(request.isCancelled());
        request.cancel();
        assertTrue(request.isCancelled());
    }

    @Test
    public void shouldReportDoneStatus() throws Exception {
        when(httpSink.pushToSink(messages)).thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertFalse(request.isDone());
        request.execute();
        request.getFuture().get(1, TimeUnit.SECONDS);
        assertTrue(request.isDone());
    }

    @Test
    public void shouldReportCorrectState() throws Exception {
        when(httpSink.pushToSink(messages)).thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertEquals(PendingRequest.RequestState.PENDING, request.getState());
        request.execute();
        request.getFuture().get(1, TimeUnit.SECONDS);
        assertEquals(PendingRequest.RequestState.COMPLETED, request.getState());
    }

    @Test
    public void shouldAddAndRetrieveContext() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.addContext("key", "value");
        assertEquals("value", request.getContext("key").orElse(null));
    }

    @Test
    public void shouldHandleNonexistentContext() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.addContext("key", "value");
        assertTrue(request.getContext("key").isPresent());
        assertFalse(request.getContext("nonexistent").isPresent());
    }

    @Test
    public void shouldRetrieveAllContext() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.addContext("key1", "value1");
        request.addContext("key2", "value2");
        assertEquals(2, request.getAllContext().size());
    }

    @Test
    public void shouldTrackRetryCount() throws Exception {
        when(httpSink.pushToSink(messages))
                .thenThrow(new SinkException("Test exception"))
                .thenThrow(new SinkException("Test exception"))
                .thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        request.getFuture().get(5, TimeUnit.SECONDS);
        assertEquals(2, request.getRetryCount());
    }

    @Test
    public void shouldAllowForceRetry() throws Exception {
        when(httpSink.pushToSink(messages))
                .thenThrow(new SinkException("Test exception"))
                .thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        Thread.sleep(100);
        request.forceRetry();
        assertEquals(sinkResponse, request.getFuture().get(5, TimeUnit.SECONDS));
    }

    @Test
    public void shouldProvideStringRepresentation() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        assertTrue(request.toString().contains("requestId"));
        assertTrue(request.toString().contains("state"));
        assertTrue(request.toString().contains("retryCount"));
        assertTrue(request.toString().contains("elapsedTime"));
    }

    @Test
    public void shouldSetAndGetRetryContext() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.setRetryContext("key", "value");
        assertEquals("value", request.getRetryContext("key"));
    }

    @Test
    public void shouldClearRetryContext() {
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.setRetryContext("key", "value");
        request.clearRetryContext();
        assertNull(request.getRetryContext("key"));
    }

    @Test
    public void shouldOpenCircuitBreaker() throws Exception {
        when(httpSink.pushToSink(messages)).thenThrow(new SinkException("Test exception"));
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        for (int i = 0; i < 5; i++) {
            request.execute();
            Thread.sleep(100);
        }
        assertTrue(request.getFuture().isCompletedExceptionally());
    }

    @Test
    public void shouldHalfOpenCircuitBreaker() throws Exception {
        when(config.getCircuitBreakerResetTimeout()).thenReturn(100L);
        when(httpSink.pushToSink(messages))
                .thenThrow(new SinkException("Test exception"))
                .thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        Thread.sleep(200);
        request.execute();
        assertEquals(sinkResponse, request.getFuture().get(1, TimeUnit.SECONDS));
    }

    @Test
    public void shouldUseExponentialBackoffStrategy() throws Exception {
        when(httpSink.pushToSink(messages))
                .thenThrow(new SinkException("Test exception"))
                .thenThrow(new SinkException("Test exception"))
                .thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        assertEquals(sinkResponse, request.getFuture().get(10, TimeUnit.SECONDS));
    }

    @Test
    public void shouldUseLinearBackoffStrategy() throws Exception {
        PendingRequest.Builder builder = new PendingRequest.Builder()
                .withConfig(config)
                .withMessages(messages)
                .withHttpSink(httpSink)
                .withMeterRegistry(meterRegistry)
                .withRetryStrategy(new PendingRequest.LinearBackoffStrategy());
        when(httpSink.pushToSink(messages))
                .thenThrow(new SinkException("Test exception"))
                .thenThrow(new SinkException("Test exception"))
                .thenReturn(sinkResponse);
        PendingRequest request = builder.build();
        request.execute();
        assertEquals(sinkResponse, request.getFuture().get(10, TimeUnit.SECONDS));
    }

    @Test
    public void shouldUseCustomRetryPredicate() throws Exception {
        PendingRequest.Builder builder = new PendingRequest.Builder()
                .withConfig(config)
                .withMessages(messages)
                .withHttpSink(httpSink)
                .withMeterRegistry(meterRegistry)
                .withRetryPredicate(throwable -> false);
        when(httpSink.pushToSink(messages)).thenThrow(new SinkException("Test exception"));
        PendingRequest request = builder.build();
        request.execute();
        assertTrue(request.getFuture().isCompletedExceptionally());
    }

    @Test
    public void shouldInvokeCompletionCallback() throws Exception {
        CompletableFuture<PendingRequest> callbackFuture = new CompletableFuture<>();
        PendingRequest.Builder builder = new PendingRequest.Builder()
                .withConfig(config)
                .withMessages(messages)
                .withHttpSink(httpSink)
                .withMeterRegistry(meterRegistry)
                .withOnCompletionCallback(callbackFuture::complete);
        when(httpSink.pushToSink(messages)).thenReturn(sinkResponse);
        PendingRequest request = builder.build();
        request.execute();
        assertEquals(request, callbackFuture.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void shouldRecordMetrics() throws Exception {
        when(httpSink.pushToSink(messages)).thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        request.getFuture().get(1, TimeUnit.SECONDS);
        assertTrue(meterRegistry.get("http.request.execution").timer().count() > 0);
        assertEquals(1, meterRegistry.get("http.request.success").counter().count());
    }

    @Test
    public void shouldRecordRetryMetrics() throws Exception {
        when(httpSink.pushToSink(messages))
                .thenThrow(new SinkException("Test exception"))
                .thenReturn(sinkResponse);
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        request.getFuture().get(5, TimeUnit.SECONDS);
        assertEquals(1, meterRegistry.get("http.request.retries").counter().count());
    }

    @Test
    public void shouldRecordFailureMetrics() throws Exception {
        when(httpSink.pushToSink(messages)).thenThrow(new SinkException("Test exception"));
        PendingRequest request = PendingRequest.create(config, messages, httpSink, meterRegistry);
        request.execute();
        Thread.sleep(1000);
        assertEquals(1, meterRegistry.get("http.request.failure").counter().count());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenConfigMissing() {
        new PendingRequest.Builder()
                .withMessages(messages)
                .withHttpSink(httpSink)
                .withMeterRegistry(meterRegistry)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenMessagesMissing() {
        new PendingRequest.Builder()
                .withConfig(config)
                .withHttpSink(httpSink)
                .withMeterRegistry(meterRegistry)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenHttpSinkMissing() {
        new PendingRequest.Builder()
                .withConfig(config)
                .withMessages(messages)
                .withMeterRegistry(meterRegistry)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenMeterRegistryMissing() {
        new PendingRequest.Builder()
                .withConfig(config)
                .withMessages(messages)
                .withHttpSink(httpSink)
                .build();
    }

    @Test
    public void shouldBuildWithAllParameters() {
        PendingRequest request = new PendingRequest.Builder()
                .withConfig(config)
                .withMessages(messages)
                .withHttpSink(httpSink)
                .withMeterRegistry(meterRegistry)
                .withMetadata("key", "value")
                .withRetryStrategy(new PendingRequest.LinearBackoffStrategy())
                .withOnCompletionCallback(r -> {})
                .withRetryPredicate(t -> true)
                .build();
        assertNotNull(request);
    }
}
