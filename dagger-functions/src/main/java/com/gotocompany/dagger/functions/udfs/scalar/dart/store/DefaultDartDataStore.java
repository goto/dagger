package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartAspects;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.MapCache;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.SetCache;
import com.gotocompany.dagger.functions.udfs.scalar.DartContains;
import com.gotocompany.dagger.functions.udfs.scalar.DartGet;
import lombok.Getter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DefaultDartDataStore would be able to fetch the darts from the object storage services.
 * pass the relevant client which implements {@link DartDataStoreClient}
 */
public class DefaultDartDataStore implements DartDataStore, Serializable {

    public static final String DART_GET_DIRECTORY = "dart-get/";
    public static final String DART_CONTAINS_DIRECTORY = "dart-contains/";

    private final String bucketId;

    @Getter
    private final DartDataStoreClient storeClient;

    /**
     * Instantiates a new data store.
     *
     * @param storeClient a {@link DartDataStoreClient} implementation for the respective object storage provider
     * @param bucketId    the bucket id
     */
    public DefaultDartDataStore(DartDataStoreClient storeClient, String bucketId) {
        this.storeClient = storeClient;
        this.bucketId = bucketId;
    }

    @Override
    public SetCache getSet(String setName, MeterStatsManager meterStatsManager, GaugeStatsManager gaugeManager) {
        return new SetCache(getSetOfObjects(setName, meterStatsManager, gaugeManager));
    }

    @Override
    public MapCache getMap(String mapName, MeterStatsManager meterStatsManager, GaugeStatsManager gaugeManager) {
        Map<String, String> mapOfObjects = getMapOfObjects(mapName, meterStatsManager, gaugeManager);
        return new MapCache(mapOfObjects);
    }

    private Map<String, String> getMapOfObjects(String dartName, MeterStatsManager meterManager, GaugeStatsManager gaugeManager) {
        String jsonData = getStoreClient().fetchJsonData(
                DartGet.class.getSimpleName(),
                gaugeManager,
                this.bucketId,
                DART_GET_DIRECTORY + dartName);

        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> map = null;
        try {
            map = mapper.readValue(jsonData, Map.class);
        } catch (IOException e) {
            meterManager.markEvent(DartAspects.DART_GCS_FETCH_FAILURES);
            e.printStackTrace();
        }
        return map;
    }

    private Set<String> getSetOfObjects(String dartName, MeterStatsManager meterManager, GaugeStatsManager gaugeManager) {
        String jsonData = getStoreClient().fetchJsonData(DartContains.class.getSimpleName(), gaugeManager, this.bucketId, DART_CONTAINS_DIRECTORY + dartName);
        ObjectMapper mapper = new ObjectMapper();
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(jsonData);
            JsonNode arrayNode = node.get("data");
            List<String> list = mapper.readValue(arrayNode.traverse(),
                    new TypeReference<ArrayList<String>>() {
                    });

            return new HashSet<>(list);
        } catch (Exception e) {
            meterManager.markEvent(DartAspects.DART_GCS_FETCH_FAILURES);
            e.printStackTrace();
        }

        return new HashSet<>();
    }
}
