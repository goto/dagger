package com.gotocompany.dagger.common.serde.typehandler.complex;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.gotocompany.dagger.consumer.TestBookingLogMessage;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.schema.GroupType;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class GoogleProtobufComplexMessageHandlerTest {

    @Test
    public void shouldReturnTrueForCanHandleForStructFieldDescriptor() {
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("profile_data");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        assertTrue(handler.canHandle());
    }

    @Test
    public void shouldReturnTrueForCanHandleForValueFieldDescriptor() {
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("tag");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        assertTrue(handler.canHandle());
    }

    @Test
    public void shouldReturnTrueForCanHandleForListValueFieldDescriptor() {
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("tags");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        assertTrue(handler.canHandle());
    }

    @Test
    public void shouldReturnTrueForCanHandleForRepeatedFieldDescriptor() {
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("labels");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        assertTrue(handler.canHandle());
    }

    @Test
    public void shouldReturnFalseForCanHandleForMessageFieldDescriptor() {
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("driver_pickup_location");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        assertFalse(handler.canHandle());
    }

    @Test
    public void shouldReturnTypeInformation() {
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("profile_data");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        TypeInformation actualTypeInformation = handler.getTypeInformation();
        TypeInformation<?> expectedTypeInformation = Types.PRIMITIVE_ARRAY(Types.BYTE);
        assertEquals(expectedTypeInformation, actualTypeInformation);
    }

    @Test
    public void shouldReturnByteArrayForTransformFromProto() {
        Struct struct = Struct.newBuilder()
                .putFields("structKey1", Value.newBuilder().setStringValue("structValue").build())
                .putFields("structKey2", Value.newBuilder().setNumberValue(23.0).build())
                .build();
        byte[] expectedBytes = struct.toByteArray();
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("profile_data");

        DynamicMessage structAsDynamicMessage = DynamicMessage.newBuilder(fieldDescriptor.getMessageType())
                .mergeFrom(struct)
                .build();
        DynamicMessage dynamicMessage = DynamicMessage.newBuilder(fieldDescriptor.getContainingType())
                .setField(fieldDescriptor, structAsDynamicMessage)
                .build();

        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);

        // when
        Object fieldValue = dynamicMessage.getField(fieldDescriptor);
        byte[] actualBytes1 = (byte[]) handler.transformFromProto(fieldValue);

        // when called using cache - as the implementation is same so asserting here itself
        byte[] actualBytes2 = (byte[]) handler.transformFromProtoUsingCache(fieldValue, null);

        assertArrayEquals(expectedBytes, actualBytes1);
        assertArrayEquals(expectedBytes, actualBytes2);
    }

    @Test
    public void shouldReturnTheBuilderSettingByteArrayValue() {
        Struct struct = Struct.newBuilder()
                .putFields("structKey1", Value.newBuilder().setStringValue("structValue").build())
                .putFields("structKey2", Value.newBuilder().setNumberValue(23.0).build())
                .build();
        byte[] byteArray = struct.toByteArray();

        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("profile_data");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(fieldDescriptor.getContainingType());

        // when
        DynamicMessage.Builder resultBuilder = handler.transformToProtoBuilder(builder, byteArray);

        assertTrue(resultBuilder.hasField(fieldDescriptor));

        Object fieldValue = resultBuilder.getField(fieldDescriptor);
        assertTrue(fieldValue instanceof DynamicMessage);

        DynamicMessage parsedMessage = (DynamicMessage) fieldValue;

        // parsed struct is NOT empty
        assertFalse(parsedMessage.getAllFields().isEmpty());

        // https://github.com/protocolbuffers/protobuf/blob/d124c2dc26841e5ee0b8d1505438fcf0660c9db0/src/google/protobuf/struct.proto#L53 field name is "fields"
        Descriptors.FieldDescriptor fieldsDescriptor = parsedMessage.getDescriptorForType().findFieldByName("fields");

        List<DynamicMessage> fields = (List<DynamicMessage>) parsedMessage.getField(fieldsDescriptor);

        // convert map entries to java map for easier assertions
        Map<String, DynamicMessage> structFields = new HashMap<>();

        for (DynamicMessage entry : fields) {
            String key = (String) entry.getField(entry.getDescriptorForType().findFieldByName("key"));
            DynamicMessage value = (DynamicMessage) entry.getField(entry.getDescriptorForType().findFieldByName("value"));
            structFields.put(key, value);
        }

        // assert keys exist
        assertTrue(structFields.containsKey("structKey1"));
        assertTrue(structFields.containsKey("structKey2"));

        // assert values
        DynamicMessage value1 = structFields.get("structKey1");
        DynamicMessage value2 = structFields.get("structKey2");

        // https://github.com/protocolbuffers/protobuf/blob/d124c2dc26841e5ee0b8d1505438fcf0660c9db0/src/google/protobuf/struct.proto#L70
        Descriptors.FieldDescriptor stringValueFd = value1.getDescriptorForType().findFieldByName("string_value");
        // https://github.com/protocolbuffers/protobuf/blob/d124c2dc26841e5ee0b8d1505438fcf0660c9db0/src/google/protobuf/struct.proto#L68
        Descriptors.FieldDescriptor numberValueFd = value2.getDescriptorForType().findFieldByName("number_value");

        assertEquals("structValue", value1.getField(stringValueFd));
        assertEquals(23.0, value2.getField(numberValueFd));
    }

    // Not implemented but adding basic test cases for test coverage

    @Test
    public void shouldReturnNullWhenTransformFromParquetIsCalledWithAnyArgument() {
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("profile_data");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        GroupType parquetSchema = org.apache.parquet.schema.Types.requiredGroup().named("TestGroupType");
        SimpleGroup simpleGroup = new SimpleGroup(parquetSchema);
        assertNull(handler.transformFromParquet(simpleGroup));
    }

    @Test
    public void shouldReturnAsIsForTransformForPostProcessor() {
        Descriptors.FieldDescriptor fieldDescriptor = TestBookingLogMessage.getDescriptor().findFieldByName("profile_data");
        GoogleProtobufComplexMessageHandler handler = new GoogleProtobufComplexMessageHandler(fieldDescriptor);
        byte[] input = "test".getBytes();
        assertEquals(handler.transformFromPostProcessor(input), input);
    }
}
