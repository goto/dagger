package com.gotocompany.dagger.functions.udfs.scalar;

import com.gotocompany.dagger.common.udfs.ScalarUdf;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.DataTypeFactory;
import org.apache.flink.table.functions.FunctionDefinition;
import org.apache.flink.table.types.AtomicDataType;
import org.apache.flink.table.types.CollectionDataType;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.FieldsDataType;
import org.apache.flink.table.types.inference.ArgumentCount;
import org.apache.flink.table.types.inference.CallContext;
import org.apache.flink.table.types.inference.ConstantArgumentCount;
import org.apache.flink.table.types.inference.InputTypeStrategy;
import org.apache.flink.table.types.inference.Signature;
import org.apache.flink.table.types.inference.TypeInference;
import org.apache.flink.table.types.inference.TypeStrategy;
import org.apache.flink.types.Row;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The Map get udf.
 */
public class MapGet extends ScalarUdf {
    /**
     * returns value for a corresponding key inside a map data type.
     *
     * @param inputMap the input map element
     * @param key      the key
     * @return the value
     * @author Ujjawal
     * @team Fraud
     */

    public Object eval(Row[] inputMap, Object key) {
        List<Row> rows = Arrays.asList(inputMap);
        Optional<Row> requiredRow = rows.stream().filter(row -> row.getField(0).equals(key)).findFirst();
        return requiredRow.map(row -> row.getField(1)).orElse(null);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Declares the input strategy (a map represented as an {@code ARRAY} of key/value {@code ROW}s plus
     * a key argument) and an output strategy that yields the map's value type.
     *
     * @param typeFactory the factory used to resolve Flink {@link DataType}s
     * @return the {@link TypeInference} describing input and output type strategies
     */
    @Override
    public TypeInference getTypeInference(DataTypeFactory typeFactory) {
        return TypeInference.newBuilder()
                .inputTypeStrategy(new MapGetInputTypeStrategy())
                .outputTypeStrategy(new MapOutputTypeStrategy())
                .build();
    }

    /**
     * Output {@link TypeStrategy} for {@link MapGet} that reports the map's value type as the result type.
     */
    private static class MapOutputTypeStrategy implements TypeStrategy {
        /**
         * Infers the output type as the value (second) field of the map's key/value row.
         *
         * @param callContext the context describing the current SQL call
         * @return an {@link Optional} containing the value {@link DataType} of the map
         */
        @Override
        public Optional<DataType> inferType(CallContext callContext) {
            CollectionDataType firstArgumentDataType = (CollectionDataType) callContext.getArgumentDataTypes().get(0);
            FieldsDataType elementDataType = (FieldsDataType) firstArgumentDataType.getElementDataType();
            List<DataType> children = elementDataType.getChildren();
            return Optional.of(children.get(1));
        }
    }

    /**
     * Input {@link InputTypeStrategy} for {@link MapGet} accepting the map and the lookup key.
     */
    private static class MapGetInputTypeStrategy implements InputTypeStrategy {
        /**
         * Restricts the SQL function to exactly two arguments.
         *
         * @return a constant {@link ArgumentCount} of two
         */
        @Override
        public ArgumentCount getArgumentCount() {
            return ConstantArgumentCount.of(2);
        }

        /**
         * Resolves the argument types to an {@code ARRAY} of key/value {@code ROW}s (derived from the map)
         * followed by the key's data type.
         *
         * @param callContext    the context describing the current SQL call
         * @param throwOnFailure whether to raise an error when types cannot be inferred
         * @return an {@link Optional} holding the resolved list of argument {@link DataType}s
         */
        @Override
        public Optional<List<DataType>> inferInputTypes(CallContext callContext, boolean throwOnFailure) {
            CollectionDataType firstArgumentDataType = (CollectionDataType) callContext.getArgumentDataTypes().get(0);
            FieldsDataType elementDataType = (FieldsDataType) firstArgumentDataType.getElementDataType();
            List<DataType> children = elementDataType.getChildren();
            AtomicDataType secondArgumentDataType = (AtomicDataType) callContext.getArgumentDataTypes().get(1);
            DataType mapDataType = DataTypes.ARRAY(DataTypes.ROW(children.get(0), children.get(1)));
            return Optional.of(Arrays.asList(mapDataType, secondArgumentDataType));
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
