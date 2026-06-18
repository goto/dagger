package com.gotocompany.dagger.core.sink.influx;

import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporterFactory;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.flink.api.connector.sink.Committer;
import org.apache.flink.api.connector.sink.GlobalCommitter;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.types.Row;

import com.gotocompany.dagger.common.configuration.Configuration;
import org.influxdb.InfluxDB;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Flink {@link Sink} implementation that writes Dagger output {@link Row} records to InfluxDB.
 *
 * <p>This is Dagger's default output sink. When a writer is created it opens a batched
 * {@link InfluxDB} connection (using the configured URL and credentials), wires in an
 * {@link ErrorHandler} to classify the connection's asynchronous batch-write failures, and
 * delegates per-row point construction and writing to an {@link InfluxDBWriter}. Optional
 * {@link InfluxSinkOverrides} let a job override the measurement name and retention policy per sink.
 * Like the other Dagger sinks it is stateless, exposing no committer, global committer or state
 * serializer (all return {@link Optional#empty()}).
 */
public class InfluxDBSink implements Sink<Row, Void, Void, Void> {
    private InfluxDBFactoryWrapper influxDBFactory;
    private Configuration configuration;
    private String[] columnNames;
    private ErrorHandler errorHandler;
    private ErrorReporter errorReporter;
    private final InfluxSinkOverrides overrides;

    /**
     * Creates an InfluxDB sink.
     *
     * @param influxDBFactory the factory used to open the {@link InfluxDB} connection when a writer
     *                       is created
     * @param configuration   the job configuration supplying the Influx URL, credentials, database
     *                       name, batching and measurement settings
     * @param columnNames     the output column names used to map row fields to Influx tags/fields
     *                       when row field names are not used
     * @param errorHandler    the handler that captures and classifies asynchronous batch-write errors
     * @param overrides       optional measurement-name/retention-policy overrides; when {@code null}
     *                       it falls back to {@link InfluxSinkOverrides#none()}
     */
    public InfluxDBSink(InfluxDBFactoryWrapper influxDBFactory, Configuration configuration, String[] columnNames,
                        ErrorHandler errorHandler, InfluxSinkOverrides overrides) {
        this.influxDBFactory = influxDBFactory;
        this.configuration = configuration;
        this.columnNames = columnNames;
        this.errorHandler = errorHandler;
        this.overrides = overrides == null ? InfluxSinkOverrides.none() : overrides;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Opens an {@link InfluxDB} connection from the configured URL, username and password,
     * initializes the {@link ErrorHandler} with the sink context, and enables client-side batching
     * (batch size and flush interval from configuration) so failures are dispatched to the handler's
     * exception callback. An {@link ErrorReporter} is created lazily from the metric group, and the
     * method returns an {@link InfluxDBWriter} bound to these collaborators and any overrides.
     *
     * @param context the Flink sink init context, used to initialize the error handler and metrics
     * @param states  the previously checkpointed writer states; ignored because this sink keeps no state
     * @return a new {@link InfluxDBWriter}
     * @throws IOException if the writer cannot be created
     */
    @Override
    public SinkWriter<Row, Void, Void> createWriter(InitContext context, List<Void> states) throws IOException {
        InfluxDB influxDB = influxDBFactory.connect(configuration.getString(Constants.SINK_INFLUX_URL_KEY, Constants.SINK_INFLUX_URL_DEFAULT),
                configuration.getString(Constants.SINK_INFLUX_USERNAME_KEY, Constants.SINK_INFLUX_USERNAME_DEFAULT),
                configuration.getString(Constants.SINK_INFLUX_PASSWORD_KEY, Constants.SINK_INFLUX_PASSWORD_DEFAULT));
        errorHandler.init(context);
        influxDB.enableBatch(configuration.getInteger(Constants.SINK_INFLUX_BATCH_SIZE_KEY, Constants.SINK_INFLUX_BATCH_SIZE_DEFAULT),
                configuration.getInteger(Constants.SINK_INFLUX_FLUSH_DURATION_MS_KEY, Constants.SINK_INFLUX_FLUSH_DURATION_MS_DEFAULT),
                TimeUnit.MILLISECONDS, Executors.defaultThreadFactory(), errorHandler.getExceptionHandler());
        if (errorReporter == null) {
            errorReporter = ErrorReporterFactory.getErrorReporter(context.metricGroup(), configuration);
        }

        InfluxDBWriter influxDBWriter = new InfluxDBWriter(configuration, influxDB, columnNames, errorHandler, errorReporter, overrides);
        return influxDBWriter;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link Optional#empty()} because this sink keeps no writer state to checkpoint.
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getWriterStateSerializer() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link Optional#empty()}; durability is handled by the batched InfluxDB client, so
     * no Flink committer is required.
     */
    @Override
    public Optional<Committer<Void>> createCommitter() throws IOException {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link Optional#empty()}; this sink has no global commit phase.
     */
    @Override
    public Optional<GlobalCommitter<Void, Void>> createGlobalCommitter() throws IOException {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link Optional#empty()} because there are no committables to serialize.
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getCommittableSerializer() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link Optional#empty()} because there are no global committables to serialize.
     */
    @Override
    public Optional<SimpleVersionedSerializer<Void>> getGlobalCommittableSerializer() {
        return Optional.empty();
    }
}
