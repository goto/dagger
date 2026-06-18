package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import com.google.protobuf.ByteString;

import static com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum.ByteType;

/**
 * The Byte value transformer.
 */
public class ByteValueTransformer implements ValueTransformer {
    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is a {@link ByteString}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return value instanceof ByteString;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type
     * @return {@code true} when {@code targetType} is {@link ValueEnum#ByteType}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return targetType == ByteType;
    }

    /**
     * Returns the Feast value slot index where the converted value is placed in the feature row.
     *
     * @return the index of {@link ValueEnum#ByteType}, as a {@link ByteString} maps to a Feast bytes value
     */
    @Override
    public Integer getIndex() {
        return ByteType.getValue();
    }
}
