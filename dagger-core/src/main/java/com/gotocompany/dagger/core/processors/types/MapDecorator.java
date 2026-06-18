package com.gotocompany.dagger.core.processors.types;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

/**
 * The interface Map decorator.
 */
public interface MapDecorator extends MapFunction<Row, Row>, StreamDecorator {

    /**
     * Decorates the given stream by applying this map function to each record.
     *
     * <p>{@inheritDoc}
     *
     * @param inputStream the input stream of {@link Row} records to map
     * @return a data stream with this map function applied to every record
     */
    @Override
    default DataStream<Row> decorate(DataStream<Row> inputStream) {
        return inputStream.map(this);
    }

}
