package com.gotocompany.dagger.functions.udfs.scalar.elementAt;

import com.gotocompany.stencil.client.StencilClient;
import com.gotocompany.dagger.functions.udfs.scalar.elementAt.descriptor.CustomDescriptor;
import com.gotocompany.dagger.functions.udfs.scalar.elementAt.row.Element;
import org.apache.flink.types.Row;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.protobuf.Descriptors.Descriptor;

/**
 * The Message reader.
 */
public class MessageReader {
    /**
     * The Flink {@link Row} record from which a nested element is read.
     */
    private Row message;
    /**
     * The fully-qualified protobuf class name describing the schema of the message.
     */
    private String protoClassName;
    /**
     * The dot-separated path identifying the parent message within the proto schema.
     */
    private String pathOfMessage;
    /**
     * The Stencil client used to resolve protobuf descriptors by class name.
     */
    private StencilClient stencilClient;

    /**
     * Instantiates a new Message reader.
     *
     * @param message        the message
     * @param protoClassName the proto class name
     * @param pathOfMessage  the path of message
     * @param stencilClient  the stencil client
     */
    public MessageReader(Row message, String protoClassName, String pathOfMessage, StencilClient stencilClient) {
        this.message = message;
        this.protoClassName = protoClassName;
        this.pathOfMessage = pathOfMessage;
        this.stencilClient = stencilClient;
    }

    /**
     * Resolves the root protobuf descriptor for the configured proto class name via the Stencil client.
     *
     * @return the root {@code Descriptor} for the configured proto class
     * @throws ClassNotFoundException if no descriptor is registered for the configured proto class name
     */
    private Descriptor getRootDescriptor() throws ClassNotFoundException {
        Descriptor dsc = stencilClient.get(protoClassName);
        if (dsc == null) {
            throw new ClassNotFoundException(protoClassName);
        }
        return dsc;
    }

    /**
     * Read path.
     *
     * @param path the path
     * @return the object
     * @throws ClassNotFoundException the class not found exception
     */
    public Object read(String path) throws ClassNotFoundException {
        List<String> pathElements = Arrays.asList(path.split("\\."));
        CustomDescriptor rootCustomDescriptor = new CustomDescriptor(getRootDescriptor());
        Element lastChildElement = null;
        for (String pathElement : pathElements) {
            if (lastChildElement == null) {
                Optional<CustomDescriptor> parentCustomDescriptor = rootCustomDescriptor.get(pathOfMessage);
                if (!parentCustomDescriptor.isPresent()) {
                    return "";
                }
                Optional<Element> child = Element.initialize(null, message, parentCustomDescriptor.get(), pathElement);
                if (!child.isPresent()) {
                    return "";
                }
                lastChildElement = child.get();
            } else {
                Optional<Element> next = lastChildElement.createNext(pathElement);
                if (!next.isPresent()) {
                    return "";
                }
                lastChildElement = next.get();
            }
        }
        return lastChildElement.fetch();
    }
}
