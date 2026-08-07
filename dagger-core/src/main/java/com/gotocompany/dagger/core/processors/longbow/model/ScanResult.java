package com.gotocompany.dagger.core.processors.longbow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ScanResult {
    private byte[] primaryKey;
    private Map<String, Map<String, byte[]>> data;

    public ScanResult(byte[] primaryKey) {
        this.primaryKey = primaryKey;
        this.data = new HashMap<>();
    }

    public void addData(byte[] columnFamily, byte[] qualifier, byte[] value) {
        String columnFamilyString = new String(columnFamily);
        if (!data.containsKey(columnFamilyString)) {
            data.put(columnFamilyString, new HashMap<>());
        }
        data.get(columnFamilyString).put(new String(qualifier), value);
    }

}
