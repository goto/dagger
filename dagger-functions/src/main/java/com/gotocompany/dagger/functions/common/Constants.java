package com.gotocompany.dagger.functions.common;

/**
 * Centralised configuration keys, default values, and structural constants used across the Dagger
 * functions module.
 *
 * <p>The entries here name the environment/configuration properties (and their fallback defaults)
 * that drive the DART artifact store (GCS/OSS/COS), the Python UDF runtime, and the pluggable job
 * builder, alongside a couple of sizes used by the feature UDFs. Defining them in one place keeps
 * key names and defaults consistent wherever they are read.
 */
public class Constants {
    /** Number of distinct data-type columns expected in a serialised feature row. */
    public static final Integer NUMBER_OF_DATA_TYPES_IN_FEATURE_ROW = 8;
    /** Number of arguments (key, value, type) that make up a single feature accumulator entry. */
    public static final Integer NUMBER_OF_ARGUMENTS_IN_FEATURE_ACCUMULATOR = 3;

    /**
     * Configuration key for the GCS project id backing the DART store.
     *
     * @deprecated superseded by the storage-agnostic {@link #UDF_DART_PROJECT_ID_KEY}.
     */
    @Deprecated
    public static final String UDF_DART_GCS_PROJECT_ID_KEY = "UDF_DART_GCS_PROJECT_ID";
    /**
     * Default GCS project id (empty) for the DART store.
     *
     * @deprecated superseded by {@link #UDF_DART_PROJECT_ID_DEFAULT}.
     */
    @Deprecated
    public static final String UDF_DART_GCS_PROJECT_ID_DEFAULT = "";
    /**
     * Configuration key for the GCS bucket id backing the DART store.
     *
     * @deprecated superseded by the storage-agnostic {@link #UDF_DART_BUCKET_ID_KEY}.
     */
    @Deprecated
    public static final String UDF_DART_GCS_BUCKET_ID_KEY = "UDF_DART_GCS_BUCKET_ID";
    /**
     * Default GCS bucket id (empty) for the DART store.
     *
     * @deprecated superseded by {@link #UDF_DART_BUCKET_ID_DEFAULT}.
     */
    @Deprecated
    public static final String UDF_DART_GCS_BUCKET_ID_DEFAULT = "";

    /** Configuration key for the cloud project id hosting the DART artifact store. */
    public static final String UDF_DART_PROJECT_ID_KEY = "UDF_DART_PROJECT_ID";
    /** Default DART project id, empty unless explicitly overridden. */
    public static final String UDF_DART_PROJECT_ID_DEFAULT = "";
    /** Configuration key for the object-store bucket holding DART artifacts. */
    public static final String UDF_DART_BUCKET_ID_KEY = "UDF_DART_BUCKET_ID";
    /** Default DART bucket id, empty unless explicitly overridden. */
    public static final String UDF_DART_BUCKET_ID_DEFAULT = "";

    /** Configuration key selecting which object-store provider backs the DART store. */
    public static final String UDF_STORE_PROVIDER_KEY = "UDF_STORE_PROVIDER";
    /** Provider value selecting Google Cloud Storage as the DART store. */
    public static final String UDF_STORE_PROVIDER_GCS = "GCS";
    /** Provider value selecting Alibaba Cloud OSS as the DART store. */
    public static final String UDF_STORE_PROVIDER_OSS = "OSS";
    /** Provider value selecting Tencent Cloud COS as the DART store. */
    public static final String UDF_STORE_PROVIDER_COS = "COS";

    /** Configuration key holding the raw Python UDF configuration block. */
    public static final String PYTHON_UDF_CONFIG = "PYTHON_UDF_CONFIG";
    /** Configuration key toggling whether Python UDFs are enabled. */
    public static final String PYTHON_UDF_ENABLE_KEY = "PYTHON_UDF_ENABLE";
    /** Default for Python UDF support: disabled. */
    public static final boolean PYTHON_UDF_ENABLE_DEFAULT = false;
    /** Configuration key listing the Python source files ({@code .py}/{@code .zip}) to register. */
    public static final String PYTHON_FILES_KEY = "PYTHON_FILES";
    /** Configuration key pointing to the Python requirements specification. */
    public static final String PYTHON_REQUIREMENTS_KEY = "PYTHON_REQUIREMENTS";
    /** Configuration key listing Python archive files to distribute with the job. */
    public static final String PYTHON_ARCHIVES_KEY = "PYTHON_ARCHIVES";
    /** Configuration key for the Arrow batch size used by Python function execution. */
    public static final String PYTHON_FN_EXECUTION_ARROW_BATCH_SIZE_KEY = "PYTHON_FN_EXECUTION_ARROW_BATCH_SIZE";
    /** Default Arrow batch size for Python function execution. */
    public static final Integer PYTHON_FN_EXECUTION_ARROW_BATCH_SIZE_DEFAULT = 10000;
    /** Configuration key for the bundle size (records per bundle) of Python function execution. */
    public static final String PYTHON_FN_EXECUTION_BUNDLE_SIZE_KEY = "PYTHON_FN_EXECUTION_BUNDLE_SIZE";
    /** Default bundle size for Python function execution. */
    public static final Integer PYTHON_FN_EXECUTION_BUNDLE_SIZE_DEFAULT = 100000;
    /** Configuration key for the maximum bundle time (milliseconds) of Python function execution. */
    public static final String PYTHON_FN_EXECUTION_BUNDLE_TIME_KEY = "PYTHON_FN_EXECUTION_BUNDLE_TIME";
    /** Default maximum bundle time, in milliseconds, for Python function execution. */
    public static final long PYTHON_FN_EXECUTION_BUNDLE_TIME_DEFAULT = 1000;

    /** Configuration key for the Alibaba Cloud OSS endpoint used by the DART store. */
    public static final String OSS_ENDPOINT = "OSS_ENDPOINT";
    /** Default OSS endpoint (Singapore region) used when none is configured. */
    public static final String DEFAULT_OSS_ENDPOINT = "oss-ap-southeast-5.aliyuncs.com";

    /** Configuration key for the Tencent Cloud COS region used by the DART store. */
    public static final String COS_REGION = "COS_REGION";
    /** Default COS region (Jakarta) used when none is configured. */
    public static final String DEFAULT_COS_REGION = "ap-jakarta";
    /** Configuration key toggling the TKE OIDC credential provider for COS authentication. */
    public static final String ENABLE_TKE_OIDC_PROVIDER = "ENABLE_TKE_OIDC_PROVIDER";

    /** Configuration key for the fully-qualified class name of the job builder to instantiate. */
    public static final String JOB_BUILDER_FQCN_KEY = "JOB_BUILDER_FQCN";
    /** Default job builder implementation: the SQL-based Dagger job builder. */
    public static final String DEFAULT_JOB_BUILDER_FQCN = "com.gotocompany.dagger.core.DaggerSqlJobBuilder";
}
