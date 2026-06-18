package com.gotocompany.dagger.core.config;

import com.gotocompany.dagger.core.exception.DaggerConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * The Factory class for configuration provider.
 */
public class ConfigurationProviderFactory {
    /**
     * Logger used to record the supplied program arguments and the resolved configuration source.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationProviderFactory.class);
    /**
     * The raw command-line arguments forwarded to the selected {@link ConfigurationProvider}.
     */
    private String[] args;
    /**
     * System-property key identifying the configuration source, such as {@code "FILE"},
     * {@code "ARGS"}, or {@code "ENVIRONMENT"}.
     */
    public static final String CONFIG_SOURCE = "ConfigSource";

    /**
     * Instantiates a new Configuration provider factory.
     *
     * @param args the args
     */
    public ConfigurationProviderFactory(String[] args) {

        this.args = args;
        LOGGER.info("Arguments are: ");
        Arrays.asList(args).stream().forEach(s -> {
            LOGGER.info(s);
            if (s.contains("ConfigFile")) {
                System.setProperty(CONFIG_SOURCE, "FILE");
                System.setProperty("DAGGER_CONFIG_PATH", s.split("=")[1]);
            }
        });

    }

    /**
     * Get configuration provider.
     *
     * @return the configuration provider
     */
    public ConfigurationProvider provider() {
        if (System.getProperties().containsKey(CONFIG_SOURCE)) {
            String configSource = System.getProperty(CONFIG_SOURCE);
            switch (configSource) {
                case "ENVIRONMENT":
                    return new EnvironmentConfigurationProvider(System.getenv());
                case "ARGS":
                    return new CommandlineConfigurationProvider(args);
                case "FILE":
                    return new FileConfigurationProvider();
                default:
                    throw new DaggerConfigurationException("Config source not provided");
            }
        }
        return new CommandlineConfigurationProvider(args);
    }
}
