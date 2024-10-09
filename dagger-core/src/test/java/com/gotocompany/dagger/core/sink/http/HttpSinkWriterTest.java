import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.exception.HttpSinkWriterException;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.depot.Sink;
import com.gotocompany.depot.SinkResponse;
import com.gotocompany.depot.error.ErrorInfo;
import com.gotocompany.depot.error.ErrorType;
import com.gotocompany.depot.message.Message;
import org.apache.flink.types.Row;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpSinkWriterTest {

    @Mock
    private ProtoSerializer protoSerializer;
    @Mock
    private Sink httpSink;
    @Mock
    private ErrorReporter errorReporter;
    @Mock
    private SinkWriter.Context context;

    private HttpSinkWriter httpSinkWriter;
    private Set<ErrorType> errorTypesForFailing;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        errorTypesForFailing = new HashSet<>(Arrays.asList(ErrorType.SINK_4XX_ERROR, ErrorType.SINK_5XX_ERROR));
        httpSinkWriter = new HttpSinkWriter(protoSerializer, httpSink, 10, errorReporter, errorTypesForFailing);
    }

    @Test
    public void shouldWriteSuccessfully() throws Exception {
        Row row = mock(Row.class);
        when(protoSerializer.serializeKey(row)).thenReturn(new byte[]{1});
        when(protoSerializer.serializeValue(row)).thenReturn(new byte[]{2});

        httpSinkWriter.write(row, context);

        verify(protoSerializer).serializeKey(row);
        verify(protoSerializer).serializeValue(row);
    }

    @Test
    public void shouldHandleQueueFullScenario() throws Exception {
        Row row = mock(Row.class);
        when(protoSerializer.serializeKey(row)).thenReturn(new byte[]{1});
        when(protoSerializer.serializeValue(row)).thenReturn(new byte[]{2});

        for (int i = 0; i < 10001; i++) {
            httpSinkWriter.write(row, context);
        }

        verify(protoSerializer, times(10001)).serializeKey(row);
        verify(protoSerializer, times(10001)).serializeValue(row);
    }

    @Test
    public void shouldPrepareCommitWithFlush() throws Exception {
        httpSinkWriter.prepareCommit(true);
        verify(httpSink).pushToSink(anyList());
    }

    @Test
    public void shouldPrepareCommitWithoutFlush() throws Exception {
        httpSinkWriter.prepareCommit(false);
        verify(httpSink, never()).pushToSink(anyList());
    }

    @Test
    public void shouldSnapshotState() {
        List<Void> state = httpSinkWriter.snapshotState(123L);
        assertTrue(state.isEmpty());
    }

    @Test
    public void shouldClose() throws Exception {
        httpSinkWriter.close();
        verify(httpSink).close();
    }

    @Test
    public void shouldWriteWhenBatchSizeReached() throws Exception {
        Row row = mock(Row.class);
        when(protoSerializer.serializeKey(row)).thenReturn(new byte[]{1});
        when(protoSerializer.serializeValue(row)).thenReturn(new byte[]{2});

        for (int i = 0; i < 10; i++) {
            httpSinkWriter.write(row, context);
        }

        verify(httpSink).pushToSink(anyList());
    }

    @Test
    public void shouldHandleSerializationError() throws Exception {
        Row row = mock(Row.class);
        when(protoSerializer.serializeKey(row)).thenThrow(new RuntimeException("Serialization error"));

        assertThrows(RuntimeException.class, () -> httpSinkWriter.write(row, context));
    }

    @Test
    public void shouldPushToHttpSinkSuccessfully() throws Exception {
        List<Message> batch = Collections.singletonList(new Message(new byte[]{1}, new byte[]{2}));
        when(httpSink.pushToSink(batch)).thenReturn(new SinkResponse());

        httpSinkWriter.getClass().getDeclaredMethod("pushToHttpSink", List.class)
                .invoke(httpSinkWriter, batch);

        verify(httpSink).pushToSink(batch);
    }

    @Test
    public void shouldHandlePushToHttpSinkWithErrors() throws Exception {
        List<Message> batch = Arrays.asList(
                new Message(new byte[]{1}, new byte[]{2}),
                new Message(new byte[]{3}, new byte[]{4})
        );
        SinkResponse response = new SinkResponse();
        response.addErrors(0, new ErrorInfo(new Exception("Error 1"), ErrorType.SINK_4XX_ERROR));
        when(httpSink.pushToSink(batch)).thenReturn(response);

        assertThrows(HttpSinkWriterException.class, () -> {
            httpSinkWriter.getClass().getDeclaredMethod("pushToHttpSink", List.class)
                    .invoke(httpSinkWriter, batch);
        });
    }

    @Test
    public void shouldHandleNonFatalErrors() throws Exception {
        SinkResponse response = new SinkResponse();
        response.addErrors(0, new ErrorInfo(new Exception("Non-fatal error"), ErrorType.DESERIALIZATION_ERROR));
        List<Message> batch = Collections.singletonList(new Message(new byte[]{1}, new byte[]{2}));

        httpSinkWriter.getClass().getDeclaredMethod("handleErrors", SinkResponse.class, List.class)
                .invoke(httpSinkWriter, response, batch);

        verify(errorReporter).reportNonFatalException(any());
    }

    @Test
    public void shouldHandleFatalErrors() throws Exception {
        SinkResponse response = new SinkResponse();
        response.addErrors(0, new ErrorInfo(new Exception("Fatal error"), ErrorType.SINK_5XX_ERROR));
        List<Message> batch = Collections.singletonList(new Message(new byte[]{1}, new byte[]{2}));

        assertThrows(HttpSinkWriterException.class, () -> {
            httpSinkWriter.getClass().getDeclaredMethod("handleErrors", SinkResponse.class, List.class)
                    .invoke(httpSinkWriter, response, batch);
        });
    }

    @Test
    public void shouldLogErrors() throws Exception {
        SinkResponse response = new SinkResponse();
        response.addErrors(0, new ErrorInfo(new Exception("Test error"), ErrorType.SINK_4XX_ERROR));
        List<Message> batch = Collections.singletonList(new Message(new byte[]{1}, new byte[]{2}));

        httpSinkWriter.getClass().getDeclaredMethod("logErrors", SinkResponse.class, List.class)
                .invoke(httpSinkWriter, response, batch);
    }

    @Test
    public void shouldPartitionErrorsByFailureType() throws Exception {
        SinkResponse response = new SinkResponse();
        response.addErrors(0, new ErrorInfo(new Exception("Fatal error"), ErrorType.SINK_5XX_ERROR));
        response.addErrors(1, new ErrorInfo(new Exception("Non-fatal error"), ErrorType.DESERIALIZATION_ERROR));

        Map<Boolean, List<ErrorInfo>> result = (Map<Boolean, List<ErrorInfo>>) httpSinkWriter.getClass()
                .getDeclaredMethod("partitionErrorsByFailureType", SinkResponse.class)
                .invoke(httpSinkWriter, response);

        assertEquals(1, result.get(Boolean.TRUE).size());
        assertEquals(1, result.get(Boolean.FALSE).size());
    }

    @Test
    public void shouldIncrementTotalRowsReceived() throws Exception {
        HttpSinkWriter.HttpSinkWriterMetrics metrics = (HttpSinkWriter.HttpSinkWriterMetrics) httpSinkWriter
                .getClass().getDeclaredField("metrics").get(httpSinkWriter);

        metrics.incrementTotalRowsReceived();
        assertEquals(1, metrics.totalRowsReceived.get());
    }

    @Test
    public void shouldIncrementTotalErrors() throws Exception {
        HttpSinkWriter.HttpSinkWriterMetrics metrics = (HttpSinkWriter.HttpSinkWriterMetrics) httpSinkWriter
                .getClass().getDeclaredField("metrics").get(httpSinkWriter);

        metrics.incrementTotalErrors();
        assertEquals(1, metrics.totalErrors.get());
    }

    @Test
    public void shouldIncrementNonFatalErrors() throws Exception {
        HttpSinkWriter.HttpSinkWriterMetrics metrics = (HttpSinkWriter.HttpSinkWriterMetrics) httpSinkWriter
                .getClass().getDeclaredField("metrics").get(httpSinkWriter);

        metrics.incrementNonFatalErrors();
        assertEquals(1, metrics.nonFatalErrors.get());
    }

    @Test
    public void shouldIncrementFatalErrors() throws Exception {
        HttpSinkWriter.HttpSinkWriterMetrics metrics = (HttpSinkWriter.HttpSinkWriterMetrics) httpSinkWriter
                .getClass().getDeclaredField("metrics").get(httpSinkWriter);

        metrics.incrementFatalErrors();
        assertEquals(1, metrics.fatalErrors.get());
    }

    @Test
    public void shouldIncrementAsyncFlushErrors() throws Exception {
        HttpSinkWriter.HttpSinkWriterMetrics metrics = (HttpSinkWriter.HttpSinkWriterMetrics) httpSinkWriter
                .getClass().getDeclaredField("metrics").get(httpSinkWriter);

        metrics.incrementAsyncFlushErrors();
        assertEquals(1, metrics.asyncFlushErrors.get());
    }

    @Test
    public void shouldIncrementDroppedMessages() throws Exception {
        HttpSinkWriter.HttpSinkWriterMetrics metrics = (HttpSinkWriter.HttpSinkWriterMetrics) httpSinkWriter
                .getClass().getDeclaredField("metrics").get(httpSinkWriter);

        metrics.incrementDroppedMessages();
        assertEquals(1, metrics.droppedMessages.get());
    }

    @Test
    public void shouldStartBatchProcessing() throws Exception {
        HttpSinkWriter.HttpSinkWriterMetrics metrics = (HttpSinkWriter.HttpSinkWriterMetrics) httpSinkWriter
                .getClass().getDeclaredField("metrics").get(httpSinkWriter);

        metrics.startBatchProcessing(5);
        assertEquals(1, metrics.totalBatchesProcessed.get());
        assertEquals(5, metrics.totalRecordsSent.get());
    }

    @Test
    public void shouldEndBatchProcessing() throws Exception {
        HttpSinkWriter.HttpSinkWriterMetrics metrics = (HttpSinkWriter.HttpSinkWriterMetrics) httpSinkWriter
                .getClass().getDeclaredField("metrics").get(httpSinkWriter);

        metrics.startBatchProcessing(5);
        Thread.sleep(10);
        metrics.endBatchProcessing();
        assertTrue(metrics.totalProcessingTimeMs.get() > 0);
    }

    @Test
    public void shouldSetLastCheckpointId() throws Exception {
        HttpSinkWriter.HttpSinkWriterState state = (HttpSinkWriter.HttpSinkWriterState) httpSinkWriter
                .getClass().getDeclaredField("state").get(httpSinkWriter);

        state.setLastCheckpointId(123L);
        assertEquals(123L, state.lastCheckpointId);
    }

    @Test
    public void shouldSetLastCheckpointTimestamp() throws Exception {
        HttpSinkWriter.HttpSinkWriterState state = (HttpSinkWriter.HttpSinkWriterState) httpSinkWriter
                .getClass().getDeclaredField("state").get(httpSinkWriter);

        long timestamp = System.currentTimeMillis();
        state.setLastCheckpointTimestamp(timestamp);
        assertEquals(timestamp, state.lastCheckpointTimestamp);
    }

    @Test
    public void shouldInitializeCustomFieldExtractors() throws Exception {
        Map<String, Function<Row, Object>> extractors = (Map<String, Function<Row, Object>>) httpSinkWriter
                .getClass().getDeclaredMethod("initializeCustomFieldExtractors").invoke(httpSinkWriter);

        assertTrue(extractors.containsKey("timestamp"));
        assertTrue(extractors.containsKey("rowHash"));
    }

    @Test
    public void shouldEnrichAndSerializeValue() throws Exception {
        Row row = mock(Row.class);
        when(protoSerializer.serializeValue(row)).thenReturn(new byte[]{1, 2, 3});

        byte[] result = (byte[]) httpSinkWriter.getClass().getDeclaredMethod("enrichAndSerializeValue", Row.class)
                .invoke(httpSinkWriter, row);

        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    public void shouldInitializePeriodicFlush() throws Exception {
        httpSinkWriter.getClass().getDeclaredMethod("initializePeriodicFlush").invoke(httpSinkWriter);

        Thread.sleep(1100);
        verify(httpSink, atLeastOnce()).pushToSink(anyList());
    }

    @Test
    public void shouldFlushQueueAsync() throws Exception {
        httpSinkWriter.getClass().getDeclaredMethod("flushQueueAsync").invoke(httpSinkWriter);

        Thread.sleep(100);
        verify(httpSink, atLeastOnce()).pushToSink(anyList());
    }

    @Test
    public void shouldNotFlushEmptyQueue() throws Exception {
        httpSinkWriter.getClass().getDeclaredMethod("flushQueue").invoke(httpSinkWriter);

        verify(httpSink, never()).pushToSink(anyList());
    }

    @Test
    public void shouldFlushNonEmptyQueue() throws Exception {
        Row row = mock(Row.class);
        when(protoSerializer.serializeKey(row)).thenReturn(new byte[]{1});
        when(protoSerializer.serializeValue(row)).thenReturn(new byte[]{2});

        httpSinkWriter.write(row, context);

        httpSinkWriter.getClass().getDeclaredMethod("flushQueue").invoke(httpSinkWriter);

        verify(httpSink).pushToSink(anyList());
    }

    @Test
    public void shouldHandleInterruptedException() throws Exception {
        Row row = mock(Row.class);
        when(protoSerializer.serializeKey(row)).thenReturn(new byte[]{1});
        when(protoSerializer.serializeValue(row)).thenReturn(new byte[]{2});

        Thread.currentThread().interrupt();

        assertThrows(InterruptedException.class, () -> httpSinkWriter.write(row, context));

        Thread.interrupted();
    }

    @Test
    public void shouldShutdownExecutorServiceOnClose() throws Exception {
        httpSinkWriter.close();

        ExecutorService executorService = (ExecutorService) httpSinkWriter.getClass()
                .getDeclaredField("executorService").get(httpSinkWriter);
        ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) httpSinkWriter.getClass()
                .getDeclaredField("scheduledExecutorService").get(httpSinkWriter);

        assertTrue(executorService.isShutdown());
        assertTrue(scheduledExecutorService.isShutdown());
    }

    @Test
    public void shouldTerminateExecutorServiceOnClose() throws Exception {
        ExecutorService executorService = mock(ExecutorService.class);
        ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);

        when(executorService.awaitTermination(30, TimeUnit.SECONDS)).thenReturn(false);
        when(scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS)).thenReturn(false);

        httpSinkWriter.getClass().getDeclaredField("executorService").set(httpSinkWriter, executorService);
        httpSinkWriter.getClass().getDeclaredField("scheduledExecutorService").set(httpSinkWriter, scheduledExecutorService);

        httpSinkWriter.close();

        verify(executorService).shutdownNow();
        verify(scheduledExecutorService).shutdownNow();
    }

    @Test
    public void shouldHandlePushToHttpSinkException() throws Exception {
        List<Message> batch = Collections.singletonList(new Message(new byte[]{1}, new byte[]{2}));
        when(httpSink.pushToSink(batch)).thenThrow(new RuntimeException("Test exception"));

        assertThrows(RuntimeException.class, () -> {
            httpSinkWriter.getClass().getDeclaredMethod("pushToHttpSink", List.class)
                    .invoke(httpSinkWriter, batch);
        });

        verify(errorReporter).reportFatalException(any(RuntimeException.class));
    }

    @Test
    public void shouldHandleMixedErrorTypes() throws Exception {
        SinkResponse response = new SinkResponse();
        response.addErrors(0, new ErrorInfo(new Exception("Fatal error"), ErrorType.SINK_5XX_ERROR));
        response.addErrors(1, new ErrorInfo(new Exception("Non-fatal error"), ErrorType.DESERIALIZATION_ERROR));
        List<Message> batch = Arrays.asList(
                new Message(new byte[]{1}, new byte[]{2}),
                new Message(new byte[]{3}, new byte[]{4})
        );

        assertThrows(HttpSinkWriterException.class, () -> {
            httpSinkWriter.getClass().getDeclaredMethod("handleErrors", SinkResponse.class, List.class)
                    .invoke(httpSinkWriter, response, batch);
        });

        verify(errorReporter).reportFatalException(any());
        verify(errorReporter).reportNonFatalException(any());
    }
}
