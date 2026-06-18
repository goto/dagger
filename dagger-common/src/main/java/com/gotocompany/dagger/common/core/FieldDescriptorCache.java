package com.gotocompany.dagger.common.core;

import com.google.protobuf.Descriptors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Serializable cache of protobuf field positions, used to keep field indices stable when the
 * Stencil schema is refreshed at runtime.
 *
 * <p>On construction it walks a protobuf {@link Descriptors.Descriptor} recursively (descending into
 * nested {@code MESSAGE} fields) and records, for every field, its original declared index keyed by
 * fully-qualified name, plus the field count (arity) of every message type keyed by fully-qualified
 * name. The protobuf and Parquet deserializers consult this cache so that rows are built against the
 * field layout captured at startup rather than against a possibly-reordered refreshed descriptor.
 */
public class FieldDescriptorCache implements Serializable {
    /** Maps each field's fully-qualified name to its original declared index within its message. */
    private final Map<String, Integer> fieldDescriptorIndexMap = new HashMap<>();
    /** Maps each message type's fully-qualified name to its original field count (arity). */
    private final Map<String, Integer> protoDescriptorArityMap = new HashMap<>();

    /**
     * Builds a cache by recursively indexing the given descriptor and all nested message types.
     *
     * @param descriptor the root protobuf descriptor to index
     */
    public FieldDescriptorCache(Descriptors.Descriptor descriptor) {

        cacheFieldDescriptorMap(descriptor);
    }

    /**
     * Recursively records field indices and message arities for the given descriptor.
     *
     * <p>Returns early if this message type has already been cached, which both avoids repeated work
     * and guards against recursive or self-referential schemas. For every field it stores the
     * field's original index, and for nested {@code MESSAGE} fields it recurses into the referenced
     * message type.
     *
     * @param descriptor the protobuf descriptor whose fields should be cached
     */
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
    }

    /**
     * Returns the cached original index of a protobuf field.
     *
     * @param fieldDescriptor the field whose original index is requested
     * @return the field's original declared index captured when it was cached
     * @throws IllegalArgumentException if the field is not present in the cache
     */
    public int getOriginalFieldIndex(Descriptors.FieldDescriptor fieldDescriptor) {
        if (!fieldDescriptorIndexMap.containsKey(fieldDescriptor.getFullName())) {
            throw new IllegalArgumentException("The Field Descriptor " + fieldDescriptor.getFullName() + " was not found in the cache");
        }
        return fieldDescriptorIndexMap.get(fieldDescriptor.getFullName());
    }

    /**
     * Indicates whether a field (by fully-qualified name) is present in the cache.
     *
     * @param fieldName the fully-qualified field name to look up
     * @return {@code true} if the field has been cached, {@code false} otherwise
     */
    public boolean containsField(String fieldName) {

        return fieldDescriptorIndexMap.containsKey(fieldName);
    }

    /**
     * Returns the cached original field count (arity) of a protobuf message type.
     *
     * @param descriptor the message descriptor whose original field count is requested
     * @return the number of fields the message declared when it was cached
     * @throws IllegalArgumentException if the descriptor is not present in the cache
     */
    public int getOriginalFieldCount(Descriptors.Descriptor descriptor) {
        if (!protoDescriptorArityMap.containsKey(descriptor.getFullName())) {
            throw new IllegalArgumentException("The Proto Descriptor " + descriptor.getFullName() + " was not found in the cache");
        }
        return protoDescriptorArityMap.get(descriptor.getFullName());
    }
}
