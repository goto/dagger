package com.gotocompany.dagger.core.processors.transformers;

import com.gotocompany.dagger.core.processors.types.Validator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that holds the Transformer configuration.
 */
public class TransformConfig implements Validator, Serializable {

    /**
     * The fully-qualified class name of the
     * {@link com.gotocompany.dagger.common.core.Transformer} implementation to instantiate and apply.
     */
    private final String transformationClass;
    /**
     * The arguments supplied to the transformer, keyed by argument name.
     */
    private final Map<String, Object> transformationArguments;

    /**
     * Instantiates a new Transform config with specified transformation class and args.
     *
     * @param transformationClass     the transformation class
     * @param transformationArguments the transformation arguments
     */
    public TransformConfig(String transformationClass, Map<String, Object> transformationArguments) {
        this.transformationClass = transformationClass;
        this.transformationArguments = transformationArguments;
    }

    /**
     * Instantiates a new Transform config.
     */
    public TransformConfig() {
        this.transformationClass = "NULL";
        this.transformationArguments = new HashMap<>();
    }

    /**
     * Gets transformation class.
     *
     * @return the transformation class
     */
    public String getTransformationClass() {
        return transformationClass;
    }

    /**
     * Gets transformation arguments.
     *
     * @return the transformation arguments
     */
    public Map<String, Object> getTransformationArguments() {
        return transformationArguments;
    }

    /**
     * Returns the fields that must be present for this configuration to be considered valid.
     *
     * <p>For a transform config the only mandatory field is the transformation class name.
     *
     * @return a map containing the {@code transformationClass} entry to be validated
     */
    public HashMap<String, Object> getMandatoryFields() {
        HashMap<String, Object> mandatoryFields = new HashMap<>();
        mandatoryFields.put("transformationClass", transformationClass);
        return mandatoryFields;
    }

    /**
     * Validates this configuration, ensuring mandatory fields are present and that no reserved
     * default-argument key is supplied by the user.
     *
     * <p>This first runs the default {@link Validator#validateFields()} checks, then rejects any
     * transformation argument whose key collides with a {@link TransformerUtils.DefaultArgument}
     * (for example {@code table_name}), since those keys are populated internally.
     *
     * @throws IllegalArgumentException if a mandatory field is missing or a reserved argument key
     *                                  is present in the transformation arguments
     */
    @Override
    public void validateFields() throws IllegalArgumentException {
        Validator.super.validateFields();
        for (TransformerUtils.DefaultArgument val : TransformerUtils.DefaultArgument.values()) {
            if (transformationArguments.containsKey(val.toString())) {
                throw new IllegalArgumentException("Transformation arguments cannot contain `" + val.toString() + "` as a key");
            }
        }
    }
}
