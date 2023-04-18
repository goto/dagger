package com.gotocompany.dagger.common.core;

import com.gotocompany.dagger.consumer.TestBookingLogMessage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class FieldDescriptorCacheTest {


    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldReturnTrueIfFieldPresentInMap() {
        FieldDescriptorCache fieldDescriptorCache = new FieldDescriptorCache(TestBookingLogMessage.getDescriptor());
        assertTrue(fieldDescriptorCache.containsField("com.gotocompany.dagger.consumer.TestBookingLogMessage.order_number"));
    }

    @Test
    public void shouldReturnFalseIfFieldNotPresentInMap() {
        FieldDescriptorCache fieldDescriptorCache = new FieldDescriptorCache(TestBookingLogMessage.getDescriptor());
        assertFalse(fieldDescriptorCache.containsField("xyz"));
    }

    @Test
    public void shouldReturnOriginalFieldIndex() {
        FieldDescriptorCache fieldDescriptorCache = new FieldDescriptorCache(TestBookingLogMessage.getDescriptor());
        assertEquals(1, fieldDescriptorCache.getOriginalFieldIndex(TestBookingLogMessage.getDescriptor().findFieldByName("order_number")));
    }

    @Test
    public void shouldReturnOriginalFieldCount() {
        FieldDescriptorCache fieldDescriptorCache = new FieldDescriptorCache(TestBookingLogMessage.getDescriptor());
        assertEquals(49, fieldDescriptorCache.getOriginalFieldCount(TestBookingLogMessage.getDescriptor()));
    }
}
