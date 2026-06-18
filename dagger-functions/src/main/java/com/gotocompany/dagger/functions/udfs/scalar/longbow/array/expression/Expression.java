package com.gotocompany.dagger.functions.udfs.scalar.longbow.array.expression;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The interface Expression.
 */
public interface Expression extends Serializable {
    /**
     * The name of the JEXL context variable that holds the input stream operated on by expressions.
     */
    String BASE_STRING = "stream";

    /**
     * Gets expression string.
     *
     * @return the expression string
     */
    String getExpressionString();

    /**
     * Create expression.
     *
     * @param operationType the operation type
     */
    void createExpression(String operationType);

    /**
     * Gets operation expression.
     *
     * @param operationType the operation type
     * @return the operation expression
     */
    default String getOperationExpression(String operationType) {
        String[] operations;
        if (operationType.contains(".")) {
            operations = operationType.split("\\.");
        } else {
            operations = new String[]{operationType};
        }
        return Arrays
                .stream(operations)
                .map(operation -> "." + operation + "()")
                .collect(Collectors.joining());
    }
}
