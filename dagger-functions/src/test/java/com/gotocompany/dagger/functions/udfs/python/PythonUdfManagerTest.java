package com.gotocompany.dagger.functions.udfs.python;

import com.gotocompany.dagger.functions.exceptions.PythonFilesEmptyException;
import com.gotocompany.dagger.functions.exceptions.PythonFilesFormatException;
import org.apache.flink.table.api.TableConfig;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.MockitoAnnotations.initMocks;

public class PythonUdfManagerTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private StreamTableEnvironment tableEnvironment;

    @Mock
    private PythonUdfConfig pythonUdfConfig;

    private TableConfig tableConfig;

    @Mock
    private com.gotocompany.dagger.common.configuration.Configuration daggerConfiguration;

    @Before
    public void setup() {
        initMocks(this);
        tableConfig = new TableConfig();
    }

    @Test
    public void shouldRegisterPythonUdfConfig() throws IOException {
        String pathFile = getPath("python_udf.zip");

        when(tableEnvironment.getConfig()).thenReturn(tableConfig);
        when(pythonUdfConfig.getPythonFiles()).thenReturn(pathFile);
        when(pythonUdfConfig.getPythonArchives()).thenReturn("/path/to/file.txt");
        when(pythonUdfConfig.getPythonRequirements()).thenReturn("requirements.txt");
        when(pythonUdfConfig.getPythonArrowBatchSize()).thenReturn(10000);
        when(pythonUdfConfig.getPythonBundleSize()).thenReturn(100000);
        when(pythonUdfConfig.getPythonBundleTime()).thenReturn(1000L);

        PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, daggerConfiguration);
        pythonUdfManager.registerPythonFunctions();

        assertEquals(pathFile, tableConfig.getConfiguration().getString("python.files", null));
        assertEquals("/path/to/file.txt", tableConfig.getConfiguration().getString("python.archives", null));
        assertEquals("requirements.txt", tableConfig.getConfiguration().getString("python.requirements", null));
        assertEquals(10000, (int) tableConfig.getConfiguration().getInteger("python.fn-execution.arrow.batch.size", -1));
        assertEquals(100000, (int) tableConfig.getConfiguration().getInteger("python.fn-execution.bundle.size", -1));
        assertEquals(1000L, (long) tableConfig.getConfiguration().getLong("python.fn-execution.bundle.time", -1L));
    }

    @Test
    public void shouldNotRegisterConfigIfNotSet() throws IOException {
        String pathFile = getPath("python_udf.zip");

        when(tableEnvironment.getConfig()).thenReturn(tableConfig);
        when(pythonUdfConfig.getPythonFiles()).thenReturn(pathFile);

        PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, daggerConfiguration);
        pythonUdfManager.registerPythonFunctions();

        assertEquals(pathFile, tableConfig.getConfiguration().getString("python.files", null));
        assertNull(tableConfig.getConfiguration().getString("python.archives", null));
        assertNull(tableConfig.getConfiguration().getString("python.requirements", null));
    }

    @Test
    public void shouldRegisterPythonUdfFromPyFile() throws IOException {
        String pathFile = getPath("test_udf.py");
        String sqlRegisterUdf = "CREATE TEMPORARY FUNCTION TEST_UDF AS 'test_udf.test_udf' LANGUAGE PYTHON";

        when(tableEnvironment.getConfig()).thenReturn(tableConfig);
        when(pythonUdfConfig.getPythonFiles()).thenReturn(pathFile);

        PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, daggerConfiguration);
        pythonUdfManager.registerPythonFunctions();

        assertEquals(pathFile, tableConfig.getConfiguration().getString("python.files", null));
        verify(tableEnvironment, times(1)).executeSql(sqlRegisterUdf);
    }

    @Test
    public void shouldOnlyExecutePyFormatInsideZipFile() throws IOException {
        String pathFile = getPath("python_udf.zip");

        String sqlRegisterFirstUdf = "CREATE TEMPORARY FUNCTION MULTIPLY AS 'python_udf.scalar.multiply.multiply' LANGUAGE PYTHON";
        String sqlRegisterSecondUdf = "CREATE TEMPORARY FUNCTION ADD AS 'python_udf.scalar.add.add' LANGUAGE PYTHON";
        String sqlRegisterThirdUdf = "CREATE TEMPORARY FUNCTION SUBSTRACT AS 'python_udf.vectorized.substract.substract' LANGUAGE PYTHON";

        when(tableEnvironment.getConfig()).thenReturn(tableConfig);
        when(pythonUdfConfig.getPythonFiles()).thenReturn(pathFile);

        PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, daggerConfiguration);
        pythonUdfManager.registerPythonFunctions();

        assertEquals(pathFile, tableConfig.getConfiguration().getString("python.files", null));
        verify(tableEnvironment, times(0)).executeSql(sqlRegisterFirstUdf);
        verify(tableEnvironment, times(1)).executeSql(sqlRegisterSecondUdf);
        verify(tableEnvironment, times(1)).executeSql(sqlRegisterThirdUdf);
    }

    @Test
    public void shouldRegisterPythonUdfFromPyAndZipFile() throws IOException {
        String zipPathFile = getPath("python_udf.zip");
        String pyPathFile = getPath("test_udf.py");

        String sqlRegisterFirstUdf = "CREATE TEMPORARY FUNCTION ADD AS 'python_udf.scalar.add.add' LANGUAGE PYTHON";
        String sqlRegisterSecondUdf = "CREATE TEMPORARY FUNCTION SUBSTRACT AS 'python_udf.vectorized.substract.substract' LANGUAGE PYTHON";
        String sqlRegisterThirdUdf = "CREATE TEMPORARY FUNCTION TEST_UDF AS 'test_udf.test_udf' LANGUAGE PYTHON";

        when(tableEnvironment.getConfig()).thenReturn(tableConfig);
        when(pythonUdfConfig.getPythonFiles()).thenReturn(zipPathFile + "," + pyPathFile);

        PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, daggerConfiguration);
        pythonUdfManager.registerPythonFunctions();

        assertEquals(zipPathFile + "," + pyPathFile, tableConfig.getConfiguration().getString("python.files", null));
        verify(tableEnvironment, times(1)).executeSql(sqlRegisterFirstUdf);
        verify(tableEnvironment, times(1)).executeSql(sqlRegisterSecondUdf);
        verify(tableEnvironment, times(1)).executeSql(sqlRegisterThirdUdf);
    }

    @Test
    public void shouldThrowExceptionIfPythonFilesNotInZipOrPyFormat() throws IOException {
        expectedEx.expect(PythonFilesFormatException.class);
        expectedEx.expectMessage("Python files should be in .py or .zip format");

        File file = File.createTempFile("test_file", ".txt");
        file.deleteOnExit();

        when(tableEnvironment.getConfig()).thenReturn(tableConfig);
        when(pythonUdfConfig.getPythonFiles()).thenReturn("test_file.txt");

        PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, daggerConfiguration);
        pythonUdfManager.registerPythonFunctions();
    }

    @Test
    public void shouldThrowExceptionIfPythonFilesIsEmpty() throws IOException {
        expectedEx.expect(PythonFilesFormatException.class);
        expectedEx.expectMessage("Python files should be in .py or .zip format");

        when(tableEnvironment.getConfig()).thenReturn(tableConfig);
        when(pythonUdfConfig.getPythonFiles()).thenReturn("");

        PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, daggerConfiguration);
        pythonUdfManager.registerPythonFunctions();
    }

    @Test
    public void shouldThrowExceptionIfPythonFilesIsNull() throws IOException {
        expectedEx.expect(PythonFilesEmptyException.class);
        expectedEx.expectMessage("Python files can not be null");

        when(tableEnvironment.getConfig()).thenReturn(tableConfig);

        PythonUdfManager pythonUdfManager = new PythonUdfManager(tableEnvironment, pythonUdfConfig, daggerConfiguration);
        pythonUdfManager.registerPythonFunctions();
    }

    private String getPath(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();

        return classLoader.getResource(filename).getPath();
    }
}
