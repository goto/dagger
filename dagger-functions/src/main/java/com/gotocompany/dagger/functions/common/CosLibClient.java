package com.gotocompany.dagger.functions.common;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.provider.OIDCRoleArnProvider;
import lombok.Getter;

/**
 * Stateless class.
 */
public class CosLibClient {
    @Getter
    private static CosLibClient instance = new CosLibClient();

    private static final String ENV_COS_SECRET_ID = "COS_SECRET_ID";
    private static final String ENV_COS_SECRET_KEY = "COS_SECRET_KEY";

    // the credential provider provides short living token. If we have a libCosClient long living object with these
    // token or say if we refresh it before client usage will not have much benefits.
    // Create client when using its operation.
    public COSClient get(boolean enableTkeOidcProvider, String cosRegion) {
        String secretId, secretKey;

        if (enableTkeOidcProvider) {
            try {
                Credential credentials = new OIDCRoleArnProvider().getCredentials();
                secretId = credentials.getSecretId();
                secretKey = credentials.getSecretKey();
            } catch (TencentCloudSDKException e) {
                throw new RuntimeException("failed to initiate oidc credential provider", e);
            }
        } else {
            secretId = System.getenv(ENV_COS_SECRET_ID);
            secretKey = System.getenv(ENV_COS_SECRET_KEY);
        }

        COSCredentials cosCredentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(cosRegion));
        return new COSClient(cosCredentials, clientConfig);
    }

    // unit test helper method; Additionally, method has no side effects.
    // the current mockito version doesn't support mockStatic.
    public static void testOnlySetInstance(CosLibClient cosLibClient) {
        CosLibClient.instance = cosLibClient;
    }
}
