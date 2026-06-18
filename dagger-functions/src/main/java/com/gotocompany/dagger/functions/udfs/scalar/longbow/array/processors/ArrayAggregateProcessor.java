package com.gotocompany.dagger.functions.udfs.scalar.longbow.array.processors;

import com.gotocompany.dagger.functions.exceptions.ArrayAggregationException;
import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.expression.Expression;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * The Array aggregate processor.
 */
public class ArrayAggregateProcessor extends ArrayProcessor {
    /**
     * Instantiates a new Array aggregate processor.
     *
     * @param expression the expression
     */
    public ArrayAggregateProcessor(Expression expression) {
        super(expression);
    }

    /**
     * Instantiates a new Array aggregate processor.
     *
     * @param jexlEngine  the jexl engine
     * @param jexlContext the jexl context
     * @param jexlScript  the jexl script
     * @param expression  the expression
     */
    public ArrayAggregateProcessor(JexlEngine jexlEngine, JexlContext jexlContext, JexlScript jexlScript, Expression expression) {
        super(jexlEngine, jexlContext, jexlScript, expression);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Executes the configured JEXL aggregation script against the prepared context and unwraps any
     * {@code Optional}-typed numeric result into a concrete value.
     *
     * @return the aggregation result, or {@code 0} when an empty optional numeric result is produced
     * @throws ArrayAggregationException if the script execution fails
     */
    @Override
    public Object process() {
        try {
            Object result = getJexlScript().execute(getJexlContext());
            return getValueFromOptionalOutput(result);
        } catch (RuntimeException e) {
            throw new ArrayAggregationException(e);
        }
    }

    /**
     * Unwraps an {@code OptionalDouble}, {@code OptionalInt}, or {@code OptionalLong} result into a concrete value.
     *
     * @param result the raw script execution result
     * @return the unwrapped numeric value defaulting to {@code 0} when the optional is empty, or the original result when it is not optional
     */
    private Object getValueFromOptionalOutput(Object result) {
        if (result instanceof OptionalDouble) {
            return ((OptionalDouble) result).orElse(0);
        } else if (result instanceof OptionalInt) {
            return ((OptionalInt) result).orElse(0);
        } else if (result instanceof OptionalLong) {
            return ((OptionalLong) result).orElse(0);
        } else {
            return result;
        }
    }
}
