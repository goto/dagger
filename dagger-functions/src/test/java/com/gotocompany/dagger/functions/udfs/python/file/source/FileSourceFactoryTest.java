package com.gotocompany.dagger.functions.udfs.python.file.source;

import com.gotocompany.dagger.functions.udfs.python.file.source.cos.CosFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.gcs.GcsFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.local.LocalFileSource;
import com.gotocompany.dagger.functions.udfs.python.file.source.oss.OssFileSource;
import org.junit.Assert;
import org.junit.Test;

public class FileSourceFactoryTest {

    @Test
    public void shouldGetLocalFileSource() {
        String pythonFile = "/path/to/file/test_function.py";

        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile);

        Assert.assertTrue(fileSource instanceof LocalFileSource);
    }

    @Test
    public void shouldGetGcsFileSource() {
        String pythonFile = "gs://bucket-name/path/to/file/test_function.py";

        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile);

        Assert.assertTrue(fileSource instanceof GcsFileSource);
    }

    @Test
    public void shouldGetOssFileSource() {
        String pythonFile = "oss://bucket-name/path/to/file/test_function.py";

        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile);

        Assert.assertTrue(fileSource instanceof OssFileSource);
    }

    @Test
    public void shouldGetCosnFileSource() {
        String pythonFile = "cosn://bucket-name/path/to/file/test_function.py";

        FileSource fileSource = FileSourceFactory.getFileSource(pythonFile);

        Assert.assertTrue(fileSource instanceof CosFileSource);
    }
}
