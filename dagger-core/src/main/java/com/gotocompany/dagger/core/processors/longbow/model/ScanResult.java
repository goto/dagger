package com.gotocompany.dagger.core.processors.longbow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@Builder
public class ScanResult {
    private byte[] primaryKey;
    private Map<byte[], Map<byte[], byte[]>> data;

    public ScanResult(byte[] primaryKey) {
        this.primaryKey = primaryKey;
        this.data = new TreeMap<>(Bytes.BYTES_COMPARATOR);
    }

    public void addData(byte[] columnFamily, byte[] qualifier, byte[] value) {
        if (!data.containsKey(columnFamily)) {
            data.put(columnFamily, new HashMap<>());
        }
        data.get(columnFamily).put(qualifier, value);
    }

}
