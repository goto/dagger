package com.gotocompany.dagger.core.sink.http;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.depot.Sink;
import com.gotocompany.depot.http.HttpSinkFactory;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.metrics.groups.SinkWriterMetricGroup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpSinkTest {

    @Mock
    private Configuration configuration;
    @Mock
    private ProtoSerializer protoSerializer;
    @Mock
    private DaggerStatsDReporter daggerStatsDReporter;
    @Mock
    private HttpSinkFactory httpSinkFactory;
    @Mock
    private Sink depotSink;
    @Mock
    private SinkWriterMetricGroup metricGroup;

    private HttpSink httpSink;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        httpSink = new HttpSink(configuration, protoSerializer, httpSinkFactory, daggerStatsDReporter);
    }

    @Test
    public void testCreateWriter() throws Exception {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("SINK_HTTP_BATCH_SIZE", "50");
        configMap.put("SINK_ERROR_TYPES_FOR_FAILURE", "DESERIALIZATION,HTTP_ERROR");
        when(configuration.getParam()).thenReturn(ConfigOptions.fromMap(configMap));
        when(httpSinkFactory.create()).thenReturn(depotSink);

        SinkWriter<?, ?, ?> writer = httpSink.createWriter(new TestInitContext(), Collections.emptyList());

        assertNotNull(writer);
        assertTrue(writer instanceof HttpSinkWriter);
        verify(httpSinkFactory).init();
        verify(httpSinkFactory).create();
    }

    @Test
    public void testGetWriterStateSerializer() {
        assertFalse(httpSink.getWriterStateSerializer().isPresent());
    }

    @Test
    public void testCreateCommitter() throws Exception {
        assertFalse(httpSink.createCommitter().isPresent());
    }

    @Test
    public void testCreateGlobalCommitter() throws Exception {
        assertFalse(httpSink.createGlobalCommitter().isPresent());
    }

    @Test
    public void testGetCommittableSerializer() {
        assertFalse(httpSink.getCommittableSerializer().isPresent());
    }

    @Test
    public void testGetGlobalCommittableSerializer() {
        assertFalse(httpSink.getGlobalCommittableSerializer().isPresent());
    }

    private class TestInitContext implements HttpSink.InitContext {
        @Override
        public SinkWriterMetricGroup metricGroup() {
            return metricGroup;
        }
    }
}
