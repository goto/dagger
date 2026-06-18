package com.gotocompany.dagger.core.processors.transformers;

/**
 * The utils of the Transformer.
 */
public class TransformerUtils {
    /**
     * The enum Default argument.
     */
    enum DefaultArgument {
        /**
         * Table name default argument.
         */
        INPUT_SCHEMA_TABLE("table_name");
        /**
         * The serialized argument key used to look this default argument up in a transformer's
         * argument map.
         */
        private final String argument;

        /**
         * Creates a default argument bound to the given serialized key.
         *
         * @param argument the argument key as it appears in a transformer's argument map
         */
        DefaultArgument(String argument) {
            this.argument = argument;
        }

        /**
         * Returns the serialized argument key for this default argument.
         *
         * @return the argument key
         */
        @Override
        public String toString() {
            return this.argument;
        }
    }

    /**
     * Populate default arguments.
     *
     * @param processor the processor
     */
    protected static void populateDefaultArguments(TransformProcessor processor) {
        for (TransformConfig config : processor.transformConfigs) {
            config.validateFields();
            config.getTransformationArguments().put(DefaultArgument.INPUT_SCHEMA_TABLE.toString(), processor.tableName);
        }
    }
}
