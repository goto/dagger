package com.gotocompany.dagger.functions.udfs.scalar;

import com.google.protobuf.ByteString;
import com.gotocompany.dagger.common.udfs.ScalarUdf;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.DataTypeFactory;
import org.apache.flink.table.functions.FunctionDefinition;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.inference.ArgumentCount;
import org.apache.flink.table.types.inference.CallContext;
import org.apache.flink.table.types.inference.ConstantArgumentCount;
import org.apache.flink.table.types.inference.InputTypeStrategy;
import org.apache.flink.table.types.inference.Signature;
import org.apache.flink.table.types.inference.TypeInference;
import org.apache.flink.table.types.inference.TypeStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Flink SQL {@link ScalarUdf} that converts a protobuf {@code ByteString} field into a UTF-8 {@code String}.
 *
 * <p>It exposes a single {@code eval} entry point and a custom {@link TypeInference} so the engine treats
 * the byte field as a {@code STRING} result.
 */
public class ByteToString extends ScalarUdf {
    /**
     * Given a ByteString, this UDF converts to String.
     *
     * @param byteField the field with byte[] in proto
     * @return string value of byteField
     */
    public String eval(ByteString byteField) {
        return byteField.toStringUtf8();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Declares that the function accepts a single argument (the byte field) and returns a
     * {@code STRING}.
     *
     * @param typeFactory the factory used to resolve Flink {@link DataType}s
     * @return the {@link TypeInference} describing input and output type strategies
     */
    @Override
    public TypeInference getTypeInference(DataTypeFactory typeFactory) {
        TypeInference build = TypeInference.newBuilder()
                .inputTypeStrategy(new ByteStringInputStrategy()).outputTypeStrategy(new ByteStringOutputStrategy())
                .build();
        return build;
    }

    /**
     * Output {@link TypeStrategy} for {@link ByteToString} that always reports a {@code STRING} result.
     */
    private static class ByteStringOutputStrategy implements TypeStrategy {
        /**
         * Infers the output type as {@code STRING}.
         *
         * @param callContext the context describing the current SQL call
         * @return an {@link Optional} containing the {@code STRING} {@link DataType}
         */
        @Override
        public Optional<DataType> inferType(CallContext callContext) {
            return Optional.of(DataTypes.STRING());
        }
    }

    /**
     * Input {@link InputTypeStrategy} for {@link ByteToString} that accepts a single argument.
     */
    private static class ByteStringInputStrategy implements InputTypeStrategy {
        /**
         * Restricts the SQL function to exactly one argument.
         *
         * @return a constant {@link ArgumentCount} of one
         */
        @Override
        public ArgumentCount getArgumentCount() {
            return ConstantArgumentCount.of(1);
        }

        /**
         * Passes through the single supplied argument data type unchanged.
         *
         * @param callContext    the context describing the current SQL call
         * @param throwOnFailure whether to raise an error when types cannot be inferred
         * @return an {@link Optional} holding the single argument {@link DataType}
         */
        @Override
        public Optional<List<DataType>> inferInputTypes(CallContext callContext, boolean throwOnFailure) {
            DataType dataType = callContext.getArgumentDataTypes().get(0);
            return Optional.of(Arrays.asList(dataType));
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
