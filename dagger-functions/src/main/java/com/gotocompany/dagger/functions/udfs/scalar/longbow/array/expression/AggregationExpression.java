package com.gotocompany.dagger.functions.udfs.scalar.longbow.array.expression;

/**
 * The Aggregation expression.
 */
public class AggregationExpression implements Expression {
    /**
     * The JEXL expression string that this aggregation builds and exposes for evaluation.
     */
    private String expressionString;

    /**
     * {@inheritDoc}
     *
     * @return the JEXL expression string built for this aggregation
     */
    @Override
    public String getExpressionString() {
        return expressionString;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds an aggregation expression by appending the operation chain for the given operation type
     * to the base stream variable.
     *
     * @param operationType the dot-separated operation chain to apply (for example {@code "sum"})
     */
    @Override
    public void createExpression(String operationType) {
        this.expressionString = BASE_STRING + getOperationExpression(operationType);
    }
}
