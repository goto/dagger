package com.gotocompany.dagger.functions.udfs.python.file.source.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class OssClientTest {

    @Mock
    private OSS libOSSClient;

    @Mock
    private OSSObject ossObject;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldGetObjectFile() throws IOException {

        String pythonFile = "oss://bucket_name/path/to/file/python_udf.zip";
        String bucketName = "bucket_name";
        String objectName = "path/to/file/python_udf.zip";
        String expectedValue = Arrays.toString("objectFile".getBytes());

        when(libOSSClient.getObject(bucketName, objectName)).thenReturn(ossObject);
        when(ossObject.getObjectContent()).thenReturn(new ByteArrayInputStream("objectFile".getBytes()));

        OssClient ossClient = new OssClient(libOSSClient);
        byte[] actualValue = ossClient.getFile(pythonFile);

        verify(libOSSClient, times(1)).getObject(bucketName, objectName);
        verify(ossObject, times(1)).getObjectContent();
        Assert.assertEquals(expectedValue, Arrays.toString(actualValue));
    }
}
