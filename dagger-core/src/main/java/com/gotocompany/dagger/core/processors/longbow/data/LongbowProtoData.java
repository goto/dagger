package com.gotocompany.dagger.core.processors.longbow.data;

import com.gotocompany.dagger.core.utils.Constants;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Longbow proto data.
 */
public class LongbowProtoData implements LongbowData {
    /**
     * The BigTable column family, in bytes, that Longbow data is stored under.
     */
    private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT);

    /**
     * Instantiates a new Longbow proto data.
     */
    public LongbowProtoData() {
    }

    /**
     * Extracts the serialized Protobuf payloads from the scanned BigTable rows.
     *
     * @param scanResult the BigTable scan results, ordered from latest to earliest
     * @return a map with a single entry keyed by the Longbow proto data key whose value is the list
     *         of raw payload byte arrays, one per scanned row
     */
    @Override
    public Map<String, List<byte[]>> parse(List<Result> scanResult) {
        ArrayList<byte[]> data = new ArrayList<>();

        for (int i = 0; i < scanResult.size(); i++) {
            data.add(i, scanResult.get(i).getValue(COLUMN_FAMILY_NAME, Bytes.toBytes(Constants.LONGBOW_QUALIFIER_DEFAULT)));
        }

        HashMap<String, List<byte[]>> longbowData = new HashMap<>();
        longbowData.put(Constants.LONGBOW_PROTO_DATA_KEY, data);
        return longbowData;
    }
}
