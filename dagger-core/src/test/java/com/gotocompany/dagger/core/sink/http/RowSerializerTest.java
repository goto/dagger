import com.google.protobuf.DynamicMessage;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.sink.http.config.HttpSinkConfig;
import com.gotocompany.dagger.core.sink.http.exception.RowSerializationException;
import com.gotocompany.dagger.core.sink.http.decorator.MessageDecorator;
import com.gotocompany.depot.message.Message;
import org.apache.flink.types.Row;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RowSerializerTest {

    @Mock
    private HttpSinkConfig httpSinkConfig;

    @Mock
    private StencilClientOrchestrator stencilClientOrchestrator;

    @Mock
    private ProtoSerializer protoSerializer;

    private RowSerializer rowSerializer;

    @Before
    public void setUp() {
        when(httpSinkConfig.getSinkConnectorSchemaProtoKeyClass()).thenReturn("TestKeyClass");
        when(httpSinkConfig.getSinkConnectorSchemaProtoMessageClass()).thenReturn("TestMessageClass");
        when(httpSinkConfig.getMaxBatchSize()).thenReturn(100);

        rowSerializer = new RowSerializer(httpSinkConfig, stencilClientOrchestrator, new String[]{"col1", "col2"});
    }

    @Test
    public void shouldSerializeRowsSuccessfully() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1", "value2"));
        rows.add(Row.of("value3", "value4"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(2, messages.size());
    }

    @Test
    public void shouldReturnEmptyListForEmptyInput() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertTrue(messages.isEmpty());
    }

    @Test(expected = RowSerializationException.class)
    public void shouldThrowExceptionWhenSerializationFails() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1", "value2"));

        when(protoSerializer.serializeValue(any(Row.class))).thenThrow(new RuntimeException("Serialization error"));

        rowSerializer.serializeRows(rows).get();
    }

    @Test
    public void shouldVisitRowSuccessfully() {
        Row row = Row.of("value1", "value2");
        DynamicMessage dynamicMessage = mock(DynamicMessage.class);
        when(protoSerializer.serializeValue(row)).thenReturn(dynamicMessage);

        Optional<Message> result = rowSerializer.visitRow(row);

        assertTrue(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyOptionalWhenVisitRowFails() {
        Row row = Row.of("value1", "value2");
        when(protoSerializer.serializeValue(row)).thenThrow(new RuntimeException("Serialization error"));

        Optional<Message> result = rowSerializer.visitRow(row);

        assertFalse(result.isPresent());
    }

    @Test
    public void shouldFlushBatchWhenMaxSizeReached() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            rows.add(Row.of("value" + i));
        }

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        future.get();

        assertNotNull(rowSerializer.getNextBatch());
    }

    @Test
    public void shouldApplyDecoratorWhenAdded() {
        MessageDecorator decorator = mock(MessageDecorator.class);
        rowSerializer.addDecorator(decorator);

        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1"));

        rowSerializer.serializeRows(rows);

        verify(decorator, times(1)).decorate(any(Message.class));
    }

    @Test
    public void shouldNotApplyDecoratorWhenRemoved() {
        MessageDecorator decorator = mock(MessageDecorator.class);
        rowSerializer.addDecorator(decorator);
        rowSerializer.removeDecorator(decorator);

        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1"));

        rowSerializer.serializeRows(rows);

        verify(decorator, never()).decorate(any(Message.class));
    }

    @Test
    public void shouldUseCustomSerializationStrategy() {
        rowSerializer.setSerializationStrategy(row -> mock(DynamicMessage.class));

        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);

        assertNotNull(future);
    }

    @Test
    public void shouldSerializeRowsAsynchronously() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRowsAsync(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldUseCacheForRepeatedSerialization() {
        Row row = Row.of("value1", "value2");
        DynamicMessage dynamicMessage = mock(DynamicMessage.class);
        when(protoSerializer.serializeValue(row)).thenReturn(dynamicMessage);

        Optional<Message> result1 = rowSerializer.serializeWithCache(row);
        Optional<Message> result2 = rowSerializer.serializeWithCache(row);

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(result1.get(), result2.get());
    }

    @Test
    public void shouldSchedulePeriodicSerialization() throws InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1"));

        rowSerializer.schedulePeriodicSerialization(rows, 0, 100, TimeUnit.MILLISECONDS);

        Thread.sleep(250);

        assertNotNull(rowSerializer.getNextBatch());
    }

    @Test
    public void shouldProfileSerialization() {
        Row row = Row.of("value1", "value2");
        DynamicMessage dynamicMessage = mock(DynamicMessage.class);
        when(protoSerializer.serializeValue(row)).thenReturn(dynamicMessage);

        rowSerializer.profileSerialization(row);

        verify(protoSerializer, times(1)).serializeValue(row);
    }

    @Test
    public void shouldSerializeWithLoadBalancing() throws ExecutionException, InterruptedException {
        Row row = Row.of("value1", "value2");
        DynamicMessage dynamicMessage = mock(DynamicMessage.class);
        when(protoSerializer.serializeValue(row)).thenReturn(dynamicMessage);

        CompletableFuture<Message> future = rowSerializer.serializeBalanced(row);
        Message message = future.get();

        assertNotNull(message);
    }

    @Test
    public void shouldCreateNewBatchWhenMaxSizeReached() throws ExecutionException, InterruptedException {
        when(httpSinkConfig.getMaxBatchSize()).thenReturn(2);
        rowSerializer = new RowSerializer(httpSinkConfig, stencilClientOrchestrator, new String[]{"col1", "col2"});

        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1", "value2"));
        rows.add(Row.of("value3", "value4"));
        rows.add(Row.of("value5", "value6"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(3, messages.size());
        assertNotNull(rowSerializer.getNextBatch());
    }

    @Test
    public void shouldHandleConcurrentSerialization() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            rows.add(Row.of("value" + i));
        }

        List<CompletableFuture<List<Message>>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(rowSerializer.serializeRows(rows));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        for (CompletableFuture<List<Message>> future : futures) {
            assertEquals(1000, future.get().size());
        }
    }

    @Test
    public void shouldHandleNullValuesInRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of(null, "value2"));
        rows.add(Row.of("value3", null));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(2, messages.size());
    }

    @Test
    public void shouldHandleEmptyStringsInRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("", "value2"));
        rows.add(Row.of("value3", ""));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(2, messages.size());
    }

    @Test
    public void shouldHandleSpecialCharactersInRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1!", "@value2"));
        rows.add(Row.of("#value3", "$value4%"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(2, messages.size());
    }

    @Test
    public void shouldHandleLargeDataInRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        StringBuilder largeString = new StringBuilder();
        for (int i = 0; i < 1000000; i++) {
            largeString.append("a");
        }
        rows.add(Row.of(largeString.toString(), "value2"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleDifferentDataTypesInRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of(1, "value2", 3.14, true));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleNestedRowsInRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        Row nestedRow = Row.of("nested1", "nested2");
        rows.add(Row.of("value1", nestedRow));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleArraysInRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        String[] array = {"arr1", "arr2", "arr3"};
        rows.add(Row.of("value1", array));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleMapsInRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        rows.add(Row.of("value1", map));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldIgnoreNullRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(null);

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertTrue(messages.isEmpty());
    }

    @Test
    public void shouldHandleMixedValidAndInvalidRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1", "value2"));
        rows.add(null);
        rows.add(Row.of("value3", "value4"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(2, messages.size());
    }

    @Test
    public void shouldReturnEmptyListForAllInvalidRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(null);
        rows.add(null);
        rows.add(null);

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertTrue(messages.isEmpty());
    }

    @Test
    public void shouldHandleEmptyRows() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of());

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleInconsistentRowSizes() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of("value1", "value2"));
        rows.add(Row.of("value3"));
        rows.add(Row.of("value4", "value5", "value6"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(3, messages.size());
    }

    @Test
    public void shouldHandleMaxIntegerValue() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of(Integer.MAX_VALUE, "value2"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleMinIntegerValue() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of(Integer.MIN_VALUE, "value2"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleMaxLongValue() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of(Long.MAX_VALUE, "value2"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleMinLongValue() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of(Long.MIN_VALUE, "value2"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleInfinityValues() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }

    @Test
    public void shouldHandleNaNValue() throws ExecutionException, InterruptedException {
        List<Row> rows = new ArrayList<>();
        rows.add(Row.of(Double.NaN, "value2"));

        CompletableFuture<List<Message>> future = rowSerializer.serializeRows(rows);
        List<Message> messages = future.get();

        assertEquals(1, messages.size());
    }
}
