package com.gotocompany.dagger.core.config;

import org.apache.flink.api.java.utils.ParameterTool;

import com.google.gson.Gson;
import com.gotocompany.dagger.common.configuration.Configuration;

import java.util.Base64;

/**
 * The class which handle configuration provided from Commandline.
 */
public class CommandlineConfigurationProvider implements ConfigurationProvider {

    /**
     * The raw command-line arguments supplied to the Dagger job, later wrapped in Flink's
     * {@code ParameterTool}.
     */
    private String[] args;
    /**
     * Shared {@link Gson} instance used to decode Base64-encoded program arguments into a
     * {@code String[]}.
     */
    private static final Gson GSON = new Gson();

    /**
     * Instantiates a new Commandline configuration provider.
     *
     * @param args the args
     */
    public CommandlineConfigurationProvider(String[] args) {

        this.args = args;
    }

    /**
     * Builds a {@link Configuration} from the command-line arguments.
     *
     * <p>When an {@code encodedArgs} parameter is present, the Base64-encoded arguments are decoded
     * and used in place of the raw arguments before being wrapped in Flink's {@code ParameterTool}.
     *
     * @return the configuration parsed from the (possibly decoded) command-line arguments
     */
    private Configuration constructParamTool() {
        String[] finalArgs = args;
        if (isEncodedArgsPresent()) {
            finalArgs = parseEncodedProgramArgs();
        }
        return new Configuration(ParameterTool.fromArgs(finalArgs));
    }

    /**
     * Checks whether the command-line arguments carry a Base64-encoded {@code encodedArgs} entry.
     *
     * @return {@code true} if an {@code encodedArgs} argument was supplied, {@code false} otherwise
     */
    private boolean isEncodedArgsPresent() {
        String encodedArgs = ParameterTool.fromArgs(args).get("encodedArgs");
        return encodedArgs != null;
    }

    /**
     * Decodes the Base64-encoded {@code encodedArgs} argument into the actual program arguments.
     *
     * <p>The decoded payload is expected to be a JSON-encoded {@code String[]}, which is parsed
     * with {@link Gson}.
     *
     * @return the decoded program arguments as a {@code String[]}
     */
    private String[] parseEncodedProgramArgs() {
        String encodedArgs = ParameterTool.fromArgs(args).get("encodedArgs");
        byte[] decoded = Base64.getMimeDecoder().decode(encodedArgs);
        return GSON.fromJson(new String(decoded), String[].class);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Logs the resolved parameters to standard output and returns the {@link Configuration}
     * built from the command-line arguments.
     *
     * @return the configuration derived from the command-line arguments
     */
    @Override
    public Configuration get() {
        System.out.println("params from " + CommandlineConfigurationProvider.class.getName());
        ParameterTool.fromArgs(args).toMap().entrySet().stream().forEach(System.out::println);
        return constructParamTool();
    }
}
