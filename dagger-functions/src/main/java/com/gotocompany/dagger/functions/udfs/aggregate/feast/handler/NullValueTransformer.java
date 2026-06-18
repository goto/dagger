package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

import com.gotocompany.dagger.functions.common.Constants;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.flink.types.Row;

/**
 * The Null value transformer.
 */
public class NullValueTransformer implements ValueTransformer {

    /**
     * Determines whether this transformer can convert the given value into a Feast value.
     *
     * @param value the candidate value to inspect
     * @return {@code true} when {@code value} is {@code null}, otherwise {@code false}
     */
    @Override
    public boolean canTransform(Object value) {
        return null == value;
    }

    /**
     * Determines whether this transformer can convert the given value into the requested Feast target type.
     *
     * @param value      the candidate value to inspect
     * @param targetType the desired Feast value type, ignored for {@code null} values
     * @return {@code true} when {@code value} is {@code null}, otherwise {@code false}
     */
    @Override
    public boolean canTransformWithTargetType(Object value, ValueEnum targetType) {
        return null == value;
    }

    /**
     * Reports that no Feast value slot index applies to a {@code null} value.
     *
     * @return never returns normally
     * @throws NotImplementedException always, since a {@code null} value has no Feast value slot index
     */
    @Override
    public Integer getIndex() {
        throw new NotImplementedException("Index for Null Value shouldn't be used");
    }

    /**
     * Builds an empty Feast value row for a {@code null} value, leaving every value slot unset.
     *
     * @param value the value to transform; expected to be {@code null}
     * @return a new empty {@link Row} sized to {@link Constants#NUMBER_OF_DATA_TYPES_IN_FEATURE_ROW}
     */
    @Override
    public Row transform(Object value) {
        return new Row(Constants.NUMBER_OF_DATA_TYPES_IN_FEATURE_ROW);
    }
}
