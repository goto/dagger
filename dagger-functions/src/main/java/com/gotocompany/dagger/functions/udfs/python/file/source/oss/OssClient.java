package com.gotocompany.dagger.functions.udfs.python.file.source.oss;

import com.aliyun.core.utils.IOUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.model.OSSObject;
import com.aliyuncs.exceptions.ClientException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OssClient {

    private static final String ENV_OSS_ENDPOINT = "OSS_ENDPOINT";
    private static final String DEFAULT_OSS_ENDPOINT = "oss-ap-southeast-1.aliyuncs.com";

    private final OSS libOssClient;

    /**
     * Instantiates a new Oss client.
     */
    public OssClient() {
        String endpoint = System.getenv(ENV_OSS_ENDPOINT);
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = DEFAULT_OSS_ENDPOINT;
        }
        try {
            libOssClient = new OSSClientBuilder().build(endpoint, CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider());
        } catch (ClientException e) {
            throw new RuntimeException("failed to initialise oss client", e);
        }
    }

    /**
     * Instantiates a new OSS client.
     * This constructor used for unit test purposes.
     *
     * @param libOssClient the storage
     */
    public OssClient(OSS libOssClient) {
        this.libOssClient = libOssClient;
    }

    /**
     * Get file byte [ ].
     *
     * @param pythonFile the python file
     * @return the byte [ ]
     */
    public byte[] getFile(String pythonFile) throws IOException {
        List<String> file = Arrays.asList(pythonFile.replace("oss://", "").split("/"));

        String bucketName = file.get(0);
        String objectName = file.stream().skip(1).collect(Collectors.joining("/"));

        OSSObject ossObject = libOssClient.getObject(bucketName, objectName);
        try (InputStream inputStream = ossObject.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        }
    }
}
