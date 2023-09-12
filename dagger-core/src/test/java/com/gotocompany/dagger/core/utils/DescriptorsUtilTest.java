package com.gotocompany.dagger.core.utils;

import com.google.protobuf.Descriptors;
import com.gotocompany.dagger.consumer.TestCustomerLogMessage;
import com.gotocompany.dagger.consumer.TestEnrichedBookingLogMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DescriptorsUtilTest {

    @Test
    public void shouldGetFieldDescriptor() {
        String fieldName = "customer_id";
        Descriptors.Descriptor descriptor = TestCustomerLogMessage.getDescriptor();
        Descriptors.FieldDescriptor fieldDescriptor = DescriptorsUtil.getFieldDescriptor(descriptor, fieldName);
        assertNotNull(fieldDescriptor);
        assertEquals("customer_id", fieldDescriptor.getName());
    }

    @Test
    public void shouldRunGetNestedFieldDescriptor() {
        String fieldName = "customer_profile.customer_id";
        Descriptors.Descriptor descriptor = TestEnrichedBookingLogMessage.getDescriptor();
        Descriptors.FieldDescriptor fieldDescriptor = DescriptorsUtil.getFieldDescriptor(descriptor, fieldName);
        assertNotNull(fieldDescriptor);
        assertEquals("customer_id", fieldDescriptor.getName());
    }

    @Test
    public void shouldRunGetNestedFieldColumnsDescriptor() {
        Descriptors.Descriptor parentDescriptor = TestEnrichedBookingLogMessage.getDescriptor();
        String[] nestedFieldNames = {"customer_profile", "customer_id"};
        Descriptors.FieldDescriptor fieldDescriptor = DescriptorsUtil.getNestedFieldDescriptor(parentDescriptor, nestedFieldNames);
        assertNotNull(fieldDescriptor);
        assertEquals("customer_id", fieldDescriptor.getName());
    }

    @Test
    public void shouldGiveNullForEmptyFieldFieldDescriptor() {
        String nonExistentField = "customer-id";
        Descriptors.FieldDescriptor nonExistentFieldDescriptor = DescriptorsUtil.getFieldDescriptor(null, nonExistentField);
        assertNull(nonExistentFieldDescriptor);
    }
    @Test
    public void shouldGiveNullForNullColumnFieldFieldDescriptor() {
        Descriptors.Descriptor descriptor = TestCustomerLogMessage.getDescriptor();
        Descriptors.FieldDescriptor nonExistentFieldDescriptor = DescriptorsUtil.getFieldDescriptor(descriptor, null);
        assertNull(nonExistentFieldDescriptor);
    }
    @Test
    public void shouldGiveNullForEmptyColumnFieldFieldDescriptor() {
        Descriptors.Descriptor descriptor = TestCustomerLogMessage.getDescriptor();
        Descriptors.FieldDescriptor nonExistentFieldDescriptor = DescriptorsUtil.getFieldDescriptor(descriptor, "");
        assertNull(nonExistentFieldDescriptor);
    }
    @Test
    public void shouldGiveNullForInvalidFieldFieldDescriptor() {
        Descriptors.Descriptor descriptor = TestCustomerLogMessage.getDescriptor();
        String nonExistentField = "customer-id";
        Descriptors.FieldDescriptor nonExistentFieldDescriptor = DescriptorsUtil.getFieldDescriptor(descriptor, nonExistentField);
        assertNull(nonExistentFieldDescriptor);
    }

    @Test
    public void shouldGiveNullForInvalidNestedFieldDescriptor() {
        Descriptors.Descriptor parentDescriptor = TestEnrichedBookingLogMessage.getDescriptor();
        String fieldName = "customer_profile.customer-id";
        Descriptors.FieldDescriptor invalidFieldDescriptor = DescriptorsUtil.getFieldDescriptor(parentDescriptor, fieldName);
        assertNull(invalidFieldDescriptor);
    }


    @Test
    public void shouldGiveNullForInvalidNestedFieldColumnsDescriptor() {
        Descriptors.Descriptor parentDescriptor = TestEnrichedBookingLogMessage.getDescriptor();
        String[] invalidNestedFieldNames = {"customer_profile", "customer-id"};
        Descriptors.FieldDescriptor invalidFieldDescriptor = DescriptorsUtil.getNestedFieldDescriptor(parentDescriptor, invalidNestedFieldNames);
        assertNull(invalidFieldDescriptor);
    }

    @Test
    public void shouldGiveNullForNullNestedFieldDescriptor() {
        String[] nonExistentField = new String[]{"customer-id"};
        Descriptors.FieldDescriptor nonExistentFieldDescriptor = DescriptorsUtil.getNestedFieldDescriptor(null, nonExistentField);
        assertNull(nonExistentFieldDescriptor);
    }

    @Test
    public void shouldGiveNullForNullColumnNestedFieldDescriptor() {
        Descriptors.Descriptor descriptor = TestCustomerLogMessage.getDescriptor();
        Descriptors.FieldDescriptor nonExistentFieldDescriptor = DescriptorsUtil.getNestedFieldDescriptor(descriptor, null);
        assertNull(nonExistentFieldDescriptor);
    }
    @Test
    public void shouldGiveNullForEmptyColumnNestedFieldDescriptor() {
        Descriptors.Descriptor descriptor = TestCustomerLogMessage.getDescriptor();
        Descriptors.FieldDescriptor nonExistentFieldDescriptor = DescriptorsUtil.getNestedFieldDescriptor(descriptor, new String[]{});
        assertNull(nonExistentFieldDescriptor);
    }
}
