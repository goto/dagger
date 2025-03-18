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

public class CosFileClient {

    private final boolean enableTkeOidcProvider;
    private final String cosRegion;

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
