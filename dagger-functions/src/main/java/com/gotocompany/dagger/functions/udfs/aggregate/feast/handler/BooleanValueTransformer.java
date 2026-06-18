package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.BooleanType;

/**
 * The Boolean value transformer.
 */
public class BooleanValueTransformer implements ValueTransformer {
    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is a {@link Boolean}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof Boolean;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code targetType} is {@link ValueEnum#BooleanType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return targetType == BooleanType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#BooleanType}, as a {@link Boolean} maps to a Feast boolean value
     */
    @Override
    public Integer getIndex() {
        return BooleanType.getValue();
    }

    /**
     * Converts the given value into the {@link Boolean} held in the Feast boolean value slot.
     *
     * @param value the value to convert; may be {@code null}
     * @return {@code value} when it is non-{@code null}, otherwise {@code false}
     */
    @Override
    public Object getValue(Object value) {
        return value != null ? value : false;
    }
}
