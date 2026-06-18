package com.gotocompany.dagger.functions.transformers;

import com.gotocompany.dagger.common.core.DaggerContext;
import com.gotocompany.dagger.functions.transformers.filter.FilterAspects;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.common.core.Transformer;
import com.gotocompany.dagger.common.metrics.managers.CounterStatsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;


/**
 * Filter the invalid records produced by dagger.
 */
public class InvalidRecordFilterTransformer extends RichFilterFunction<Row> implements Transformer {
    /**
     * Name of the table whose invalid records this filter counts, used as a metric tag.
     */
    private final String tableName;
    /**
     * Index, within the configured column names, of the internal validation flag column.
     */
    private final int validationIndex;
    /**
     * Manager used to count the number of invalid records filtered out for this table.
     */
    private CounterStatsManager metricsManager = null;
    /**
     * Logger used to report how many invalid records have been filtered.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InvalidRecordFilterTransformer.class.getName());

    /**
     * Name of the internal column that carries the per-record validation flag.
     */
    protected static final String INTERNAL_VALIDATION_FILED = "__internal_validation_field__";
    /**
     * Metric tag name used to group the filtered-record counter by table.
     */
    private static final String PER_TABLE = "per_table";

    /**
     * Instantiates a new Invalid record filter transformer.
     *
     * @param transformationArguments the transformation arguments
     * @param columnNames             the column names
     * @param daggerContext           the daggerContext
     */
    public InvalidRecordFilterTransformer(Map<String, Object> transformationArguments, String[] columnNames, DaggerContext daggerContext) {
        this.tableName = (String) transformationArguments.getOrDefault("table_name", "");
        validationIndex = Arrays.asList(columnNames).indexOf(INTERNAL_VALIDATION_FILED);
    }

    /**
     * Registers the invalid-record counter metric when the operator starts.
     *
     * <p>Obtains the metric group from the runtime context and registers a counter for filtered invalid
     * records, tagged with the configured table name.
     *
     * @param internalFlinkConfig the Flink configuration supplied by the runtime
     * @throws Exception if metric registration fails
     */
    @Override
    public void open(org.apache.flink.configuration.Configuration internalFlinkConfig) throws Exception {
        MetricGroup metricGroup = getRuntimeContext().getMetricGroup();
        metricsManager = new CounterStatsManager(metricGroup);
        metricsManager.register(FilterAspects.FILTERED_INVALID_RECORDS, PER_TABLE, tableName);
    }

    /**
     * Keeps only records that are marked valid by the internal validation flag.
     *
     * <p>When the validation flag is {@code false} the invalid-record counter is incremented, a log line
     * is emitted and the record is dropped, otherwise the record is kept.
     *
     * @param value the record being evaluated
     * @return {@code true} if the record is valid and should be kept, {@code false} if it is invalid
     */
    @Override
    public boolean filter(Row value) {
        if (!(boolean) value.getField(validationIndex)) {
            metricsManager.inc(FilterAspects.FILTERED_INVALID_RECORDS);
            LOGGER.info("Filtering invalid record for table "
                    + this.tableName + "\n"
                    + "Total = ", metricsManager.getCount(FilterAspects.FILTERED_INVALID_RECORDS));
            return false;
        }
        return true;
    }

    /**
     * Wires this filter into the streaming pipeline.
     *
     * <p>Applies this {@link RichFilterFunction} over the input data stream and returns a new
     * {@link StreamInfo} that preserves the original column names.
     *
     * @param streamInfo the incoming stream and its column metadata
     * @return a {@link StreamInfo} wrapping the filtered data stream with the original column names
     */
    @Override
    public StreamInfo transform(StreamInfo streamInfo) {
        return new StreamInfo(
                streamInfo.getDataStream().filter(this),
                streamInfo.getColumnNames());
    }
}
