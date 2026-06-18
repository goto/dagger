package com.gotocompany.dagger.core.processors.external.grpc.client;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistryLite;

import io.grpc.MethodDescriptor.Marshaller;

/**
 * A {@link Marshaller} for dynamic messages.
 */
public class DynamicMessageMarshaller implements Marshaller<DynamicMessage> {
    /**
     * The protobuf descriptor describing the dynamic message type marshalled by this instance.
     */
    private final Descriptor messageDescriptor;

    /**
     * Instantiates a new Dynamic message marshaller.
     *
     * @param messageDescriptor the message descriptor
     */
    public DynamicMessageMarshaller(Descriptor messageDescriptor) {
        this.messageDescriptor = messageDescriptor;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reads a {@link DynamicMessage} of the configured type from the supplied stream.
     *
     * @param inputStream the stream carrying the serialized protobuf message
     * @return the parsed dynamic message
     * @throws RuntimeException if the message cannot be read from the stream
     */
    @Override
    public DynamicMessage parse(InputStream inputStream) {
        try {
            return DynamicMessage.newBuilder(messageDescriptor)
                    .mergeFrom(inputStream, ExtensionRegistryLite.getEmptyRegistry())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Unable to merge from the supplied input stream", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Serializes the given message into a stream over its protobuf byte representation.
     *
     * @param abstractMessage the dynamic message to serialize
     * @return a stream over the serialized message bytes
     */
    @Override
    public InputStream stream(DynamicMessage abstractMessage) {
        return abstractMessage.toByteString().newInput();
    }
}
