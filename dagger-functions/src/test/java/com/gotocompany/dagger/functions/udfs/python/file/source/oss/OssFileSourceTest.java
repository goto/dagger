package com.gotocompany.dagger.functions.udfs.python.file.source.oss;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class OssFileSourceTest  {

    @Mock
    private OssClient ossClient;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldGetObjectFile() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String pythonFile = classLoader.getResource("python_udf.zip").getFile();
        byte[] expectedObject = Files.readAllBytes(Paths.get(pythonFile));

        when(ossClient.getFile(pythonFile)).thenReturn(expectedObject);
        OssFileSource ossFileSource = new OssFileSource(pythonFile, ossClient, "some-oss-endpoint");

        byte[] actualObject = ossFileSource.getObjectFile();

        Assert.assertEquals(expectedObject, actualObject);
    }
}
