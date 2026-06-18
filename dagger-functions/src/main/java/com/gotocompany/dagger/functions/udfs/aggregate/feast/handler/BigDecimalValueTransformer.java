package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import java.math.BigDecimal;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.DoubleType;

/**
 * The Big decimal value transformer.
 */
public class BigDecimalValueTransformer implements ValueTransformer {
    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is a {@link BigDecimal}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof BigDecimal;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code value} is a {@link BigDecimal} and {@code targetType} is
     *         {@link ValueEnum#DoubleType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return value instanceof BigDecimal && targetType == DoubleType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#DoubleType}, as a {@link BigDecimal} maps to a Feast double value
     */
    @Override
    public Integer getIndex() {
        return DoubleType.getValue();
    }

    /**
     * Converts the given {@link BigDecimal} into the {@code double} held in the Feast double value slot.
     *
     * @param value the {@link BigDecimal} to convert; may be {@code null}
     * @return the {@code double} value of {@code value}, or {@code 0.0} when {@code value} is {@code null}
     */
    @Override
    public Object getValue(Object value) {
        if (value == null) {
            return 0.0D;
        }
        BigDecimal bigDecimalValue = (BigDecimal) value;
        return bigDecimalValue.doubleValue();
    }
}
