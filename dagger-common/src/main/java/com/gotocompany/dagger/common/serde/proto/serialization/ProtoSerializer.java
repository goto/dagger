package com.gotocompany.dagger.common.serde.proto.serialization;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.gotocompany.dagger.common.exceptions.DescriptorNotFoundException;
import com.gotocompany.dagger.common.exceptions.serde.DaggerSerializationException;
import com.gotocompany.dagger.common.exceptions.serde.InvalidColumnMappingException;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import org.apache.flink.types.Row;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Serializes Flink {@link Row} records into protobuf byte arrays for Kafka keys and values.
 *
 * <p>Column names supplied at construction map row fields to protobuf fields (supporting nested
 * fields via dot-separated names), and a {@link StencilClientOrchestrator} resolves the key and
 * message descriptors from the schema registry.
 */
public class ProtoSerializer implements Serializable {

    /**
     * The fully-qualified protobuf class name used to serialize the Kafka record key;
     * may be {@code null} or empty when no key is produced.
     */
    private final String keyProtoClassName;
    /**
     * The row column names, positionally aligned with the fields of each {@link Row}.
     */
    private final String[] columnNames;
    /**
     * The orchestrator used to resolve protobuf descriptors from the Stencil registry.
     */
    private final StencilClientOrchestrator stencilClientOrchestrator;
    /**
     * The fully-qualified protobuf class name used to serialize the Kafka record value.
     */
    private final String messageProtoClassName;

    /**
     * Instantiates a new proto serializer.
     *
     * @param keyProtoClassName         the protobuf class name for the record key, may be {@code null} or empty
     * @param messageProtoClassName     the protobuf class name for the record value; required and non-empty
     * @param columnNames               the column names mapping row fields to protobuf fields
     * @param stencilClientOrchestrator the orchestrator used to resolve proto descriptors
     * @throws DaggerSerializationException if {@code messageProtoClassName} is {@code null} or empty
     */
    public ProtoSerializer(String keyProtoClassName, String messageProtoClassName, String[] columnNames, StencilClientOrchestrator stencilClientOrchestrator) {
        this.keyProtoClassName = keyProtoClassName;
        this.columnNames = columnNames;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.messageProtoClassName = messageProtoClassName;
        checkValidity();
    }

    /**
     * Validates that a non-empty message proto class name was supplied.
     *
     * @throws DaggerSerializationException if {@code messageProtoClassName} is {@code null} or empty
     */
    private void checkValidity() {
        if (Objects.isNull(messageProtoClassName) || messageProtoClassName.isEmpty()) {
            throw new DaggerSerializationException("messageProtoClassName is required");
        }
    }

    /**
     * Serialize key message.
     *
     * @param row the row
     * @return the byte [ ]
     */
    public byte[] serializeKey(Row row) {
        return (Objects.isNull(keyProtoClassName) || keyProtoClassName.isEmpty()) ? null
                : parse(row, getDescriptor(keyProtoClassName)).toByteArray();
    }

    /**
     * Serializes the value portion of a row into protobuf bytes.
     *
     * @param row the row to serialize using {@code messageProtoClassName}
     * @return the serialized protobuf message as a byte array
     */
    public byte[] serializeValue(Row row) {
        return parse(row, getDescriptor(messageProtoClassName)).toByteArray();
    }

    /**
     * Builds a {@link DynamicMessage} from a row using the given descriptor.
     *
     * <p>Each column is mapped onto the corresponding protobuf field; dot-separated column
     * names are routed to nested message builders, while unknown top-level fields are skipped.
     *
     * @param element    the row whose fields are written into the message
     * @param descriptor the descriptor of the protobuf message being built
     * @return the populated protobuf message
     */
    private DynamicMessage parse(Row element, Descriptors.Descriptor descriptor) {
        int numberOfElements = element.getArity();
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        for (int index = 0; index < numberOfElements; index++) {
            String columnName = columnNames[index];
            Object data = element.getField(index);
            String[] nestedColumnNames = columnName.split("\\.");
            if (nestedColumnNames.length > 1) {
                Descriptors.FieldDescriptor firstField = descriptor.findFieldByName(nestedColumnNames[0]);
                if (firstField == null) {
                    continue;
                }
                builder = populateNestedBuilder(descriptor, nestedColumnNames, builder, data);
            } else {
                Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByName(columnName);
                builder = populateBuilder(builder, fieldDescriptor, data);
            }
        }
        return builder.build();
    }

    /**
     * Resolves the protobuf {@link Descriptors.Descriptor} for the given class name via Stencil.
     *
     * @param className the fully-qualified protobuf class name to resolve
     * @return the descriptor for {@code className}
     * @throws DescriptorNotFoundException if no descriptor is registered for {@code className}
     */
    private Descriptors.Descriptor getDescriptor(String className) {
        Descriptors.Descriptor dsc = stencilClientOrchestrator.getStencilClient().get(className);
        if (dsc == null) {
            throw new DescriptorNotFoundException();
        }
        return dsc;
    }

    /**
     * Recursively populates a nested protobuf field addressed by a dot-separated column path.
     *
     * <p>The first element of {@code nestedColumnNames} selects a child field on
     * {@code parentDescriptor}; the method descends into the child message builder until the
     * leaf field is reached, then writes {@code data} into it.
     *
     * @param parentDescriptor  the descriptor of the message currently being populated
     * @param nestedColumnNames the remaining path segments identifying the target field
     * @param parentBuilder     the builder of the message currently being populated
     * @param data              the value to set on the leaf field
     * @return the parent builder with the nested field populated
     * @throws InvalidColumnMappingException if a path segment does not exist on the descriptor
     */
    private DynamicMessage.Builder populateNestedBuilder(Descriptors.Descriptor parentDescriptor, String[] nestedColumnNames, DynamicMessage.Builder parentBuilder, Object data) {
        String childColumnName = nestedColumnNames[0];
        Descriptors.FieldDescriptor childFieldDescriptor = parentDescriptor.findFieldByName(childColumnName);
        if (childFieldDescriptor == null) {
            throw new InvalidColumnMappingException(String.format("column %s doesn't exists in the proto of %s", childColumnName, parentDescriptor.getFullName()));
        }
        if (nestedColumnNames.length == 1) {
            return populateBuilder(parentBuilder, childFieldDescriptor, data);
        }
        Descriptors.Descriptor childDescriptor = childFieldDescriptor.getMessageType();
        DynamicMessage.Builder childBuilder = DynamicMessage.newBuilder(childDescriptor);
        childBuilder.mergeFrom((DynamicMessage) parentBuilder.build().getField(childFieldDescriptor));
        parentBuilder.setField(childFieldDescriptor, populateNestedBuilder(childDescriptor, Arrays.copyOfRange(nestedColumnNames, 1, nestedColumnNames.length), childBuilder, data).build());
        return parentBuilder;
    }

    /**
     * Sets a single, non-nested protobuf field on the given builder.
     *
     * <p>A {@code null} field descriptor or {@code null} data leaves the builder unchanged;
     * otherwise the value is converted by the {@link TypeHandler} resolved for the field.
     *
     * @param builder         the builder to populate
     * @param fieldDescriptor the target protobuf field, may be {@code null}
     * @param data            the value to write, may be {@code null}
     * @return the (possibly unchanged) builder
     * @throws InvalidColumnMappingException if {@code data}'s type does not match the field type
     */
    private DynamicMessage.Builder populateBuilder(DynamicMessage.Builder builder, Descriptors.FieldDescriptor fieldDescriptor, Object data) {
        if (fieldDescriptor == null) {
            return builder;
        }
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(fieldDescriptor);
        if (data != null) {
            try {
                builder = typeHandler.transformToProtoBuilder(builder, data);
            } catch (RuntimeException e) {
                String protoType = fieldDescriptor.getType().toString();
                if (fieldDescriptor.isRepeated()) {
                    protoType = String.format("REPEATED %s", fieldDescriptor.getType());
                }
                String errMessage = String.format("column invalid: type mismatch of column %s, expecting %s type. Actual type %s", fieldDescriptor.getName(), protoType, data.getClass());
                throw new InvalidColumnMappingException(errMessage, e);
            }
        }

        return builder;
    }
}
