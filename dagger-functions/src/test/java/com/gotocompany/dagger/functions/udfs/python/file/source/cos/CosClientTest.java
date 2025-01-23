package com.gotocompany.dagger.functions.udfs.python.file.source.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CosClientTest {

    @Mock
    private COSClient libCosClient;

    @Mock
    private COSObject cosObject;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldGetObjectFile() throws IOException {
        HttpRequestBase mockRequest = Mockito.mock(HttpRequestBase.class);

        String pythonFile = "cosn://bucket_name/path/to/file/python_udf.zip";
        String bucketName = "bucket_name";
        String objectName = "path/to/file/python_udf.zip";
        String expectedValue = Arrays.toString("objectFile".getBytes());

        when(libCosClient.getObject(bucketName, objectName)).thenReturn(cosObject);
        when(cosObject.getObjectContent()).thenReturn(new COSObjectInputStream(new ByteArrayInputStream("objectFile".getBytes()), mockRequest));

        CosClient cosClient = new CosClient(libCosClient);
        byte[] actualValue = cosClient.getFile(pythonFile);

        verify(libCosClient, times(1)).getObject(bucketName, objectName);
        verify(cosObject, times(1)).getObjectContent();
        Assert.assertEquals(expectedValue, Arrays.toString(actualValue));
    }
}
