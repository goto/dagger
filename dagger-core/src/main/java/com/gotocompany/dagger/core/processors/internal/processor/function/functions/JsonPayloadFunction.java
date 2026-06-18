package com.gotocompany.dagger.core.processors.internal.processor.function.functions;

import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.common.SchemaConfig;
import com.gotocompany.dagger.core.processors.internal.processor.function.FunctionProcessor;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;
import com.gotocompany.dagger.common.serde.typehandler.TypeInformationFactory;

import com.google.protobuf.Descriptors;
import com.gotocompany.stencil.client.StencilClient;
import org.apache.flink.formats.json.JsonRowSerializationSchema;

import java.util.Map;
import java.io.Serializable;

/**
 * Internal post-processor function that serialises the incoming record to its JSON representation.
 *
 * <p>Selected when an internal source of type {@code function} has the value {@code JSON_PAYLOAD}.
 * The proto class named under {@link #SCHEMA_PROTO_CLASS_KEY} (resolved through the stencil client)
 * drives the {@link JsonRowSerializationSchema} that converts the input {@code Row} to JSON.
 */
public class JsonPayloadFunction implements FunctionProcessor, Serializable {
    /** The configured function value that selects this function. */
    public static final String JSON_PAYLOAD_FUNCTION_KEY = "JSON_PAYLOAD";
    /** Key, within the internal processor config, naming the proto class used to build the JSON schema. */
    public static final String SCHEMA_PROTO_CLASS_KEY = "schema_proto_class";

    /** The internal source configuration providing the nested processor settings. */
    private InternalSourceConfig internalSourceConfig;
    /** Schema/runtime context used to obtain the stencil client and proto descriptors. */
    private SchemaConfig schemaConfig;
    /** Lazily-built schema converting the input row into JSON; cached after first use. */
    private JsonRowSerializationSchema jsonRowSerializationSchema;

    /**
     * Instantiates a new JsonPayloadFunction processor.
     *
     * @param internalSourceConfig the internal source config
     * @param schemaConfig         the schema config
     */
    public JsonPayloadFunction(InternalSourceConfig internalSourceConfig, SchemaConfig schemaConfig) {
        this.internalSourceConfig = internalSourceConfig;
        this.schemaConfig = schemaConfig;
    }

    /**
     * Indicates whether this function handles the supplied function name.
     *
     * @param functionName the configured function value to match
     * @return {@code true} when {@code functionName} equals {@link #JSON_PAYLOAD_FUNCTION_KEY}, {@code false} otherwise
     */
    @Override
    public boolean canProcess(String functionName) {
        return JSON_PAYLOAD_FUNCTION_KEY.equals(functionName);
    }

    /**
     * Gets payload in JSON.
     *
     * @return the incoming message as JSON
     */
    @Override
    public Object getResult(RowManager rowManager) {
        if (jsonRowSerializationSchema == null) {
            jsonRowSerializationSchema = createJsonRowSerializationSchema();
        }
        return new String(jsonRowSerializationSchema.serialize(rowManager.getInputData()));
    }

    /**
     * Builds the {@link JsonRowSerializationSchema} used to serialise input records to JSON.
     *
     * <p>The proto descriptor named by {@link #SCHEMA_PROTO_CLASS_KEY} is resolved through the
     * stencil client and converted into Flink type information.
     *
     * @return a serialization schema matching the configured input proto descriptor
     * @throws InvalidConfigurationException when the stencil client is unavailable, the internal
     *         processor config is missing, or it does not declare {@link #SCHEMA_PROTO_CLASS_KEY}
     */
    private JsonRowSerializationSchema createJsonRowSerializationSchema() {
        StencilClient stencilClient = schemaConfig.getStencilClientOrchestrator().getStencilClient();
        if (stencilClient == null) {
            throw new InvalidConfigurationException("Invalid configuration: stencil client is null");
        }

        Map<String, String> internalProcessorConfig = internalSourceConfig.getInternalProcessorConfig();
        if (internalProcessorConfig == null) {
            throw new InvalidConfigurationException("Invalid internal source configuration: missing internal processor config");
        }

        if (!internalProcessorConfig.containsKey(SCHEMA_PROTO_CLASS_KEY)) {
            throw new InvalidConfigurationException(String.format("Invalid internal source configuration: missing \"%s\" key in internal processor config", SCHEMA_PROTO_CLASS_KEY));
        }

        String schemaProtoClassKey = internalProcessorConfig.get(SCHEMA_PROTO_CLASS_KEY);
        Descriptors.Descriptor inputDescriptor = stencilClient.get(schemaProtoClassKey);

        return JsonRowSerializationSchema
                .builder()
                .withTypeInfo(TypeInformationFactory.getRowType(inputDescriptor))
                .build();
    }
}
