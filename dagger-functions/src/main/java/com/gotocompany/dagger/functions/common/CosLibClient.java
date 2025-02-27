package com.gotocompany.dagger.functions.common;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.provider.OIDCRoleArnProvider;

/**
 * Stateless class
 */
public class CosLibClient {
    public static CosLibClient INSTANCE = new CosLibClient();

    private static final String ENV_COS_REGION = "COS_REGION";

    public COSClient get() {
        String region = System.getenv(ENV_COS_REGION);
        Credential credentials;
        try {
            credentials = new OIDCRoleArnProvider().getCredentials();
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("failed to initiate oidc credential provider", e);
        }

        COSCredentials cosCredentials = new BasicCOSCredentials(credentials.getSecretId(), credentials.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cosCredentials, clientConfig);
    }

    // unit test helper method; has no side effects.
    // even instance is used, as the current mockito version doesn't support mockStatic.
    public static void testOnlySetInstance(CosLibClient instance) {
        INSTANCE = instance;
    }
}
