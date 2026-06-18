package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.DoubleType;

/**
 * The Double value transformer.
 */
public class DoubleValueTransformer implements ValueTransformer {

    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is a {@link Double}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof Double;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code targetType} is {@link ValueEnum#DoubleType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return targetType == DoubleType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#DoubleType}, as a {@link Double} maps to a Feast double value
     */
    @Override
    public Integer getIndex() {
        return DoubleType.getValue();
    }

    /**
     * Converts the given value into the {@code double} held in the Feast double value slot.
     *
     * @param value the value to convert; may be {@code null}
     * @return the parsed {@code double} of {@code value}, or {@code 0.0} when {@code value} is {@code null}
     */
    @Override
    public Object getValue(Object value) {
        return value != null ? Double.valueOf(value.toString()) : 0.0d;
    }
}
