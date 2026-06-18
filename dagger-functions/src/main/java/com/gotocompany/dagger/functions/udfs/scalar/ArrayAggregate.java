package com.gotocompany.dagger.functions.udfs.scalar;

import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.LongbowArrayType;
import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.expression.AggregationExpression;
import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.processors.ArrayAggregateProcessor;
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

import com.gotocompany.dagger.common.udfs.ScalarUdf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The ArrayAggregate udf.
 */
public class ArrayAggregate extends ScalarUdf implements Serializable {

    /**
     * Processor that evaluates the configured aggregation over the supplied array; created lazily on open.
     */
    private ArrayProcessor arrayProcessor;

    /**
     * Holds the JEXL expression describing the aggregation function (for example {@code sum} or {@code avg}).
     */
    private AggregationExpression expression;

    /**
     * Instantiates a new Array aggregate.
     */
    public ArrayAggregate() {
        this.expression = new AggregationExpression();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily instantiates the {@link ArrayProcessor} as an {@code ArrayAggregateProcessor} backed by
     * the configured {@link AggregationExpression} the first time Flink opens this function instance.
     *
     * @param context the Flink function context supplied during initialisation
     * @throws Exception if the superclass fails to open
     */
    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
        if (arrayProcessor == null) {
            arrayProcessor = new ArrayAggregateProcessor(expression);
        }
    }

    /**
     * Given an Object Array, this UDF performs basic mathematical functions on the Array.
     *
     * @param arrayElements input object array
     * @param operationType the aggregation function in string
     * @param inputDataType data type of object in the given array
     * @return the result of aggregate
     * @author arujit
     * @team DE
     */
    public Object eval(Object[] arrayElements, String operationType, String inputDataType) {
        expression.createExpression(operationType);
        LongbowArrayType longbowArrayType = LongbowArrayType.getDataType(inputDataType);
        arrayProcessor.initJexl(longbowArrayType, arrayElements);
        return arrayProcessor.process();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wires up the input strategy ({@code ARRAY}, operation {@code STRING}, data-type {@code STRING})
     * and the output strategy (a raw {@code Object}) used to type-check and plan this SQL function.
     *
     * @param typeFactory the factory used to resolve Flink {@link DataType}s
     * @return the {@link TypeInference} describing input and output type strategies
     */
    @Override
    public TypeInference getTypeInference(DataTypeFactory typeFactory) {
        TypeInference build = TypeInference.newBuilder()
                .inputTypeStrategy(new ArrayAggregateInputStrategy())
                .outputTypeStrategy(new ArrayAggregateOutputStrategy())
                .build();
        return build;
    }


    /**
     * Output {@link TypeStrategy} for {@link ArrayAggregate} describing the SQL result type.
     *
     * <p>The aggregation may produce any numeric or object value, so the result is reported as a raw
     * {@code Object} data type.
     */
    private static class ArrayAggregateOutputStrategy implements TypeStrategy {
        /**
         * Infers the output type as a raw {@code Object} data type.
         *
         * @param callContext the context describing the current SQL call
         * @return an {@link Optional} containing the raw {@code Object} {@link DataType}
         */
        @Override
        public Optional<org.apache.flink.table.types.DataType> inferType(CallContext callContext) {
            DataTypeFactory dataTypeFactory = callContext.getDataTypeFactory();
            UnresolvedDataType opUnresolvedType = DataTypes.RAW(new GenericTypeInfo<>(Object.class));
            DataType opDataType = dataTypeFactory.createDataType(opUnresolvedType);
            return Optional.of(opDataType);
        }
    }

    /**
     * Input {@link InputTypeStrategy} for {@link ArrayAggregate} that fixes the call to three arguments.
     *
     * <p>The arguments are the input array, the aggregation operation name and the element data type.
     */
    private static class ArrayAggregateInputStrategy implements InputTypeStrategy {
        /**
         * The exact number of arguments accepted by the {@code ArrayAggregate} SQL function.
         */
        private static final Integer ARRAY_AGGREGATE_UDF_FUNCTION_ARG_COUNT = 3;

        /**
         * Restricts the SQL function to exactly three arguments.
         *
         * @return a constant {@link ArgumentCount} of three
         */
        @Override
        public ArgumentCount getArgumentCount() {
            return ConstantArgumentCount.of(ARRAY_AGGREGATE_UDF_FUNCTION_ARG_COUNT);
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
            return Optional.of(Arrays.asList(new org.apache.flink.table.types.DataType[]{resolvedArgOneType, DataTypes.STRING(), DataTypes.STRING()}));
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
}
