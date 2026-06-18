package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.IntegerType;

/**
 * The Integer value transformer.
 */
public class IntegerValueTransformer implements ValueTransformer {


    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is an {@link Integer}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof Integer;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code targetType} is {@link ValueEnum#IntegerType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return targetType == IntegerType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#IntegerType}, as an {@link Integer} maps to a Feast integer value
     */
    @Override
    public Integer getIndex() {
        return IntegerType.getValue();
    }

    /**
     * Converts the given value into the {@code int} held in the Feast integer value slot.
     *
     * @param value the value to convert; may be {@code null}
     * @return the parsed {@code int} of {@code value}, or {@code 0} when {@code value} is {@code null}
     */
    @Override
    public Object getValue(Object value) {
        return value != null ? Integer.valueOf(value.toString()) : 0;
    }
}
