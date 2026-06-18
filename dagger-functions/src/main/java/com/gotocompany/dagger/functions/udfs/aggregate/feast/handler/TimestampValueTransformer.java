package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import org.apache.flink.types.Row;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.TimestampType;

/**
 * The Timestamp value transformer.
 */
public class TimestampValueTransformer implements ValueTransformer {

    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is a {@link Row} with an arity of {@code 2}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof Row && ((Row) value).getArity() == 2;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code value} is a {@link Row} with an arity of {@code 2} and {@code targetType}
     *         is {@link ValueEnum#TimestampType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return value instanceof Row && ((Row) value).getArity() == 2 && targetType == ValueEnum.TimestampType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#TimestampType}, as a two-field {@link Row} maps to a Feast timestamp value
     */
    @Override
    public Integer getIndex() {
        return TimestampType.getValue();
    }
}
