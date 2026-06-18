package com.gotocompany.dagger.functions.udfs.python;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.functions.common.Constants;
import lombok.Getter;

/**
 * The type Python udf config.
 */
public class PythonUdfConfig {
    /**
     * Shared, pre-configured Gson instance used to deserialize the raw Python UDF
     * configuration JSON into a {@link PythonUdfConfig} instance.
     *
     * <p>Complex map key serialization and pretty printing are enabled so the parser
     * can faithfully reconstruct the configured values from their JSON representation.
     */
    private static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();

    /**
     * Comma-separated list of Python source files that contain the UDFs to register,
     * bound to the {@code PYTHON_FILES_KEY} JSON property.
     */
    @SerializedName(Constants.PYTHON_FILES_KEY)
    private String pythonFiles;

    /**
     * Path to the {@code requirements.txt} listing third-party Python dependencies,
     * bound to the {@code PYTHON_REQUIREMENTS_KEY} JSON property.
     */
    @SerializedName(Constants.PYTHON_REQUIREMENTS_KEY)
    @Getter
    private String pythonRequirements;

    /**
     * Comma-separated list of archive files (such as packaged virtual environments)
     * to extract for the Python workers, bound to the {@code PYTHON_ARCHIVES_KEY} JSON property.
     */
    @SerializedName(Constants.PYTHON_ARCHIVES_KEY)
    private String pythonArchives;

    /**
     * Maximum number of records transferred to Python workers per Arrow batch, bound to
     * the {@code PYTHON_FN_EXECUTION_ARROW_BATCH_SIZE_KEY} JSON property; {@code null}
     * selects the configured default.
     */
    @SerializedName(Constants.PYTHON_FN_EXECUTION_ARROW_BATCH_SIZE_KEY)
    private Integer pythonArrowBatchSize;

    /**
     * Maximum number of elements processed per bundle by the Python function runner, bound
     * to the {@code PYTHON_FN_EXECUTION_BUNDLE_SIZE_KEY} JSON property; {@code null} selects
     * the configured default.
     */
    @SerializedName(Constants.PYTHON_FN_EXECUTION_BUNDLE_SIZE_KEY)
    private Integer pythonBundleSize;

    /**
     * Maximum time, in milliseconds, that a bundle may be buffered before dispatch to the
     * Python workers, bound to the {@code PYTHON_FN_EXECUTION_BUNDLE_TIME_KEY} JSON property;
     * {@code null} selects the configured default.
     */
    @SerializedName(Constants.PYTHON_FN_EXECUTION_BUNDLE_TIME_KEY)
    private Long pythonBundleTime;

    /**
     * Gets python files.
     *
     * @return the python files
     */
    public String getPythonFiles() {
        if (pythonFiles != null) {
            return pythonFiles.replaceAll("\\s+", "");
        }
        return null;
    }

    /**
     * Gets python archives.
     *
     * @return the python archives
     */
    public String getPythonArchives() {
        if (pythonArchives != null) {
            return pythonArchives.replaceAll("\\s+", "");
        }
        return null;
    }

    /**
     * Gets python arrow batch size.
     *
     * @return the python arrow batch size
     */
    public int getPythonArrowBatchSize() {
        if (pythonArrowBatchSize == null) {
            return Constants.PYTHON_FN_EXECUTION_ARROW_BATCH_SIZE_DEFAULT;
        }
        return pythonArrowBatchSize;
    }

    /**
     * Gets python bundle size.
     *
     * @return the python bundle size
     */
    public int getPythonBundleSize() {
        if (pythonBundleSize == null) {
            return Constants.PYTHON_FN_EXECUTION_BUNDLE_SIZE_DEFAULT;
        }
        return pythonBundleSize;
    }

    /**
     * Gets python bundle time.
     *
     * @return the python bundle time
     */
    public long getPythonBundleTime() {
        if (pythonBundleTime == null) {
            return Constants.PYTHON_FN_EXECUTION_BUNDLE_TIME_DEFAULT;
        }
        return pythonBundleTime;
    }

    /**
     * Parse python udf config.
     *
     * @param configuration the configuration
     * @return the python udf config
     */
    public static PythonUdfConfig parse(Configuration configuration) {
        String jsonString = configuration.getString(Constants.PYTHON_UDF_CONFIG, "");

        return GSON.fromJson(jsonString, PythonUdfConfig.class);
    }
}
