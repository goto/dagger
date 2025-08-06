package com.gotocompany.dagger.core.processors.longbow.data;

import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class LongbowProtoDataTest {

    @Mock
    private ScanResult scanResult;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldParseProtoByteDataFromBigTable() {
        ArrayList<ScanResult> results = new ArrayList<>();
        results.add(scanResult);
        byte[] mockResult = Bytes.toBytes("test");
        Map<String, Map<String, byte[]>> data = new HashMap<>();
        Map<String, byte[]> innerData = new HashMap<>();
        innerData.put(Constants.LONGBOW_QUALIFIER_DEFAULT, mockResult);
        data.put(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT, innerData);
        Mockito.when(scanResult.getData()).thenReturn(data);

        LongbowProtoData longbowProtoData = new LongbowProtoData();
        Map<String, List<byte[]>> actualMap = longbowProtoData.parse(results);
        Map<String, List<byte[]>> expectedMap = new HashMap<String, List<byte[]>>() {{
            put("proto_data", Arrays.asList(mockResult));
        }};
        assertEquals(expectedMap, actualMap);
    }
}
