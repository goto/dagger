package com.gotocompany.dagger.common.serde.proto.deserialization;

import com.gotocompany.dagger.common.exceptions.DescriptorNotFoundException;
import com.gotocompany.dagger.common.serde.typehandler.TypeInformationFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.types.Row;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.serde.DaggerInternalTypeInformation;

import java.io.Serializable;

/**
 * Getting TypeInformation required for Flink from the proto.
 */
public class ProtoType implements Serializable, DaggerInternalTypeInformation {

    /**
     * The cached, lazily-resolved protobuf {@link Descriptor} for {@code protoClassName};
     * marked {@code transient} because descriptors are not serializable.
     */
    private transient Descriptor protoFieldDescriptor;
    /**
     * The fully-qualified protobuf class name whose schema drives the row type.
     */
    private String protoClassName;
    /**
     * The name of the attribute that carries the Flink rowtime (event-time) field.
     */
    private String rowtimeAttributeName;
    /**
     * The orchestrator used to obtain the Stencil client that resolves proto descriptors.
     */
    private StencilClientOrchestrator stencilClientOrchestrator;

    /**
     * Instantiates a new Proto type.
     *
     * @param protoClassName            the proto class name
     * @param rowtimeAttributeName      the rowtime attribute name
     * @param stencilClientOrchestrator the stencil client orchestrator
     */
    public ProtoType(String protoClassName, String rowtimeAttributeName, StencilClientOrchestrator stencilClientOrchestrator) {
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.protoClassName = protoClassName;
        this.rowtimeAttributeName = rowtimeAttributeName;
    }

    /**
     * Gets row type info.
     *
     * @return the row type info
     */
    public TypeInformation<Row> getRowType() {
        TypeInformation<Row> rowNamed = TypeInformationFactory.getRowType(getProtoFieldDescriptor());
        return addInternalFields(rowNamed, rowtimeAttributeName);
    }

    /**
     * Returns the protobuf {@link Descriptor}, resolving and caching it on first access.
     *
     * @return the proto field descriptor for {@code protoClassName}
     */
    private Descriptor getProtoFieldDescriptor() {
        if (protoFieldDescriptor == null) {
            protoFieldDescriptor = createFieldDescriptor();
        }
        return protoFieldDescriptor;
    }

    /**
     * Resolves the protobuf {@link Descriptor} for {@code protoClassName} via the Stencil client.
     *
     * @return the resolved descriptor
     * @throws DescriptorNotFoundException if no descriptor is registered for {@code protoClassName}
     */
    private Descriptor createFieldDescriptor() {
        Descriptors.Descriptor dsc = stencilClientOrchestrator.getStencilClient().get(protoClassName);
        if (dsc == null) {
            throw new DescriptorNotFoundException();
        }
        return dsc;
    }
}
