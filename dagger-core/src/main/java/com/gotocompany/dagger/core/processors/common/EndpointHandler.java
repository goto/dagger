package com.gotocompany.dagger.core.processors.common;

import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.utils.Constants.ExternalPostProcessorVariableType;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.exceptions.DescriptorNotFoundException;
import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.core.metrics.aspects.ExternalSourceAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.google.protobuf.Descriptors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singleton;

/**
 * The Endpoint handler.
 */
public class EndpointHandler {
    /**
     * Logger used to record diagnostic information during endpoint variable resolution.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointHandler.class.getName());
    /**
     * Meter stats manager used to record external source metrics such as empty inputs.
     */
    private MeterStatsManager meterStatsManager;
    /**
     * Reporter used to surface fatal errors encountered while resolving descriptors.
     */
    private ErrorReporter errorReporter;
    /**
     * The fully qualified names of the input Protobuf classes used to locate field descriptors.
     */
    private String[] inputProtoClasses;
    /**
     * Lazily built mapping from input column name to its Protobuf {@link Descriptors.FieldDescriptor}.
     */
    private Map<String, Descriptors.FieldDescriptor> descriptorMap;
    /**
     * Manager that resolves input and output column indices by name.
     */
    private ColumnNameManager columnNameManager;
    /**
     * Manager that resolves Protobuf descriptors by proto class name.
     */
    private DescriptorManager descriptorManager;
    /**
     * The most recently resolved input Protobuf message descriptor.
     */
    private Descriptors.Descriptor descriptor;

    /**
     * Instantiates a new Endpoint handler.
     *
     * @param sourceConfig      the source config
     * @param meterStatsManager the meter stats manager
     * @param errorReporter     the error reporter
     * @param inputProtoClasses the input proto classes
     * @param columnNameManager the column name manager
     * @param descriptorManager the descriptor manager
     */
    public EndpointHandler(MeterStatsManager meterStatsManager,
                           ErrorReporter errorReporter,
                           String[] inputProtoClasses,
                           ColumnNameManager columnNameManager,
                           DescriptorManager descriptorManager) {
        this.meterStatsManager = meterStatsManager;
        this.errorReporter = errorReporter;
        this.inputProtoClasses = inputProtoClasses;
        this.columnNameManager = columnNameManager;
        this.descriptorManager = descriptorManager;
    }

    /**
     * Get external post processor variables values.
     *
     * @param rowManager   the row manager
     * @param variableType the variable type
     * @parm variables     the variable list
     * @param resultFuture the result future
     * @return the array object
     */
    public Object[] getVariablesValue(RowManager rowManager, ExternalPostProcessorVariableType variableType, String variables, ResultFuture<Row> resultFuture) {
        if (StringUtils.isEmpty(variables)) {
            return new Object[0];
        }

        String[] requiredInputColumns = variables.split(",");
        ArrayList<Object> inputColumnValues = new ArrayList<>();
        if (descriptorMap == null) {
            descriptorMap = createDescriptorMap(requiredInputColumns, inputProtoClasses, resultFuture);
        }

        for (String inputColumnName : requiredInputColumns) {
            int inputColumnIndex = columnNameManager.getInputIndex(inputColumnName);
            if (inputColumnIndex == -1) {
                throw new InvalidConfigurationException(String.format("Column '%s' not found as configured in the '%s' variable", inputColumnName, variableType));
            }

            Descriptors.FieldDescriptor fieldDescriptor = descriptorMap.get(inputColumnName);
            Object singleColumnValue;
            if (fieldDescriptor != null) {
                TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(fieldDescriptor);
                singleColumnValue = typeHandler.transformToJson(rowManager.getFromInput(inputColumnIndex));
            } else {
                singleColumnValue = rowManager.getFromInput(inputColumnIndex);
            }
            inputColumnValues.add(singleColumnValue);
        }
        return inputColumnValues.toArray();
    }

    /**
     * Check if the query is invalid.
     *
     * @param resultFuture            the result future
     * @param rowManager              the row manager
     * @param variables               the request/header variables
     * @param variablesValue          the variables value
     * @return the boolean
     */
    public boolean isQueryInvalid(ResultFuture<Row> resultFuture, RowManager rowManager, String variables, Object[] variablesValue) {
        if (!StringUtils.isEmpty(variables) && (Arrays.asList(variablesValue).isEmpty() || Arrays.stream(variablesValue).allMatch(""::equals))) {
            LOGGER.warn("Could not populate any request variable. Skipping external calls");
            meterStatsManager.markEvent(ExternalSourceAspects.EMPTY_INPUT);
            resultFuture.complete(singleton(rowManager.getAll()));
            return true;
        }
        return false;
    }

    /**
     * Builds a lookup from required input column names to their Protobuf field descriptors.
     *
     * <p>For each required column the method scans every configured input proto class and records
     * the first matching {@link Descriptors.FieldDescriptor} found. Columns without a matching field
     * are simply omitted from the returned map.
     *
     * @param requiredInputColumns the input column names that need descriptors
     * @param inputProtoClassNames the proto class names to search for matching fields
     * @param resultFuture         the result future completed exceptionally if a descriptor is missing
     * @return a map from column name to its matching field descriptor
     */
    private Map<String, Descriptors.FieldDescriptor> createDescriptorMap(String[] requiredInputColumns,
                                                                         String[] inputProtoClassNames,
                                                                         ResultFuture<Row> resultFuture) {
        HashMap<String, Descriptors.FieldDescriptor> descriptorHashMap = new HashMap<>();
        Descriptors.Descriptor currentDescriptor;
        for (String columnName : requiredInputColumns) {
            for (String protoClassName : inputProtoClassNames) {
                currentDescriptor = getInputDescriptor(resultFuture, protoClassName);
                Descriptors.FieldDescriptor currentFieldDescriptor = currentDescriptor.findFieldByName(columnName);
                if (currentFieldDescriptor != null && descriptorHashMap.get(columnName) == null) {
                    descriptorHashMap.put(columnName, currentFieldDescriptor);
                }
            }
        }
        return descriptorHashMap;
    }

    /**
     * Resolves the Protobuf message descriptor for the given proto class name.
     *
     * <p>If the descriptor cannot be found the underlying error is reported and propagated through
     * the supplied result future.
     *
     * @param resultFuture   the result future completed exceptionally when the descriptor is missing
     * @param protoClassName the fully qualified proto class name to resolve
     * @return the resolved message descriptor, or the previously held descriptor if resolution failed
     */
    private Descriptors.Descriptor getInputDescriptor(ResultFuture<Row> resultFuture, String protoClassName) {
        try {
            descriptor = descriptorManager.getDescriptor(protoClassName);
        } catch (DescriptorNotFoundException descriptorNotFound) {
            reportAndThrowError(resultFuture, descriptorNotFound);
        }
        return descriptor;
    }

    /**
     * Reports the given exception as fatal and completes the result future exceptionally.
     *
     * @param resultFuture the result future to complete exceptionally
     * @param exception    the exception to report and propagate
     */
    private void reportAndThrowError(ResultFuture<Row> resultFuture, Exception exception) {
        errorReporter.reportFatalException(exception);
        resultFuture.completeExceptionally(exception);
    }
}
