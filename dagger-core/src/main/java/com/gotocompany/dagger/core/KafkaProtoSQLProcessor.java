package com.gotocompany.dagger.core;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.config.ConfigurationProvider;
import com.gotocompany.dagger.core.config.ConfigurationProviderFactory;
import com.gotocompany.dagger.functions.common.Constants;
import org.apache.flink.client.program.ProgramInvocationException;
import com.gotocompany.dagger.common.core.DaggerContext;

import java.lang.reflect.Constructor;
import java.util.TimeZone;

import static com.gotocompany.dagger.functions.common.Constants.JOB_BUILDER_FQCN_KEY;

/**
 * Main class to run Dagger.
 */
public class KafkaProtoSQLProcessor {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws ProgramInvocationException the program invocation exception
     */
    public static void main(String[] args) throws ProgramInvocationException {
        try {
            ConfigurationProvider provider = new ConfigurationProviderFactory(args).provider();
            Configuration configuration = provider.get();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            DaggerContext daggerContext = DaggerContext.init(configuration);

            JobBuilder jobBuilder = getJobBuilderInstance(daggerContext);
            jobBuilder
                    .registerConfigs()
                    .registerSourceWithPreProcessors()
                    .registerFunctions()
                    .registerOutputStream()
                    .execute();
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw new ProgramInvocationException(e);
        }
    }

    private static JobBuilder getJobBuilderInstance(DaggerContext daggerContext) {
        String className = daggerContext.getConfiguration().getString(JOB_BUILDER_FQCN_KEY, Constants.DEFAULT_JOB_BUILDER_FQCN);
        try {
            Class<?> builderClazz = Class.forName(className);
            Constructor<?> builderClazzConstructor = builderClazz.getConstructor(DaggerContext.class);
            return (JobBuilder) builderClazzConstructor.newInstance(daggerContext);
        } catch (Exception e) {
            Exception wrapperException = new Exception("Unable to instantiate job builder class: <" + className + "> \n"
                    + "Instantiating default job builder com.gotocompany.dagger.core.DaggerSqlJobBuilder", e);
            wrapperException.printStackTrace();
            return new DaggerSqlJobBuilder(daggerContext);
        }
    }
}
