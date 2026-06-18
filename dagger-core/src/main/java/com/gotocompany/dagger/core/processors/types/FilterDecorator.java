package com.gotocompany.dagger.core.processors.types;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

/**
 * The interface Filter decorator.
 */
public interface FilterDecorator extends FilterFunction<Row>, StreamDecorator {

    /**
     * Decorates the given stream by applying this filter to it.
     *
     * <p>{@inheritDoc}
     *
     * @param inputStream the input stream of {@link Row} records to filter
     * @return a data stream retaining only the records that satisfy this filter
     */
    @Override
    default DataStream<Row> decorate(DataStream<Row> inputStream) {
        return inputStream.filter(this);
    }

}
