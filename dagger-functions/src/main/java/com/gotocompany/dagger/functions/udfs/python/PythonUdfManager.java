package com.gotocompany.dagger.functions.udfs.python;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.functions.exceptions.PythonFilesEmptyException;
import com.gotocompany.dagger.functions.udfs.python.file.type.FileType;
import com.gotocompany.dagger.functions.udfs.python.file.type.FileTypeFactory;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Python udf manager.
 */
public class PythonUdfManager {

    /**
     * Flink table environment into which the discovered Python UDFs are registered as
     * temporary SQL functions.
     */
    private StreamTableEnvironment tableEnvironment;
    /**
     * Parsed Python UDF configuration describing the files, requirements, archives and
     * execution tuning to apply.
     */
    private PythonUdfConfig pythonUdfConfig;
    /**
     * Dagger configuration used to resolve the appropriate file source (local, GCS, OSS or
     * COS) for each configured Python file.
     */
    private final Configuration configuration;

    /**
     * Instantiates a new Python udf manager.
     *
     * @param tableEnvironment the table environment
     * @param pythonUdfConfig  the python udf config
     */
    public PythonUdfManager(StreamTableEnvironment tableEnvironment, PythonUdfConfig pythonUdfConfig, Configuration configuration) {
        this.tableEnvironment = tableEnvironment;
        this.pythonUdfConfig = pythonUdfConfig;
        this.configuration = configuration;
    }

    /**
     * Register python functions.
     */
    public void registerPythonFunctions() throws IOException {
        String inputFiles = pythonUdfConfig.getPythonFiles();
        String[] pythonFiles;
        if (inputFiles != null) {
            registerPythonConfig();
            pythonFiles = inputFiles.split(",");
        } else {
            throw new PythonFilesEmptyException("Python files can not be null");
        }

        for (String pythonFile : pythonFiles) {
            FileType fileType = FileTypeFactory.getFileType(pythonFile, configuration);
            List<String> fileNames = fileType.getFileNames();
            List<String> sqlQueries = createQuery(fileNames);
            executeSql(sqlQueries);
        }
    }

    /**
     * Applies the Python execution settings from the {@link PythonUdfConfig} onto the
     * underlying Flink table environment configuration.
     *
     * <p>Optional requirements and archives are only set when present, while the Python
     * files, Arrow batch size, bundle size and bundle time are always configured using the
     * values (or defaults) resolved from the config.
     */
    private void registerPythonConfig() {
        if (pythonUdfConfig.getPythonRequirements() != null) {
            tableEnvironment.getConfig().getConfiguration().setString("python.requirements", pythonUdfConfig.getPythonRequirements());
        }
        if (pythonUdfConfig.getPythonArchives() != null) {
            tableEnvironment.getConfig().getConfiguration().setString("python.archives", pythonUdfConfig.getPythonArchives());
        }
        tableEnvironment.getConfig().getConfiguration().setString("python.files", pythonUdfConfig.getPythonFiles());
        tableEnvironment.getConfig().getConfiguration().setInteger("python.fn-execution.arrow.batch.size", pythonUdfConfig.getPythonArrowBatchSize());
        tableEnvironment.getConfig().getConfiguration().setInteger("python.fn-execution.bundle.size", pythonUdfConfig.getPythonBundleSize());
        tableEnvironment.getConfig().getConfiguration().setLong("python.fn-execution.bundle.time", pythonUdfConfig.getPythonBundleTime());
    }

    /**
     * Executes each of the supplied SQL statements against the table environment.
     *
     * @param sqlQueries the SQL statements to run, typically temporary function
     *                   registrations derived from the discovered Python files
     */
    private void executeSql(List<String> sqlQueries) {
        for (String query : sqlQueries) {
            tableEnvironment.executeSql(query);
        }
    }

    /**
     * Builds the {@code CREATE TEMPORARY FUNCTION} SQL statements that register each
     * discovered Python file as a Flink SQL function.
     *
     * <p>For every entry the {@code .py} suffix is stripped and path separators are
     * converted to dots to form the fully-qualified Python callable; the derived function
     * name (the last path segment, upper-cased) is bound to that callable using the
     * {@code PYTHON} language.
     *
     * @param fileNames the Python file names discovered for a configured source
     * @return one SQL registration statement per supplied file name
     */
    private List<String> createQuery(List<String> fileNames) {
        List<String> sqlQueries = new ArrayList<>();
        for (String fileName : fileNames) {
            fileName = fileName.replace(".py", "").replace("/", ".");
            String functionName = fileName.substring(fileName.lastIndexOf(".") + 1);
            String query = "CREATE TEMPORARY FUNCTION " + functionName.toUpperCase() + " AS '" + fileName + "." + functionName + "' LANGUAGE PYTHON";
            sqlQueries.add(query);
        }
        return sqlQueries;
    }
}
