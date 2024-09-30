package com.gotocompany.dagger.core.sink.http;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.api.java.utils.ParameterTool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpSinkBuilderTest {

    private HttpSinkBuilder builder;

    @Mock
    private Configuration mockConfiguration;
    @Mock
    private StencilClientOrchestrator mockStencilClientOrchestrator;
    @Mock
    private DaggerStatsDReporter mockDaggerStatsDReporter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        builder = HttpSinkBuilder.create();
        when(mockConfiguration.getParam()).thenReturn(ParameterTool.fromArgs(new String[]{}));
    }

    @Test
    public void testCreate() {
        assertNotNull(builder);
    }

    @Test
    public void testSetConfiguration() {
        assertSame(builder, builder.setConfiguration(mockConfiguration));
    }

    @Test
    public void testSetColumnNames() {
        String[] columnNames = {"col1", "col2"};
        assertSame(builder, builder.setColumnNames(columnNames));
    }

    @Test
    public void testSetStencilClientOrchestrator() {
        assertSame(builder, builder.setStencilClientOrchestrator(mockStencilClientOrchestrator));
    }

    @Test
    public void testSetDaggerStatsDReporter() {
        assertSame(builder, builder.setDaggerStatsDReporter(mockDaggerStatsDReporter));
    }

    @Test
    public void testSetEndpoint() {
        assertSame(builder, builder.setEndpoint("http://example.com"));
    }

    @Test
    public void testSetMaxRetries() {
        assertSame(builder, builder.setMaxRetries(5));
    }

    @Test
    public void testSetRetryBackoffMs() {
        assertSame(builder, builder.setRetryBackoffMs(2000));
    }

    @Test
    public void testSetConnectTimeoutMs() {
        assertSame(builder, builder.setConnectTimeoutMs(10000));
    }

    @Test
    public void testSetReadTimeoutMs() {
        assertSame(builder, builder.setReadTimeoutMs(15000));
    }

    @Test
    public void testSetAuthType() {
        assertSame(builder, builder.setAuthType("Bearer"));
    }

    @Test
    public void testSetAuthToken() {
        assertSame(builder, builder.setAuthToken("token123"));
    }

    @Test
    public void testSetCompressionEnabled() {
        assertSame(builder, builder.setCompressionEnabled(true));
    }

    @Test
    public void testSetCompressionType() {
        assertSame(builder, builder.setCompressionType("deflate"));
    }

    @Test
    public void testSetBatchSize() {
        assertSame(builder, builder.setBatchSize(200));
    }

    @Test
    public void testSetFlushIntervalMs() {
        assertSame(builder, builder.setFlushIntervalMs(5000));
    }

    @Test
    public void testSetAsyncEnabled() {
        assertSame(builder, builder.setAsyncEnabled(false));
    }

    @Test
    public void testSetMaxConnections() {
        assertSame(builder, builder.setMaxConnections(20));
    }

    @Test
    public void testSetValidateSslCertificate() {
        assertSame(builder, builder.setValidateSslCertificate(false));
    }

    @Test
    public void testSetProxyHost() {
        assertSame(builder, builder.setProxyHost("proxy.example.com"));
    }

    @Test
    public void testSetProxyPort() {
        assertSame(builder, builder.setProxyPort(8080));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithoutEndpoint() {
        builder.setConfiguration(mockConfiguration).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidEndpoint() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("invalid-url")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidBatchSize() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setBatchSize(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidFlushInterval() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setFlushIntervalMs(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidMaxRetries() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setMaxRetries(-1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidRetryBackoff() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setRetryBackoffMs(-1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidConnectTimeout() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setConnectTimeoutMs(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidReadTimeout() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setReadTimeoutMs(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidMaxConnections() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setMaxConnections(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithInvalidProxyPort() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setProxyPort(70000)
                .build();
    }

    @Test
    public void testBuildWithValidConfiguration() {
        HttpSink sink = builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setDaggerStatsDReporter(mockDaggerStatsDReporter)
                .build();
        assertNotNull(sink);
    }

    @Test
    public void testDefaultValues() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com");
        HttpSink sink = builder.build();
        
        verify(mockConfiguration).getParam();
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_MAX_RETRIES), eq("3"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_RETRY_BACKOFF_MS), eq("1000"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_CONNECT_TIMEOUT_MS), eq("5000"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_READ_TIMEOUT_MS), eq("30000"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_AUTH_TYPE), eq("None"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_COMPRESSION_ENABLED), eq("false"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_COMPRESSION_TYPE), eq("gzip"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_BATCH_SIZE), eq("100"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_FLUSH_INTERVAL_MS), eq("1000"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_ASYNC_ENABLED), eq("true"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_MAX_CONNECTIONS), eq("10"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_VALIDATE_SSL_CERTIFICATE), eq("true"));
    }

    @Test
    public void testCustomValuesOverrideDefaults() {
        builder.setConfiguration(mockConfiguration)
                .setEndpoint("http://example.com")
                .setMaxRetries(5)
                .setRetryBackoffMs(2000)
                .setConnectTimeoutMs(10000)
                .setReadTimeoutMs(15000)
                .setAuthType("Bearer")
                .setCompressionEnabled(true)
                .setCompressionType("deflate")
                .setBatchSize(200)
                .setFlushIntervalMs(5000)
                .setAsyncEnabled(false)
                .setMaxConnections(20)
                .setValidateSslCertificate(false);

        HttpSink sink = builder.build();
        
        verify(mockConfiguration).getParam();
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_MAX_RETRIES), eq("5"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_RETRY_BACKOFF_MS), eq("2000"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_CONNECT_TIMEOUT_MS), eq("10000"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_READ_TIMEOUT_MS), eq("15000"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_AUTH_TYPE), eq("Bearer"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_COMPRESSION_ENABLED), eq("true"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_COMPRESSION_TYPE), eq("deflate"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_BATCH_SIZE), eq("200"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_FLUSH_INTERVAL_MS), eq("5000"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_ASYNC_ENABLED), eq("false"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_MAX_CONNECTIONS), eq("20"));
        verify(mockConfiguration.getParam()).put(eq(Constants.SINK_HTTP_VALIDATE_SSL_CERTIFICATE), eq("false"));
    }
}
