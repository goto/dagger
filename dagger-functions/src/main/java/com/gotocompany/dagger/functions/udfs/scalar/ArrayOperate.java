package com.gotocompany.dagger.functions.udfs.scalar;


import com.gotocompany.dagger.common.udfs.ScalarUdf;
import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.LongbowArrayType;
import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.expression.OperationExpression;
import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.processors.ArrayOperateProcessor;
import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.processors.ArrayProcessor;
import org.apache.flink.api.java.typeutils.GenericTypeInfo;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.DataTypeFactory;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.FunctionDefinition;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.UnresolvedDataType;
import org.apache.flink.table.types.inference.ArgumentCount;
import org.apache.flink.table.types.inference.CallContext;
import org.apache.flink.table.types.inference.ConstantArgumentCount;
import org.apache.flink.table.types.inference.InputTypeStrategy;
import org.apache.flink.table.types.inference.Signature;
import org.apache.flink.table.types.inference.TypeInference;
import org.apache.flink.table.types.inference.TypeStrategy;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * The ArrayOperate udf.
 */
public class ArrayOperate extends ScalarUdf implements Serializable {
    /**
     * String representation of the configured operation expression.
     */
    private String expressionString;

    /**
     * Processor that applies the configured element-wise operation to the supplied array; created lazily on open.
     */
    private ArrayProcessor arrayProcessor;

    /**
     * Holds the JEXL expression describing the operation applied to each array element.
     */
    private OperationExpression expression;

    /**
     * Instantiates a new Array operate.
     */
    public ArrayOperate() {
        this.expression = new OperationExpression();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily instantiates the {@link ArrayProcessor} as an {@code ArrayOperateProcessor} backed by the
     * configured {@link OperationExpression} the first time Flink opens this function instance.
     *
     * @param context the Flink function context supplied during initialisation
     * @throws Exception if the superclass fails to open
     */
    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
        if (arrayProcessor == null) {
            arrayProcessor = new ArrayOperateProcessor(expression);
        }
    }

    /**
     * Given an Object Array, this UDF performs basic functions on the Array.
     *
     * @param operationType the operation type
     * @param inputDataType the input data type
     * @param arrayElements the array elements
     * @return the result of the aggregate
     */

   public Object[] eval(Object[] arrayElements, String operationType, String inputDataType) {
        expression.createExpression(operationType);
        LongbowArrayType dataType = LongbowArrayType.getDataType(inputDataType);
        arrayProcessor.initJexl(dataType, arrayElements);
        return getCopyArray(arrayProcessor.process());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wires up the input strategy ({@code ARRAY}, operation {@code STRING}, data-type {@code STRING})
     * and the output strategy (an {@code ARRAY} of raw {@code Object}) for this SQL function.
     *
     * @param typeFactory the factory used to resolve Flink {@link DataType}s
     * @return the {@link TypeInference} describing input and output type strategies
     */
    @Override
    public TypeInference getTypeInference(DataTypeFactory typeFactory) {
        TypeInference build = TypeInference.newBuilder()
                .inputTypeStrategy(new ArrayOperateInputTypeStrategy())
                .outputTypeStrategy(new ArrayOperateOutputStrategy())
                .build();
        return build;
    }

    /**
     * Copies an arbitrary (possibly primitive) array into a new {@code Object[]} via reflection.
     *
     * <p>This normalises the processor output, which may be a primitive array, into a boxed object array
     * suitable for returning to Flink SQL.
     *
     * @param originalArray the source array, reflectively accessed element by element
     * @return a new {@code Object[]} containing the same elements as {@code originalArray}
     */
    private Object[] getCopyArray(Object originalArray) {
        int arrayLen = Array.getLength(originalArray);
        return IntStream.range(0, arrayLen).mapToObj(i -> Array.get(originalArray, i)).toArray();
    }

    /**
     * Input {@link InputTypeStrategy} for {@link ArrayOperate} that fixes the call to three arguments.
     *
     * <p>The arguments are the input array, the operation name and the element data type.
     */
    private static class ArrayOperateInputTypeStrategy implements InputTypeStrategy {
        /**
         * The exact number of arguments accepted by the {@code ArrayOperate} SQL function.
         */
        private static final Integer ARRAY_OPERATE_UDF_FUNCTION_ARG_COUNT = 3;

        /**
         * Restricts the SQL function to exactly three arguments.
         *
         * @return a constant {@link ArgumentCount} of three
         */
        @Override
        public ArgumentCount getArgumentCount() {
            return ConstantArgumentCount.of(ARRAY_OPERATE_UDF_FUNCTION_ARG_COUNT);
        }

        /**
         * Resolves the argument types to an {@code ARRAY} of raw {@code Object} followed by two
         * {@code STRING}s (the operation type and the element data type).
         *
         * @param callContext    the context describing the current SQL call
         * @param throwOnFailure whether to raise an error when types cannot be inferred
         * @return an {@link Optional} holding the resolved list of argument {@link DataType}s
         */
        @Override
        public Optional<List<DataType>> inferInputTypes(CallContext callContext, boolean throwOnFailure) {
            DataTypeFactory dataTypeFactory = callContext.getDataTypeFactory();
            UnresolvedDataType unresolvedArgOneType = DataTypes.ARRAY(DataTypes.RAW(new GenericTypeInfo<>(Object.class)));
            DataType resolvedArgOneType = dataTypeFactory.createDataType(unresolvedArgOneType);
            return Optional.of(Arrays.asList(new DataType[]{resolvedArgOneType, DataTypes.STRING(), DataTypes.STRING()}));
        }

        /**
         * {@inheritDoc}
         *
         * <p>This UDF does not advertise explicit call signatures.
         *
         * @param definition the Flink function definition
         * @return {@code null}, as no fixed signatures are declared
         */
        @Override
        public List<Signature> getExpectedSignatures(FunctionDefinition definition) {
            return null;
        }

    }

    /**
     * Output {@link TypeStrategy} for {@link ArrayOperate} describing the SQL result type.
     *
     * <p>The result is reported as an {@code ARRAY} of raw {@code Object} values.
     */
    private static class ArrayOperateOutputStrategy implements TypeStrategy {
            /**
             * Infers the output type as an {@code ARRAY} of raw {@code Object}.
             *
             * @param callContext the context describing the current SQL call
             * @return an {@link Optional} containing the array {@link DataType}
             */
            @Override
            public Optional<DataType> inferType(CallContext callContext) {
            DataTypeFactory dataTypeFactory = callContext.getDataTypeFactory();
            UnresolvedDataType unresolvedDataType = DataTypes.ARRAY(DataTypes.RAW(new GenericTypeInfo<>(Object.class)));
            DataType dataType = dataTypeFactory.createDataType(unresolvedDataType);
            return Optional.of(dataType);
        }
    }
}
