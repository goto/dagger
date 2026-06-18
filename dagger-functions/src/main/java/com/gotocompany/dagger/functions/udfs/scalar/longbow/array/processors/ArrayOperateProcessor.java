package com.gotocompany.dagger.functions.udfs.scalar.longbow.array.processors;

import com.gotocompany.dagger.functions.exceptions.ArrayOperateException;
import com.gotocompany.dagger.functions.udfs.scalar.longbow.array.expression.Expression;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;

/**
 * The Array operate processor.
 */
public class ArrayOperateProcessor extends ArrayProcessor {
    /**
     * Instantiates a new Array operate processor.
     *
     * @param expression the expression
     */
    public ArrayOperateProcessor(Expression expression) {
        super(expression);
    }

    /**
     * Instantiates a new Array operate processor.
     *
     * @param jexlEngine  the jexl engine
     * @param jexlContext the jexl context
     * @param jexlScript  the jexl script
     * @param expression  the expression
     */
    public ArrayOperateProcessor(JexlEngine jexlEngine, JexlContext jexlContext, JexlScript jexlScript, Expression expression) {
        super(jexlEngine, jexlContext, jexlScript, expression);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Executes the configured JEXL operation script against the prepared context and returns the
     * resulting array.
     *
     * @return the result produced by executing the operation script
     * @throws ArrayOperateException if the script execution fails
     */
    @Override
    public Object process() {
        try {
            return getJexlScript().execute(getJexlContext());
        } catch (RuntimeException e) {
            throw new ArrayOperateException(e);
        }
    }
}
