package com.gotocompany.dagger.functions.udfs.scalar;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.InputGroup;
import org.apache.flink.table.functions.FunctionContext;

import com.gotocompany.stencil.client.StencilClient;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.udfs.ScalarUdf;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * The Filters udf.
 */
public class Filters extends ScalarUdf {

    /**
     * Orchestrator used to obtain the {@link StencilClient} for protobuf descriptor lookups.
     */
    private StencilClientOrchestrator stencilClientOrchestrator;

    /**
     * Stencil client used to resolve protobuf descriptors; supplied directly or via the orchestrator.
     */
    private StencilClient stencilClient;

    /**
     * Instantiates a new Filters.
     *
     * @param stencilClientOrchestrator the stencil client orchestrator
     */
    public Filters(StencilClientOrchestrator stencilClientOrchestrator) {
        this.stencilClientOrchestrator = stencilClientOrchestrator;
    }

    /**
     * Instantiates a new Filters.
     *
     * @param stencilClient the stencil client
     */
    public Filters(StencilClient stencilClient) {
        this.stencilClient = stencilClient;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the {@link StencilClient} used to look up protobuf descriptors when the UDF is opened.
     *
     * @param context the Flink function context supplied during initialisation
     * @throws Exception if the superclass fails to open
     */
    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
        stencilClient = getStencilClient();
    }

    /**
     * This is one of the UDFs related to LongbowPlus and has to be used with SelectFields and CondEq UDFs.
     * Takes input as zero or more Predicates (we have only CondEq as a predicate that is defined for now).
     * Applies the predicated conditions on the proto ByteString list field that is selected from the query and returns filtered Data.
     *
     * @param inputProtoBytes the input proto bytes
     * @param protoClassName  the proto class name
     * @param predicates      the predicates
     * @return the filtered Data
     * @throws ClassNotFoundException         the class not found exception
     * @throws InvalidProtocolBufferException the invalid protocol buffer exception
     * @author arujit
     * @team DE
     */
    @DataTypeHint(value = "RAW", bridgedTo = List.class)
    public List<DynamicMessage> eval(@DataTypeHint(inputGroup = InputGroup.ANY) Object inputProtoBytes, String protoClassName,
                                     @DataTypeHint(value = "RAW", bridgedTo = Predicate.class) Predicate<DynamicMessage>... predicates) throws ClassNotFoundException, InvalidProtocolBufferException {
        if (!(inputProtoBytes instanceof ByteString[])) {
            return null;
        }
        ByteString[] inputData = (ByteString[]) inputProtoBytes;
        Descriptors.Descriptor descriptor = getDescriptor(protoClassName);
        LinkedList output = new LinkedList<>();

        for (ByteString byteString : inputData) {
            DynamicMessage dynamicMessage = DynamicMessage.parseFrom(descriptor, byteString);
            if (testDynamicMessage(dynamicMessage, predicates)) {
                output.add(dynamicMessage);
            }
        }
        return output;
    }

    /**
     * Tests a single {@code DynamicMessage} against all supplied predicates, requiring every predicate
     * to pass.
     *
     * <p>Evaluation short-circuits on the first failing predicate; the message is accepted only when the
     * number of satisfied predicates equals the total number supplied.
     *
     * @param dynamicMessage the decoded protobuf message to evaluate
     * @param predicates     the predicates to apply; a message must satisfy all of them
     * @return {@code true} if the message satisfies every predicate, otherwise {@code false}
     */
    @SafeVarargs
    private final boolean testDynamicMessage(DynamicMessage dynamicMessage, Predicate<DynamicMessage>... predicates) {
        int counter = 0;
        for (Predicate predicate : predicates) {
            if (predicate.test(dynamicMessage)) {
                counter++;
            } else {
                break;
            }
        }
        return counter == predicates.length;
    }

    /**
     * Resolves the protobuf {@link Descriptors.Descriptor} for the given class name via the Stencil client.
     *
     * @param protoClassName the fully-qualified protobuf message class name
     * @return the resolved descriptor for {@code protoClassName}
     * @throws ClassNotFoundException if no descriptor is registered for {@code protoClassName}
     */
    private Descriptors.Descriptor getDescriptor(String protoClassName) throws ClassNotFoundException {
        Descriptors.Descriptor descriptor = getStencilClient().get(protoClassName);
        if (descriptor == null) {
            throw new ClassNotFoundException(protoClassName);
        }
        return descriptor;
    }

    /**
     * Gets stencil client.
     *
     * @return the stencil client
     */
    StencilClient getStencilClient() {
        if (stencilClient == null) {
            return stencilClientOrchestrator.getStencilClient();
        }
        return stencilClient;
    }
}
