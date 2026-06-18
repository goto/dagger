package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.LongType;

/**
 * The Long value transformer.
 */
public class LongValueTransformer implements ValueTransformer {

    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is a {@link Long}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof Long;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code targetType} is {@link ValueEnum#LongType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return targetType == LongType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#LongType}, as a {@link Long} maps to a Feast long value
     */
    @Override
    public Integer getIndex() {
        return LongType.getValue();
    }

    /**
     * Converts the given value into the {@code long} held in the Feast long value slot.
     *
     * @param value the value to convert; may be {@code null}
     * @return the parsed {@code long} of {@code value}, or {@code 0L} when {@code value} is {@code null}
     */
    @Override
    public Object getValue(Object value) {
        return value != null ? Long.valueOf(value.toString()) : 0L;
    }
}
