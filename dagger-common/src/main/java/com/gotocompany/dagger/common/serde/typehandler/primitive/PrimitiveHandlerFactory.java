package com.gotocompany.dagger.common.serde.typehandler.primitive;

import com.gotocompany.dagger.common.exceptions.serde.DataTypeNotSupportedException;
import com.google.protobuf.Descriptors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The factory class for Primitive type handler.
 */
public class PrimitiveHandlerFactory {
    /**
     * Gets type handler.
     *
     * @param fieldDescriptor the field descriptor
     * @return the type handler
     */
    public static PrimitiveHandler getTypeHandler(Descriptors.FieldDescriptor fieldDescriptor) {
        Optional<PrimitiveHandler> filteredTypeHandlers =
                getSpecificHandlers(fieldDescriptor)
                        .stream()
                        .filter(PrimitiveHandler::canHandle)
                        .findFirst();
        return filteredTypeHandlers.orElseThrow(() -> new DataTypeNotSupportedException("Data type " + fieldDescriptor.getJavaType() + " not supported in primitive type handlers"));
    }

    /**
     * Builds the ordered list of candidate primitive handlers for the given field.
     *
     * <p>{@code getTypeHandler} selects the first handler whose {@code canHandle()} returns
     * {@code true}, so the order determines which handler claims each Java type.
     *
     * @param fieldDescriptor the field descriptor to build candidate handlers for
     * @return the ordered list of candidate primitive handlers to try
     */
    private static List<PrimitiveHandler> getSpecificHandlers(Descriptors.FieldDescriptor fieldDescriptor) {
        return Arrays.asList(
                new IntegerHandler(fieldDescriptor),
                new BooleanHandler(fieldDescriptor),
                new DoubleHandler(fieldDescriptor),
                new FloatHandler(fieldDescriptor),
                new LongHandler(fieldDescriptor),
                new StringHandler(fieldDescriptor),
                new ByteStringHandler(fieldDescriptor)
        );
    }
}
