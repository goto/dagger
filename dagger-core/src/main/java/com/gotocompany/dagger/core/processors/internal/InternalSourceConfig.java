package com.gotocompany.dagger.core.processors.internal;

import com.gotocompany.dagger.core.processors.types.Validator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that holds internal post processor configuration.
 */
public class InternalSourceConfig implements Validator, Serializable {
    /** Configuration key under which the nested internal-processor settings are supplied. */
    public static final String INTERNAL_PROCESSOR_CONFIG_KEY = "internal_processor_config";

    /** Name of the output column this mapping writes its resolved value into. */
    private String outputField;
    /** The configured value or expression to resolve; its meaning depends on {@link #type}. */
    private String value;
    /** The processor type handling this mapping, for example {@code sql}, {@code function} or {@code constant}. */
    private String type;
    /** Additional key/value settings consumed by the selected internal processor. */
    private Map<String, String> internalProcessorConfig;

    /**
     * Instantiates a new Internal source config.
     *
     * @param outputField             the output field
     * @param value                   the value
     * @param type                    the type
     * @param internalProcessorConfig the internal processor config
     */
    public InternalSourceConfig(String outputField, String value, String type, Map<String, String> internalProcessorConfig) {
        this.outputField = outputField;
        this.value = value;
        this.type = type;
        this.internalProcessorConfig = internalProcessorConfig;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Declares {@code output_field}, {@code type} and {@code value} as mandatory so that
     * validation fails fast when any of them is missing from the internal source config.
     *
     * @return a map of mandatory field names to their currently configured values
     */
    @Override
    public HashMap<String, Object> getMandatoryFields() {
        HashMap<String, Object> mandatoryFields = new HashMap<>();
        mandatoryFields.put("output_field", outputField);
        mandatoryFields.put("type", type);
        mandatoryFields.put("value", value);

        return mandatoryFields;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets output field.
     *
     * @return the output field
     */
    public String getOutputField() {
        return outputField;
    }

    /**
     * Gets internal processor config.
     *
     * @return the internal processor config
     */
    public Map<String, String> getInternalProcessorConfig() {
        return internalProcessorConfig;
    }
}
