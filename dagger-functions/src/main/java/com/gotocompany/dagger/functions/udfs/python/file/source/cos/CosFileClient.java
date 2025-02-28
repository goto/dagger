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
    public CosFileClient() {
        // the credential provider provides short living token. If we have a libCosClient long living object with these
        // token or say if we refresh it before client usage will not have much benefits.
        // Create client when using its operation.
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

        COSClient libCosClient = CosLibClient.getInstance().get();
        COSObject cosObject = libCosClient.getObject(bucketName, objectName);
        try (COSObjectInputStream inputStream = cosObject.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        }
    }
}
