package com.gotocompany.dagger.common.serde.typehandler;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.common.serde.typehandler.complex.*;
import com.gotocompany.dagger.common.serde.typehandler.repeated.RepeatedEnumHandler;
import com.gotocompany.dagger.common.serde.typehandler.repeated.RepeatedMessageHandler;
import com.gotocompany.dagger.common.serde.typehandler.repeated.RepeatedPrimitiveHandler;
import com.gotocompany.dagger.common.serde.typehandler.repeated.RepeatedStructMessageHandler;
import com.gotocompany.dagger.consumer.TestBookingLogMessage;
import com.gotocompany.dagger.consumer.TestFeedbackLogMessage;
import com.gotocompany.dagger.consumer.TestNestedRepeatedMessage;
import com.gotocompany.dagger.consumer.TestRepeatedEnumMessage;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TypeHandlerFactoryTest {
    @Before
    public void setup() {
        TypeHandlerFactory.clearTypeHandlerMap();
    }

    @Test
    public void shouldReturnTheSameHandlerObjectWhenBothFieldDescriptorFullNameAndFieldDescriptorHashCodeIsSame() {
        Descriptors.FieldDescriptor mapFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("metadata");
        TypeHandler typeHandler1 = TypeHandlerFactory.getTypeHandler(mapFieldDescriptor);
        TypeHandler typeHandler2 = TypeHandlerFactory.getTypeHandler(mapFieldDescriptor);
        assertEquals(typeHandler1, typeHandler2);
    }

    @Test
    public void shouldReturnDifferentCopiesOfHandlerObjectWhenFieldDescriptorFullNameIsSameButHashCodeIsDifferent() {
        Descriptors.FieldDescriptor mapFieldDescriptor1 = TestBookingLogMessage.getDescriptor().findFieldByName("metadata");

        Descriptors.FieldDescriptor mapFieldDescriptor2 = mock(Descriptors.FieldDescriptor.class);
        when(mapFieldDescriptor2.getFullName()).thenReturn(mapFieldDescriptor1.getFullName());


        TypeHandler typeHandler1 = TypeHandlerFactory.getTypeHandler(mapFieldDescriptor1);
        TypeHandler typeHandler2 = TypeHandlerFactory.getTypeHandler(mapFieldDescriptor2);
        assertNotEquals(typeHandler1, typeHandler2);
    }


    @Test
    public void shouldReturnMapHandlerIfMapFieldDescriptorPassed() {
        Descriptors.FieldDescriptor mapFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("metadata");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(mapFieldDescriptor);
        assertEquals(MapHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnTimestampHandlerIfTimestampFieldDescriptorPassed() {
        Descriptors.FieldDescriptor timestampFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("event_timestamp");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(timestampFieldDescriptor);
        assertEquals(TimestampHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnEnumHandlerIfEnumFieldDescriptorPassed() {
        Descriptors.FieldDescriptor enumFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("service_type");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(enumFieldDescriptor);
        assertEquals(EnumHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnRepeatedHandlerIfRepeatedFieldDescriptorPassed() {
        Descriptors.FieldDescriptor repeatedFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("meta_array");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(repeatedFieldDescriptor);
        assertEquals(RepeatedPrimitiveHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnRepeatedMessageHandlerIfRepeatedMessageFieldDescriptorPassed() {
        Descriptors.FieldDescriptor repeatedMessageFieldDescriptor = TestFeedbackLogMessage.getDescriptor().findFieldByName("reason");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(repeatedMessageFieldDescriptor);
        assertEquals(RepeatedMessageHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnRepeatedEnumHandlerIfRepeatedEnumFieldDescriptorPassed() {
        Descriptors.FieldDescriptor repeatedEnumFieldDescriptor = TestRepeatedEnumMessage.getDescriptor().findFieldByName("test_enums");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(repeatedEnumFieldDescriptor);
        assertEquals(RepeatedEnumHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnRepeatedStructHandlerIfRepeatedStructFieldDescriptorPassed() {
        Descriptors.FieldDescriptor repeatedStructFieldDescriptor = TestNestedRepeatedMessage.getDescriptor().findFieldByName("metadata");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(repeatedStructFieldDescriptor);
        assertEquals(RepeatedStructMessageHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnStructHandlerIfStructFieldDescriptorPassed() {
        Descriptors.FieldDescriptor structFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("profile_data");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(structFieldDescriptor);
        assertEquals(StructMessageHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnMessageHandlerIfMessageFieldDescriptorPassed() {
        Descriptors.FieldDescriptor messageFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("payment_option_metadata");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(messageFieldDescriptor);
        assertEquals(MessageHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnDefaultHandlerIfPrimitiveFieldDescriptorPassed() {
        Descriptors.FieldDescriptor primitiveFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("order_number");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(primitiveFieldDescriptor);
        assertEquals(PrimitiveTypeHandler.class, typeHandler.getClass());
    }

    @Test
    public void shouldReturnTheSameObjectWithMultipleThreads() throws InterruptedException {
        ExecutorService e = Executors.newFixedThreadPool(100);
        final TypeHandler[] cache = {null};
        for (int i = 0; i < 1000; i++) {
            e.submit(() -> {
                Descriptors.FieldDescriptor primitiveFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("order_number");
                TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(primitiveFieldDescriptor);
                assertEquals(PrimitiveTypeHandler.class, typeHandler.getClass());
                synchronized (cache) {
                    TypeHandler oldHandler = cache[0];
                    if (oldHandler != null) {
                        assertEquals(typeHandler, cache[0]);
                    } else {
                        // Only one thread will set this
                        cache[0] = typeHandler;
                    }
                }
            });
        }
        e.shutdown();
        e.awaitTermination(10000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void shouldReturnTheSameObjectWhenFactoryMethodIsCalledMultipleTimes() {
        Descriptors.FieldDescriptor primitiveFieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("order_number");
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(primitiveFieldDescriptor);
        assertEquals(PrimitiveTypeHandler.class, typeHandler.getClass());
        TypeHandler newTypeHandler = TypeHandlerFactory.getTypeHandler(primitiveFieldDescriptor);
        assertEquals(PrimitiveTypeHandler.class, newTypeHandler.getClass());
        assertEquals(typeHandler, newTypeHandler);
    }

    public static class SerializableFieldDescriptor implements Serializable {
        public Descriptors.FieldDescriptor fieldDescriptor;

        SerializableFieldDescriptor(Descriptors.FieldDescriptor fieldDescriptor) {
            this.fieldDescriptor = fieldDescriptor;
        }
    }
}
