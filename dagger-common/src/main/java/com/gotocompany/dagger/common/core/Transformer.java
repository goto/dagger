package com.gotocompany.dagger.common.core;

/**
 * The interface for all the transformer.
 */
public interface Transformer {
    /**
     * Applies this transformation to the given {@link StreamInfo} and returns the result.
     *
     * <p>Implementations typically derive a new {@code DataStream} and/or column layout
     * from the input and wrap them in the returned {@link StreamInfo}.
     *
     * @param streamInfo the input stream and its column metadata to transform
     * @return the transformed stream information
     */
    StreamInfo transform(StreamInfo streamInfo);
}
