package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.StringType;

/**
 * The String value transformer.
 */
public class StringValueTransformer implements ValueTransformer {

    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is a {@link String}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof String;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code targetType} is {@link ValueEnum#StringType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return targetType == StringType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#StringType}, as a {@link String} maps to a Feast string value
     */
    @Override
    public Integer getIndex() {
        return StringType.getValue();
    }

    /**
     * Converts the given value into the {@link String} held in the Feast string value slot.
     *
     * @param value the value to convert; expected to be non-{@code null}
     * @return the {@link String} form of {@code value} produced by its {@code toString()} method
     */
    @Override
    public Object getValue(Object value) {
        return value.toString();
    }
}
