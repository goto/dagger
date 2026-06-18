package com.gotocompany.dagger.functions.udfs.scalar;

import com.gotocompany.dagger.functions.udfs.scalar.elementAt.MessageReader;
import com.gotocompany.stencil.client.StencilClient;
import com.gotocompany.dagger.common.core.StencilClientOrchestrator;
import com.gotocompany.dagger.common.udfs.ScalarUdf;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.DataTypeFactory;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.FunctionDefinition;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.inference.ArgumentCount;
import org.apache.flink.table.types.inference.CallContext;
import org.apache.flink.table.types.inference.InputTypeStrategy;
import org.apache.flink.table.types.inference.Signature;
import org.apache.flink.table.types.inference.TypeInference;
import org.apache.flink.table.types.inference.TypeStrategy;
import org.apache.flink.types.Row;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * The ElementAt udf.
 */
public class ElementAt extends ScalarUdf {
    /**
     * Mapping of table name to its protobuf class name, used to resolve the descriptor for array elements.
     */
    private LinkedHashMap<String, String> protos;

    /**
     * Orchestrator used to obtain the {@link StencilClient} for protobuf descriptor lookups.
     */
    private final StencilClientOrchestrator stencilClientOrchestrator;

    /**
     * Cached Stencil client resolved on open and reused for descriptor lookups.
     */
    private StencilClient stencilClient;

    /**
     * Maximum number of arguments accepted by the {@code ElementAt} SQL function.
     */
    private static final int MAX_ARG_COUNT = 5;

    /**
     * Minimum number of arguments accepted by the {@code ElementAt} SQL function.
     */
    private static final int MINIMUM_ARG_COUNT = 2;

    /**
     * Argument count used when the function is applied to a single-table (non-join) query.
     */
    private static final int ARG_COUNT_WHEN_SINGLE_TABLE_QUERY = 4;


    /**
     * Instantiates a new Element at.
     *
     * @param protos                    the protos
     * @param stencilClientOrchestrator the stencil client orchestrator
     */
    public ElementAt(LinkedHashMap<String, String> protos, StencilClientOrchestrator stencilClientOrchestrator) {
        this.protos = protos;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves and caches the {@link StencilClient} on first open so protobuf descriptors can be
     * looked up while evaluating array elements.
     *
     * @param context the Flink function context supplied during initialisation
     * @throws Exception if the superclass fails to open
     */
    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
        if (stencilClient == null) {
            stencilClient = getStencilClient();
        }
    }

    /**
     * finds out the element at a given index and a given path in an array of complex Datatypes.
     *
     * @param array       the array
     * @param pathOfArray the path of array
     * @param index       the index
     * @param path        the path on element in the array
     * @return the value
     * @author gaurav.s
     * @team DE
     */
    public String eval(Row[] array, String pathOfArray, int index, String path) {
        try {
            if (array == null || array.length <= index) {
                return "";
            }
            String protoClassName = protos.entrySet().iterator().next().getValue();
            MessageReader messageReader = new MessageReader(array[index], protoClassName, pathOfArray, stencilClient);
            Object value = messageReader.read(path);
            return String.valueOf(value);

        } catch (Exception ignored) {

        }
        return "";
    }

    /**
     * for the given table, finds out the element at a given index and a given path in an array of complex Datatypes.
     *
     * @param array       the array
     * @param pathOfArray the path of array
     * @param index       the index
     * @param path        the path on element in the array
     * @param tableName   name of the table to apply udf on (in case of multiple streams / JOINS)
     * @return the value
     * @author mayur.gubrele
     * @team DE
     */
    public String eval(Row[] array, String pathOfArray, int index, String path, String tableName) {
        try {
            if (array == null || array.length <= index) {
                return "";
            }
            String protoClassName = protos.get(tableName);
            MessageReader messageReader = new MessageReader(array[index], protoClassName, pathOfArray, stencilClient);
            Object value = messageReader.read(path);
            return String.valueOf(value);

        } catch (Exception ignored) {

        }
        return "";
    }

    /**
     * finds the element at a given position for an object ArrayList.
     *
     * @param arrayList the array list
     * @param index     the index
     * @return the value
     * @author mayur.gubrele
     * @team DE
     */
    public Object eval(ArrayList<Object> arrayList, int index) {
        return eval(arrayList.toArray(), index);
    }

    /**
     * finds the element at a given position for an object Array. supports -ve index
     *
     * @param array the array
     * @param index the index
     * @return the value
     * @author arujit
     * @team DE
     */
    public Object eval(Object[] array, int index) {
        try {
            if (array == null || array.length <= index) {
                return null;
            }
            int arrayLength = array.length;
            if (index < 0) {
                int absoluteIndex = arrayLength + index;
                if (absoluteIndex < 0) {
                    return null;
                }
                return getString(array[arrayLength + index]);
            }
            return getString(array[index]);
        } catch (Exception ignored) {

        }
        return null;
    }

    /**
     * Gets stencil client.
     *
     * @return the stencil client
     */
    public StencilClient getStencilClient() {
        if (stencilClient != null) {
            return stencilClient;
        }
        return stencilClientOrchestrator.getStencilClient();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Configures the variable-arity input strategy (two, four or five arguments) and a {@code STRING}
     * output type for this SQL function.
     *
     * @param typeFactory the factory used to resolve Flink {@link DataType}s
     * @return the {@link TypeInference} describing input and output type strategies
     */
    @Override
    public TypeInference getTypeInference(DataTypeFactory typeFactory) {
        return TypeInference.newBuilder()
                .outputTypeStrategy(new ElementAtOutputTypeStrategy())
                .inputTypeStrategy(new ElementAtInputTypeStrategy())
                .build();
    }

    /**
     * Output {@link TypeStrategy} for {@link ElementAt} that always reports a {@code STRING} result.
     */
    private static class ElementAtOutputTypeStrategy implements TypeStrategy {
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
     * Null-safely converts a value to its {@code String} representation.
     *
     * @param value the value to stringify; may be {@code null}
     * @return the string form of {@code value}, or {@code null} when {@code value} is {@code null}
     */
    private String getString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Input {@link InputTypeStrategy} for {@link ElementAt} accepting two, four or five arguments.
     *
     * <p>Two arguments select from a plain object array, four arguments target a single table, and five
     * arguments additionally specify the table name for joined streams.
     */
    private static class ElementAtInputTypeStrategy implements InputTypeStrategy {
        /**
         * Builds an {@link ArgumentCount} that accepts the two-, four- or five-argument forms of the UDF.
         *
         * @return an {@link ArgumentCount} validating the supported argument counts
         */
        @Override
        public ArgumentCount getArgumentCount() {
            return new ArgumentCount() {


                @Override
                public boolean isValidCount(int count) {

                    return count == MINIMUM_ARG_COUNT || count == ARG_COUNT_WHEN_SINGLE_TABLE_QUERY || count == MAX_ARG_COUNT;
                }

                @Override
                public Optional<Integer> getMinCount() {
                    return Optional.of(MINIMUM_ARG_COUNT);
                }

                @Override
                public Optional<Integer> getMaxCount() {
                    return Optional.of(MAX_ARG_COUNT);
                }
            };
        }

        /**
         * Resolves the argument types depending on the call arity: the supplied types for the two-argument
         * form, or the array type followed by {@code STRING}/{@code INT} positional types for the four- and
         * five-argument forms.
         *
         * @param callContext    the context describing the current SQL call
         * @param throwOnFailure whether to raise an error when types cannot be inferred
         * @return an {@link Optional} holding the resolved list of argument {@link DataType}s
         */
        @Override
        public Optional<List<DataType>> inferInputTypes(CallContext callContext, boolean throwOnFailure) {
            List<DataType> argumentDataTypes = callContext.getArgumentDataTypes();
            int argumentSize = argumentDataTypes.size();
            if (argumentSize == MINIMUM_ARG_COUNT) {
                return Optional.of(callContext.getArgumentDataTypes());
            }
            if (argumentSize == ARG_COUNT_WHEN_SINGLE_TABLE_QUERY) {
                return Optional.of(Arrays.asList(argumentDataTypes.get(0), DataTypes.STRING(), DataTypes.INT(), DataTypes.STRING()));
            }
            return Optional.of(Arrays.asList(argumentDataTypes.get(0), DataTypes.STRING(), DataTypes.INT(), DataTypes.STRING(), DataTypes.STRING()));
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
