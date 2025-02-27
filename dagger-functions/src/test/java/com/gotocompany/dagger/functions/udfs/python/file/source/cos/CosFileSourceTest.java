package com.gotocompany.dagger.functions.udfs.python.file.source.cos;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CosFileSourceTest {

    @Mock
    private CosFileClient cosFileClient;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldGetObjectFile() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String pythonFile = classLoader.getResource("python_udf.zip").getFile();
        byte[] expectedObject = Files.readAllBytes(Paths.get(pythonFile));

        when(cosFileClient.getFile(pythonFile)).thenReturn(expectedObject);
        CosFileSource cosFileSource = new CosFileSource(pythonFile, cosFileClient);

        byte[] actualObject = cosFileSource.getObjectFile();

        Assert.assertEquals(expectedObject, actualObject);
    }
}
