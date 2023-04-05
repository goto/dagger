package com.gotocompany.dagger.common.core;

import com.google.protobuf.Descriptors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FieldDescriptorCache implements Serializable {
    private final Map<String, Integer> fieldDescriptorIndexMap = new HashMap<>();
    private final Map<String, Integer> protoDescriptorArityMap = new HashMap<>();
    private final boolean stencilCacheAutoRefreshEnable;

    public FieldDescriptorCache(Descriptors.Descriptor descriptor, boolean stencilCacheAutoRefreshEnable) {
        this.stencilCacheAutoRefreshEnable = stencilCacheAutoRefreshEnable;

        cacheFieldDescriptorMap(descriptor);
    }

    public void cacheFieldDescriptorMap(Descriptors.Descriptor descriptor) {

        if (protoDescriptorArityMap.containsKey(descriptor.getFullName())) {
            return;
        }
        List<Descriptors.FieldDescriptor> descriptorFields = descriptor.getFields();
        protoDescriptorArityMap.putIfAbsent(descriptor.getFullName(), descriptorFields.size());

        for (Descriptors.FieldDescriptor fieldDescriptor : descriptorFields) {
            fieldDescriptorIndexMap.putIfAbsent(fieldDescriptor.getFullName(), fieldDescriptor.getIndex());
        }

        for (Descriptors.FieldDescriptor fieldDescriptor : descriptorFields) {
            if (fieldDescriptor.getType().toString().equals("MESSAGE")) {
                cacheFieldDescriptorMap(fieldDescriptor.getMessageType());

            }
        }

        List<Descriptors.Descriptor> nestedTypes = descriptor.getNestedTypes();
        for (Descriptors.Descriptor nestedTypeDescriptor : nestedTypes) {
            cacheFieldDescriptorMap(nestedTypeDescriptor);

        }

    }

    public int getOriginalFieldIndex(Descriptors.FieldDescriptor fieldDescriptor) {
        return (stencilCacheAutoRefreshEnable) ? fieldDescriptorIndexMap.get(fieldDescriptor.getFullName()) : fieldDescriptor.getIndex();
    }

    public boolean containsField(String fieldName) {

        return fieldDescriptorIndexMap.containsKey(fieldName);
    }

    public int getOriginalFieldCount(Descriptors.Descriptor descriptor) {

        return protoDescriptorArityMap.get(descriptor.getFullName());
    }
}
