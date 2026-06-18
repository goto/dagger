package com.gotocompany.dagger.functions.udfs.python.file.source.cos;

import com.gotocompany.dagger.functions.common.CosLibClient;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tencent Cloud Object Storage (COS) client used to download Python UDF artifacts.
 *
 * <p>Wraps a {@code COSClient} obtained from {@code CosLibClient}, resolving the bucket
 * name and object key from a {@code cosn://} location and reading the object content into
 * an in-memory byte array.
 */
public class CosFileClient {

    /**
     * Whether to authenticate using the Tencent Kubernetes Engine (TKE) OIDC provider when
     * constructing the underlying COS client.
     */
    private final boolean enableTkeOidcProvider;
    /**
     * Tencent Cloud region in which the target COS bucket resides.
     */
    private final String cosRegion;

    /**
     * Creates a COS file client bound to the given region and authentication mode.
     *
     * @param enableTkeOidcProvider whether to authenticate via the TKE OIDC provider
     * @param cosRegion             the Tencent Cloud region of the target bucket
     */
    public CosFileClient(boolean enableTkeOidcProvider, String cosRegion) {
        this.enableTkeOidcProvider = enableTkeOidcProvider;
        this.cosRegion = cosRegion;
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

        COSClient cosClient = CosLibClient.getInstance().get(enableTkeOidcProvider, cosRegion);
        COSObject cosObject = cosClient.getObject(bucketName, objectName);
        try (COSObjectInputStream inputStream = cosObject.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        }
    }
}
