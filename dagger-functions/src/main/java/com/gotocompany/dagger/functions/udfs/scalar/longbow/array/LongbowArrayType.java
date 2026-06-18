package com.gotocompany.dagger.functions.udfs.scalar.longbow.array;

import com.gotocompany.dagger.functions.exceptions.ArrayAggregationException;

import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

/**
 * The enum Data type.
 */
public enum LongbowArrayType implements Serializable {
    /**
     * Casts the array elements to {@code int} values for aggregation.
     */
    INTEGER((Stream<Object> stream) -> (stream.mapToInt(Integer.class::cast))),
    /**
     * Casts the array elements to {@code int} values for aggregation (alias of {@code INTEGER}).
     */
    INT((Stream<Object> stream) -> (stream.mapToInt(Integer.class::cast))),
    /**
     * Casts the array elements to {@code double} values for aggregation.
     */
    DOUBLE((Stream<Object> stream) -> (stream.mapToDouble(Double.class::cast))),
    /**
     * Casts the array elements (originally {@code float}) to {@code double} values for aggregation.
     */
    FLOAT((Stream<Object> stream) -> (stream.mapToDouble(Float.class::cast))),
    /**
     * Casts the array elements to {@code long} values for aggregation.
     */
    LONG((Stream<Object> stream) -> (stream.mapToLong(Long.class::cast))),
    /**
     * Casts the array elements to {@code long} values for aggregation (alias of {@code LONG}).
     */
    BIGINT((Stream<Object> stream) -> (stream.mapToLong(Long.class::cast))),
    /**
     * Leaves the array elements unconverted, passing the object stream through unchanged.
     */
    OTHER((Stream<Object> stream) -> (stream));

    /**
     * The function that casts a stream of array elements to the primitive-typed stream used for aggregation.
     */
    private Function<Stream<Object>, BaseStream> inputCastingFunction;

    /**
     * Instantiates a new Longbow array type.
     *
     * @param inputCastingFunction the function that casts an object stream to the appropriate primitive stream
     */
    LongbowArrayType(Function<Stream<Object>, BaseStream> inputCastingFunction) {
        this.inputCastingFunction = inputCastingFunction;
    }

    /**
     * Gets data type.
     *
     * @param inputDataType the input data type
     * @return the data type
     */
    public static LongbowArrayType getDataType(String inputDataType) {
        String typeInUpperCaseCase = inputDataType.toUpperCase();
        try {
            return LongbowArrayType.valueOf(typeInUpperCaseCase);
        } catch (IllegalArgumentException e) {
            throw new ArrayAggregationException("No support for inputDataType: "
                    + inputDataType
                    + ".Please provide 'Other' as inputDataType instead.");
        }
    }

    /**
     * Gets input casting function.
     *
     * @return the input casting function
     */
    public Function<Stream<Object>, BaseStream> getInputCastingFunction() {
        return inputCastingFunction;
    }

}
