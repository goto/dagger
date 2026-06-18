package com.gotocompany.dagger.core.processors.external.grpc.client;

import com.gotocompany.dagger.core.exception.InvalidGrpcBodyException;
import com.gotocompany.dagger.core.processors.common.DescriptorManager;
import com.gotocompany.dagger.core.processors.external.grpc.GrpcSourceConfig;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;

/**
 * The Grpc request handler.
 */
public class GrpcRequestHandler {

    /**
     * The gRPC source configuration providing the request pattern and request proto schema.
     */
    private GrpcSourceConfig grpcSourceConfig;
    /**
     * Resolver used to look up the protobuf descriptor for the request message.
     */
    private DescriptorManager descriptorManager;

    /**
     * Instantiates a new Grpc request handler.
     *
     * @param grpcSourceConfig  the grpc source config
     * @param descriptorManager the descriptor manager
     */
    public GrpcRequestHandler(GrpcSourceConfig grpcSourceConfig, DescriptorManager descriptorManager) {
        this.grpcSourceConfig = grpcSourceConfig;
        this.descriptorManager = descriptorManager;
    }

    /**
     * Create dynamic message.
     *
     * @param requestVariablesValues the request variables values
     * @return the dynamic message
     */
    public DynamicMessage create(Object[] requestVariablesValues) {
        String requestBody = String.format(grpcSourceConfig.getPattern(), requestVariablesValues).replaceAll("'", "\"");

        try {

            DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptorManager.getDescriptor(grpcSourceConfig.getGrpcRequestProtoSchema()));
            JsonFormat.parser().merge(requestBody, builder);

            return builder.build();

        } catch (Exception e) {
            throw new InvalidGrpcBodyException(e.getMessage());
        }

    }

}
