package com.gotocompany.dagger.core.source.parquet.path;

import org.apache.flink.core.fs.Path;

import java.text.ParseException;
import java.time.Instant;

/**
 * Extracts an event-time {@link Instant} from a Parquet file {@link Path} based on its partition
 * layout.
 *
 * <p>Implementations decode the date/hour partition encoded in the file path so that the bounded
 * Parquet source can order file splits chronologically (see {@code ChronologyOrderedSplitAssigner}).
 */
public interface PathParser {

    /**
     * Parses the timestamp encoded in the given file path's partition segments.
     *
     * @param path the Parquet file path to parse
     * @return the {@link Instant} represented by the path's date/hour partition
     * @throws ParseException if the path does not conform to a recognised partitioning scheme
     */
    Instant instantFromFilePath(Path path) throws ParseException;
}
