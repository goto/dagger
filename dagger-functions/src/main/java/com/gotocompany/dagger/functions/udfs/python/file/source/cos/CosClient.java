package com.gotocompany.dagger.functions.udfs.python.file.source.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.IOUtils;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.provider.OIDCRoleArnProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CosClient {

    private static final String ENV_COS_REGION = "COS_REGION";

    private final COSClient libCosClient;

    /**
     * Instantiates a new Cos client.
     */
    public CosClient() {
        String region = System.getenv(ENV_COS_REGION); // ap-jakarta

        Credential credentials;
        try {
            credentials = new OIDCRoleArnProvider().getCredentials();
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("failed to initiate oidc credential provider", e);
        }

        COSCredentials cosCredentials = new BasicCOSCredentials(credentials.getSecretId(), credentials.getSecretKey());

        ClientConfig clientConfig = new ClientConfig(new Region(region));
        libCosClient = new COSClient(cosCredentials, clientConfig);
    }

    /**
     * Instantiates a new Cos client.
     * This constructor used for unit test purposes.
     *
     * @param libCosClient the storage
     */
    public CosClient(COSClient libCosClient) {
        this.libCosClient = libCosClient;
    }

    /**
     * Get file byte [ ].
     *
     * @param pythonFile the python file
     * @return the byte [ ]
     */
    public byte[] getFile(String pythonFile) throws IOException {
        List<String> file = Arrays.asList(pythonFile.replace("cosn://", "").split("/"));

        String bucketName = file.get(0);
        String objectName = file.stream().skip(1).collect(Collectors.joining("/"));

        COSObject cosObject = libCosClient.getObject(bucketName, objectName);
        try (COSObjectInputStream inputStream = cosObject.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        }
    }
}
