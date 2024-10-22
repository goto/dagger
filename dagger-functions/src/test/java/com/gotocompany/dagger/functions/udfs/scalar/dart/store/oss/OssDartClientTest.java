package com.gotocompany.dagger.functions.udfs.scalar.dart.store.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class OssDartClientTest {
    @Mock
    private OSS libOSSClient;

    @Mock
    private OSSObject ossObject;

    @Mock
    private GaugeStatsManager gaugeStatsManager;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldGetObjectFile() throws IOException {
        String bucketName = "bucket_name";
        String udfName = "DartGet";
        String dartName = "dart-get/path/to/data.json";
        String jsonFileContent = "{\"name\":\"house-stark-dev\"}";

        when(libOSSClient.getObject(bucketName, dartName)).thenReturn(ossObject);
        when(ossObject.getObjectContent()).thenReturn(new ByteArrayInputStream(jsonFileContent.getBytes()));

        OssDartClient ossDartClient = new OssDartClient(libOSSClient);
        String jsonData = ossDartClient.fetchJsonData(udfName, gaugeStatsManager, bucketName, dartName);

        verify(libOSSClient, times(1)).getObject(bucketName, dartName);
        verify(ossObject, times(1)).getObjectContent();
        Assert.assertEquals(jsonFileContent, jsonData);
    }
}
