package com.gotocompany.dagger.functions.udfs.python.file.source;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.functions.common.Constants;
import com.gotocompany.dagger.functions.udfs.python.file.source.cos.CosFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.gcs.GcsFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.local.LocalFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.oss.OssFileSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FileSourceFactoryTest {

    @Mock
    private Configuration configuration;

    @Before
    public void setUp() {
        initMocks(this);
        when(configuration.getString(Constants.OSS_ENDPOINT, Constants.DEFAULT_OSS_ENDPOINT)).thenReturn("oss-ap-southeast-5.aliyuncs.com");
    }

    @Test
    public void shouldGetLocalFileSource() {
        String pythonFile = "/path/to/file/test_function.py";

        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile, configuration);

        Assert.assertTrue(fileSource instanceof LocalFileSource);
    }

    @Test
    public void shouldGetGcsFileSource() {
        String pythonFile = "gs://bucket-name/path/to/file/test_function.py";

        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile, configuration);

        Assert.assertTrue(fileSource instanceof GcsFileSource);
    }

    @Test
    public void shouldGetOssFileSource() {
        String pythonFile = "oss://bucket-name/path/to/file/test_function.py";

        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile, configuration);

        Assert.assertTrue(fileSource instanceof OssFileSource);
    }

    @Test
    public void shouldGetCosnFileSource() {
        String pythonFile = "cosn://bucket-name/path/to/file/test_function.py";

        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile, configuration);

        Assert.assertTrue(fileSource instanceof CosFileSource);
    }
}
