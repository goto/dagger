package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.FloatType;

/**
 * The Float value transformer.
 */
public class FloatValueTransformer implements ValueTransformer {
    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is a {@link Float}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof Float;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code targetType} is {@link ValueEnum#FloatType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return targetType == FloatType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#FloatType}, as a {@link Float} maps to a Feast float value
     */
    @Override
    public Integer getIndex() {
        return FloatType.getValue();
    }

    /**
     * Converts the given value into the {@code float} held in the Feast float value slot.
     *
     * @param value the value to convert; may be {@code null}
     * @return the parsed {@code float} of {@code value}, or {@code 0.0} when {@code value} is {@code null}
     */
    @Override
    public Object getValue(Object value) {
        return value != null ? Float.valueOf(value.toString()) : 0.0f;
    }
}
