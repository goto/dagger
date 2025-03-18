package com.gotocompany.dagger.functions.udfs.python.file.type;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.functions.common.Constants;
import com.gotocompany.dagger.functions.exceptions.PythonFilesFormatException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FileTypeFactoryTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private Configuration configuration;

    @Before
    public void setUp() {
        initMocks(this);
        when(configuration.getString(Constants.OSS_ENDPOINT, Constants.DEFAULT_OSS_ENDPOINT)).thenReturn("oss-ap-southeast-5.aliyuncs.com");
    }

    @Test
    public void shouldGetPythonFileType() {
        String pythonFile = "/path/to/file/test_udf.py";

        FileType fileType = FileTypeFactory.getFileType(pythonFile, configuration);

        Assert.assertTrue(fileType instanceof PythonFileType);
    }

    @Test
    public void shouldGetZipFileType() {
        String pythonFile = "/path/to/file/python_udf.zip";

        FileType fileType = FileTypeFactory.getFileType(pythonFile, configuration);

        Assert.assertTrue(fileType instanceof ZipFileType);
    }

    @Test
    public void shouldThrowExceptionIfPythonFilesNotInZipOrPyFormat() {
        expectedEx.expect(PythonFilesFormatException.class);
        expectedEx.expectMessage("Python files should be in .py or .zip format");

        String pythonFile = "/path/to/file/test_file.txt";

        FileTypeFactory.getFileType(pythonFile, configuration);
    }
}
