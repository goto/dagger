package com.gotocompany.dagger.functions.udfs.scalar.dart.store.cos;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CosDartClientTest {

    @Mock
    private COSClient libCosClient;

    @Mock
    private COSObject cosObject;

    @Mock
    private GaugeStatsManager gaugeStatsManager;

    @Mock
    private HttpRequestBase mockRequest;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldGetObjectFile() {
        String bucketName = "bucket_name";
        String udfName = "DartGet";
        String dartName = "dart-get/path/to/data.json";
        String jsonFileContent = "{\"name\":\"house-stark-dev\"}";

        when(libCosClient.getObject(bucketName, dartName)).thenReturn(cosObject);
        when(cosObject.getObjectContent()).thenReturn(new COSObjectInputStream(new ByteArrayInputStream(jsonFileContent.getBytes()), mockRequest));

        CosDartClient cosDartClient = new CosDartClient(libCosClient);
        String jsonData = cosDartClient.fetchJsonData(udfName, gaugeStatsManager, bucketName, dartName);

        verify(libCosClient, times(1)).getObject(bucketName, dartName);
        verify(cosObject, times(1)).getObjectContent();
        Assert.assertEquals(jsonFileContent, jsonData);
    }
}