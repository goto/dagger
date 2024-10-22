package com.gotocompany.dagger.functions.udfs.scalar.dart.store.gcs;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartAspects;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.MapCache;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.SetCache;
import com.gotocompany.dagger.functions.udfs.scalar.DartContains;
import com.gotocompany.dagger.functions.udfs.scalar.DartGet;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.DartDataStore;
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
 * The type Gcs data store.
 */
public class GcsDartDataStore implements DartDataStore, Serializable {

    private final String projectId;

    private final String bucketId;

    private GcsClient gcsClient;

    /**
     * Instantiates a new Gcs data store.
     *
     * @param projectId the project id
     * @param bucketId  the bucket id
     */
    public GcsDartDataStore(String projectId, String bucketId) {
        this.projectId = projectId;
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
        String jsonData = getGcsClient().fetchJsonData(
                DartGet.class.getSimpleName(),
                gaugeManager,
                this.bucketId,
                "dart-get/" + dartName);

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

        String jsonData = getGcsClient().fetchJsonData(DartContains.class.getSimpleName(), gaugeManager, this.bucketId, "dart-contains/" + dartName);
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

    /**
     * Gets gcs client.
     *
     * @return the gcs client
     */
    GcsClient getGcsClient() {
        if (this.gcsClient == null) {
            this.gcsClient = new GcsClient(this.projectId);
        }
        return this.gcsClient;
    }
}
