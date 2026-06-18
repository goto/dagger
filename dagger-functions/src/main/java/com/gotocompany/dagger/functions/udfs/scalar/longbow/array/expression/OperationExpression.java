package com.gotocompany.dagger.functions.udfs.scalar.longbow.array.expression;

/**
 * The Operation expression.
 */
public class OperationExpression implements Expression {
    /**
     * The trailing JEXL fragment that materialises the operated stream back into an array.
     */
    public static final String CONVERT_TO_ARRAY = ".toArray()";
    /**
     * The JEXL expression string that this operation builds and exposes for evaluation.
     */
    private String expressionString;

    /**
     * {@inheritDoc}
     *
     * @return the JEXL expression string built for this operation
     */
    @Override
    public String getExpressionString() {
        return expressionString;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds an operation expression by appending the operation chain for the given operation type
     * to the base stream variable and converting the result back into an array.
     *
     * @param operationType the dot-separated operation chain to apply (for example {@code "distinct"})
     */
    @Override
    public void createExpression(String operationType) {
        this.expressionString = BASE_STRING + getOperationExpression(operationType) + CONVERT_TO_ARRAY;
    }
}
