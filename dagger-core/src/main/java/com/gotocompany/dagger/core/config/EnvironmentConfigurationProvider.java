package com.gotocompany.dagger.core.config;

import org.apache.flink.api.java.utils.ParameterTool;

import com.gotocompany.dagger.common.configuration.Configuration;

import java.util.Map;

/**
 * The class which handle configuration provided from Environment.
 */
public class EnvironmentConfigurationProvider implements ConfigurationProvider {

    /**
     * The environment variables used to build the {@link Configuration}, typically the process
     * environment from {@code System.getenv()}.
     */
    private Map<String, String> environmentParameters;

    /**
     * Instantiates a new Environment configuration provider.
     *
     * @param environmentParameters the environment parameters
     */
    public EnvironmentConfigurationProvider(Map<String, String> environmentParameters) {
        this.environmentParameters = environmentParameters;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds the {@link Configuration} from the supplied environment variables, wrapping them in
     * Flink's {@code ParameterTool}.
     *
     * @return the configuration derived from the environment variables
     */
    @Override
    public Configuration get() {
        return new Configuration(ParameterTool.fromMap(environmentParameters));
    }
}
