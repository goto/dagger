package com.gotocompany.dagger.core.processors.longbow.data;

import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;
import com.gotocompany.dagger.core.utils.Constants;
import com.gotocompany.dagger.core.processors.longbow.LongbowSchema;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class LongbowTableDataTest {

    private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT);

    @Mock
    private ScanResult result1;

    @Mock
    private ScanResult result2;

    @Before
    public void setUp() {
        initMocks(this);
        Map<byte[], Map<byte[], byte[]>> data1 = new HashMap<>();
        Map<byte[], byte[]> innerData1 = new HashMap<>();
        innerData1.put(Bytes.toBytes("longbow_data1"), Bytes.toBytes("RB-234"));
        innerData1.put(Bytes.toBytes("longbow_data2"), Bytes.toBytes("RB-235"));
        Map<byte[], Map<byte[], byte[]>> data2 = new HashMap<>();
        Map<byte[], byte[]> innerData2 = new HashMap<>();
        innerData2.put(Bytes.toBytes("longbow_data1"), Bytes.toBytes("RB-224"));
        innerData2.put(Bytes.toBytes("longbow_data2"), Bytes.toBytes("RB-225"));
        when(result1.getData()).thenReturn(data1);
        when(result2.getData()).thenReturn(data2);
    }

    @Test
    public void shouldReturnEmptyDataWhenScanResultIsEmpty() {
        List<ScanResult> scanResult = new ArrayList<>();
        String[] columnNames = {"longbow_key", "longbow_data1", "rowtime", "longbow_duration"};

        LongbowSchema longbowSchema = new LongbowSchema(columnNames);
        LongbowTableData longbowTableData = new LongbowTableData(longbowSchema);
        Map<String, List<String>> actualData = longbowTableData.parse(scanResult);
        assertEquals(Collections.emptyList(), actualData.get("longbow_data1"));
    }

    @Test
    public void shouldReturnListOfString() {
        List<ScanResult> scanResult = new ArrayList<>();
        scanResult.add(result1);
        String[] columnNames = {"longbow_key", "longbow_data1", "rowtime", "longbow_duration"};

        LongbowSchema longbowSchema = new LongbowSchema(columnNames);
        LongbowTableData longbowTableData = new LongbowTableData(longbowSchema);
        Map<String, List<String>> actualData = longbowTableData.parse(scanResult);
        assertEquals(Collections.singletonList("RB-234"), actualData.get("longbow_data1"));
    }

    @Test
    public void shouldReturnMultipleListOfStringWhenLongbowDataMoreThanOne() {
        List<ScanResult> scanResult = new ArrayList<>();
        scanResult.add(result1);
        scanResult.add(result2);
        String[] columnNames = {"longbow_key", "longbow_data1", "rowtime", "longbow_duration", "longbow_data2"};

        LongbowSchema longbowSchema = new LongbowSchema(columnNames);
        LongbowTableData longbowTableData = new LongbowTableData(longbowSchema);
        Map<String, List<String>> actualData = longbowTableData.parse(scanResult);
        Map<String, List<String>> expectedMap = new HashMap<String, List<String>>() {{
            put("longbow_data1", Arrays.asList("RB-234", "RB-224"));
            put("longbow_data2", Arrays.asList("RB-235", "RB-225"));
        }};
        assertEquals(expectedMap, actualData);
    }
}
