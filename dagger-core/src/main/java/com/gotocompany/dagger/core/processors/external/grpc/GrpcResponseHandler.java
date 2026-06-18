package com.gotocompany.dagger.core.processors.external.grpc;

import com.gotocompany.dagger.core.exception.GrpcFailureException;
import com.gotocompany.dagger.core.metrics.aspects.ExternalSourceAspects;
import com.gotocompany.dagger.core.metrics.reporters.ErrorReporter;
import com.gotocompany.dagger.core.processors.ColumnNameManager;
import com.gotocompany.dagger.core.processors.common.OutputMapping;
import com.gotocompany.dagger.core.processors.common.PostResponseTelemetry;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandler;
import com.gotocompany.dagger.common.serde.typehandler.TypeHandlerFactory;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.gotocompany.dagger.core.utils.DescriptorsUtil;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.grpc.stub.StreamObserver;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.types.Row;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * The Grpc response handler.
 */
public class GrpcResponseHandler implements StreamObserver<DynamicMessage> {

    /**
     * Logger used to record gRPC response parsing and processing errors.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcResponseHandler.class.getName());
    /**
     * Manager wrapping the input and output rows that response values are written into.
     */
    private final RowManager rowManager;
    /**
     * Resolver mapping output column names to their positions in the output row.
     */
    private ColumnNameManager columnNameManager;
    /**
     * The protobuf descriptor of the output message used to type-cast response values.
     */
    private Descriptors.Descriptor descriptor;
    /**
     * The future completed with the enriched row once the response has been handled.
     */
    private ResultFuture<Row> resultFuture;
    /**
     * The gRPC source configuration describing output mappings and error behaviour.
     */
    private GrpcSourceConfig grpcSourceConfig;
    /**
     * Manager used to emit meter-style metrics for response outcomes.
     */
    private MeterStatsManager meterStatsManager;
    /**
     * The time at which the request was issued, used to compute response latency telemetry.
     */
    private Instant startTime;
    /**
     * Reporter used to surface fatal and non-fatal errors raised while handling responses.
     */
    private ErrorReporter errorReporter;
    /**
     * Helper that emits success and failure telemetry for the external call.
     */
    private PostResponseTelemetry postResponseTelemetry;

    /**
     * Instantiates a new Grpc response handler.
     *
     * @param grpcSourceConfig      the grpc source config
     * @param meterStatsManager     the meter stats manager
     * @param rowManager            the row manager
     * @param columnNameManager     the column name manager
     * @param outputDescriptor      the output descriptor
     * @param resultFuture          the result future
     * @param errorReporter         the error reporter
     * @param postResponseTelemetry the post response telemetry
     */
    public GrpcResponseHandler(GrpcSourceConfig grpcSourceConfig, MeterStatsManager meterStatsManager, RowManager rowManager, ColumnNameManager columnNameManager, Descriptors.Descriptor outputDescriptor, ResultFuture<Row> resultFuture, ErrorReporter errorReporter, PostResponseTelemetry postResponseTelemetry) {

        this.grpcSourceConfig = grpcSourceConfig;
        this.meterStatsManager = meterStatsManager;
        this.rowManager = rowManager;
        this.columnNameManager = columnNameManager;
        this.descriptor = outputDescriptor;
        this.resultFuture = resultFuture;
        this.errorReporter = errorReporter;
        this.postResponseTelemetry = postResponseTelemetry;
    }

    /**
     * Handles a successful gRPC response by extracting each configured output value and populating the row.
     *
     * <p>The response message is rendered to JSON and each configured output mapping is read via its JSON
     * path and written into the output row; path or protobuf parsing failures are reported as errors. On
     * success the result future is completed with the enriched row.
     *
     * @param message the response message returned by the gRPC service
     */
    private void successHandler(DynamicMessage message) {
        Map<String, OutputMapping> outputMappings = grpcSourceConfig.getOutputMapping();
        ArrayList<String> outputMappingKeys = new ArrayList<>(outputMappings.keySet());

        try {
            String json = JsonFormat.printer().includingDefaultValueFields().preservingProtoFieldNames().print(message);

            outputMappingKeys.forEach(key -> {
                OutputMapping outputMappingKeyConfig = outputMappings.get(key);
                Object value;
                try {

                    value = JsonPath.parse(json).read(outputMappingKeyConfig.getPath(), Object.class);

                } catch (PathNotFoundException e) {
                    postResponseTelemetry.failureReadingPath(meterStatsManager);
                    LOGGER.error(e.getMessage());
                    reportAndThrowError(e);
                    return;
                }
                int fieldIndex = columnNameManager.getOutputIndex(key);
                setField(key, value, fieldIndex);
            });
        } catch (InvalidProtocolBufferException e) {
            meterStatsManager.markEvent(ExternalSourceAspects.OTHER_ERRORS);
            LOGGER.error(e.getMessage());
            reportAndThrowError(e);
            return;
        }
        postResponseTelemetry.sendSuccessTelemetry(meterStatsManager, startTime);
        resultFuture.complete(Collections.singleton(rowManager.getAll()));

    }

    /**
     * Writes a single response value into the output row at the given index.
     *
     * <p>When the response type is not retained (or an explicit type is configured), the value is converted
     * using the field's type handler; otherwise the raw value is stored directly.
     *
     * @param key        the output column (field) name being populated
     * @param value      the raw value read from the response
     * @param fieldIndex the output row index to write the value into
     */
    private void setField(String key, Object value, int fieldIndex) {
        if (!grpcSourceConfig.isRetainResponseType() || grpcSourceConfig.hasType()) {
            setFieldUsingType(key, value, fieldIndex);
        } else {
            rowManager.setInOutput(fieldIndex, value);
        }
    }

    /**
     * Converts and writes a response value using the type handler resolved from the output descriptor.
     *
     * @param key        the output column (field) name being populated
     * @param value      the raw value read from the response
     * @param fieldIndex the output row index to write the value into
     */
    private void setFieldUsingType(String key, Object value, int fieldIndex) {
        Descriptors.FieldDescriptor fieldDescriptor = null;
        try {
            fieldDescriptor = DescriptorsUtil.getFieldDescriptor(descriptor, key);
            if (fieldDescriptor == null) {
                throw new IllegalArgumentException("Field Descriptor not found for field: " + key);
            }
        } catch (RuntimeException exception) {
            reportAndThrowError(exception);
        }
        TypeHandler typeHandler = TypeHandlerFactory.getTypeHandler(fieldDescriptor);
        rowManager.setInOutput(fieldIndex, typeHandler.transformFromPostProcessor(value));
    }


    /**
     * Reports the given exception as fatal and completes the result future exceptionally.
     *
     * @param e the exception to report and propagate
     */
    private void reportAndThrowError(Exception e) {
        errorReporter.reportFatalException(e);
        resultFuture.completeExceptionally(e);
    }


    /**
     * Failure handler.
     *
     * @param logMessage the log message
     */
    public void failureHandler(String logMessage) {
        postResponseTelemetry.sendFailureTelemetry(meterStatsManager, startTime);
        LOGGER.error(logMessage);
        Exception grpcFailureException = new GrpcFailureException(logMessage);
        if (grpcSourceConfig.isFailOnErrors()) {
            reportAndThrowError(grpcFailureException);
        } else {
            errorReporter.reportNonFatalException(grpcFailureException);
        }
        resultFuture.complete(Collections.singleton(rowManager.getAll()));
    }

    /**
     * Start timer.
     */
    public void startTimer() {
        startTime = Instant.now();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates the received response message to the success handler.
     *
     * @param message the response message emitted by the gRPC stream
     */
    @Override
    public void onNext(DynamicMessage message) {
        successHandler(message);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Records an error metric and routes the failure through the failure handler.
     *
     * @param t the error raised by the gRPC stream
     */
    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        meterStatsManager.markEvent(ExternalSourceAspects.OTHER_ERRORS);
        failureHandler(t.getMessage());

    }

    /**
     * {@inheritDoc}
     *
     * <p>No action is required when the gRPC stream completes, as results are emitted from
     * {@link #onNext(DynamicMessage)}.
     */
    @Override
    public void onCompleted() {
    }


}
