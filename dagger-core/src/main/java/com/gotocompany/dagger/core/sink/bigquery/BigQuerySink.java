package com.gotocompany.dagger.core.sink.bigquery;

import com.google.common.base.Splitter;
import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.serde.proto.serialization.ProtoSerializer;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporterFactory;
import com.gotocompany.dagger.core.metrics.reporters.statsd.DaggerStatsDReporter;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.depot.bigquery.BigQuerySinkFactory;
import com.gotocompany.depot.config.BigQuerySinkConfig;
import com.gotocompany.depot.error.ErrorType;
import org.aeonbits.owner.ConfigFactory;
import org.apache.flink.api.connector.sink.Committer;
import org.apache.flink.api.connector.sink.GlobalCommitter;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A Flink {@link Sink} implementation that writes Dagger output {@link Row} records to BigQuery.
 *
 * <p>Each row is serialized with a {@link ProtoSerializer} and handed to the depot
 * {@code BigQuerySinkFactory}, which performs the actual batched writes. This sink does not rely on
 * Flink's committer or global-committer mechanism, so all committer-related factory methods return
 * {@link Optional#empty()}.
 */
public class BigQuerySink implements Sink<Row, Void, Void, Void> {
    /** Serializes each output {@link Row} into protobuf key and value bytes. */
    private final ProtoSerializer protoSerializer;
    /** Dagger configuration supplying BigQuery sink settings, batch size and parameters. */
    private final Configuration configuration;
    /** Reporter used to build the StatsD instrumentation passed to the depot sink factory. */
    private final DaggerStatsDReporter daggerStatsDReporter;
    /** Lazily-initialized depot factory that creates the underlying BigQuery sink; not serialized. */
    private transient BigQuerySinkFactory sinkFactory;

    /**
     * Instantiates a new BigQuery sink without a pre-built sink factory.
     *
     * <p>The {@code BigQuerySinkFactory} is created lazily when the first writer is opened.
     *
     * @param configuration        the Dagger configuration carrying BigQuery sink settings
     * @param protoSerializer      the serializer that converts rows into protobuf messages
     * @param daggerStatsDReporter the StatsD reporter used for sink instrumentation
     */
    protected BigQuerySink(Configuration configuration, ProtoSerializer protoSerializer, DaggerStatsDReporter daggerStatsDReporter) {
        this(configuration, protoSerializer, null, daggerStatsDReporter);
    }

    /**
     * Constructor for testing.
     */
    protected BigQuerySink(Configuration configuration, ProtoSerializer protoSerializer, BigQuerySinkFactory sinkFactory, DaggerStatsDReporter daggerStatsDReporter) {
        this.configuration = configuration;
        this.protoSerializer = protoSerializer;
        this.sinkFactory = sinkFactory;
        this.daggerStatsDReporter = daggerStatsDReporter;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lazily initializes the depot {@code BigQuerySinkFactory} on first invocation, resolves the
     * configured batch size and the set of {@code ErrorType}s that should fail the job, and returns a
     * {@link BigQuerySinkWriter} bound to a freshly created depot sink. Initialization failures are
     * reported as fatal exceptions before being rethrown.
     *
     * @param context the sink initialization context providing the metric group
     * @param states  the restored writer states; unused because this sink keeps no state
     * @return a new {@link BigQuerySinkWriter} that batches and writes rows to BigQuery
     */
    @Override
    public SinkWriter<Row, Void, Void> createWriter(InitContext context, List<Void> states) {
        ErrorReporter errorReporter = ErrorReporterFactory.getErrorReporter(context.metricGroup(), configuration);
        if (sinkFactory == null) {
            BigQuerySinkConfig sinkConfig = ConfigFactory.create(BigQuerySinkConfig.class, configuration.getParam().toMap());
            sinkFactory = new BigQuerySinkFactory(sinkConfig, daggerStatsDReporter.buildStatsDReporter());
            try {
                sinkFactory.init();
            } catch (Exception e) {
                errorReporter.reportFatalException(e);
                throw e;
            }
        }
        com.gotocompany.depot.Sink sink = sinkFactory.create();
        int batchSize = configuration.getInteger(
                Constants.SINK_BIGQUERY_BATCH_SIZE,
                Constants.SINK_BIGQUERY_BATCH_SIZE_DEFAULT);
        String errorsForFailing = configuration.getString(
                Constants.SINK_ERROR_TYPES_FOR_FAILURE,
                Constants.SINK_ERROR_TYPES_FOR_FAILURE_DEFAULT);
        Set<ErrorType> errorTypesForFailing = new HashSet<>();
        for (String s : Splitter.on(",").omitEmptyStrings().split(errorsForFailing)) {
            errorTypesForFailing.add(ErrorType.valueOf(s.trim()));
        }
        return new BigQuerySinkWriter(protoSerializer, sink, batchSize, errorReporter, errorTypesForFailing);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This sink keeps no writer state, so no serializer is provided.
     *
     * @return an empty {@link Optional}
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getWriterStateSerializer() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>BigQuery writes are committed inline by the writer, so no Flink committer is used.
     *
     * @return an empty {@link Optional}
     * @throws IOException never thrown by this implementation
     */
    @Override
    public Optional<Committer<Void>> createCommitter() throws IOException {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>No global commit phase is required for the BigQuery sink.
     *
     * @return an empty {@link Optional}
     * @throws IOException never thrown by this implementation
     */
    @Override
    public Optional<GlobalCommitter<Void, Void>> createGlobalCommitter() throws IOException {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>No committables are produced, so no serializer is provided.
     *
     * @return an empty {@link Optional}
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getCommittableSerializer() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>No global committables are produced, so no serializer is provided.
     *
     * @return an empty {@link Optional}
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getGlobalCommittableSerializer() {
        return Optional.empty();
    }
}
