package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartAspects;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.MapCache;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.SetCache;
import com.gotocompany.dagger.functions.udfs.scalar.DartContains;
import com.gotocompany.dagger.functions.udfs.scalar.DartGet;
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

    /**
     * The object-storage directory prefix under which {@code dart-get} map payloads are stored.
     */
    public static final String DART_GET_DIRECTORY = "dart-get/";
    /**
     * The object-storage directory prefix under which {@code dart-contains} set payloads are stored.
     */
    public static final String DART_CONTAINS_DIRECTORY = "dart-contains/";

    /**
     * Provider of the backend-specific client used to fetch dart JSON payloads from object storage.
     */
    private final DartDataStoreClientProvider clientProvider;
    /**
     * The identifier of the object-storage bucket that holds the dart data.
     */
    private final String bucketId;
    /**
     * The Dagger configuration used to resolve dart-related settings.
     */
    private final Configuration configuration;

    /**
     * Instantiates a new data store.
     *
     * @param clientProvider a {@link DartDataStoreClient} implementation for the respective object storage provider
     * @param bucketId       the bucket id
     */
    public DefaultDartDataStore(DartDataStoreClientProvider clientProvider, String bucketId, Configuration configuration) {
        this.clientProvider = clientProvider;
        this.bucketId = bucketId;
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Fetches the {@code dart-contains} payload for the given set name from object storage and
     * wraps the resulting values in a {@link SetCache}.
     *
     * @param setName           the name of the dart set to load
     * @param meterStatsManager the meter manager used to record fetch successes and failures
     * @param gaugeManager      the gauge manager used to record payload size and path telemetry
     * @return a {@link SetCache} backed by the fetched set of values
     */
    @Override
    public SetCache getSet(String setName, MeterStatsManager meterStatsManager, GaugeStatsManager gaugeManager) {
        return new SetCache(getSetOfObjects(setName, meterStatsManager, gaugeManager));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Fetches the {@code dart-get} payload for the given map name from object storage and wraps
     * the resulting key-value pairs in a {@link MapCache}.
     *
     * @param mapName           the name of the dart map to load
     * @param meterStatsManager the meter manager used to record fetch successes and failures
     * @param gaugeManager      the gauge manager used to record payload size and path telemetry
     * @return a {@link MapCache} backed by the fetched map of values
     */
    @Override
    public MapCache getMap(String mapName, MeterStatsManager meterStatsManager, GaugeStatsManager gaugeManager) {
        Map<String, String> mapOfObjects = getMapOfObjects(mapName, meterStatsManager, gaugeManager);
        return new MapCache(mapOfObjects);
    }

    /**
     * Fetches and parses the {@code dart-get} JSON payload for the given dart name into a key-value map.
     *
     * <p>On a parsing failure the error is recorded via {@link DartAspects#DART_GCS_FETCH_FAILURES}
     * and {@code null} is returned.
     *
     * @param dartName     the name of the dart whose map payload should be fetched
     * @param meterManager the meter manager used to record fetch failures
     * @param gaugeManager the gauge manager used to record path and size telemetry
     * @return the parsed {@code Map<String, String>} of key-value pairs, or {@code null} when the payload cannot be parsed
     */
    private Map<String, String> getMapOfObjects(String dartName, MeterStatsManager meterManager, GaugeStatsManager gaugeManager) {
        String jsonData = clientProvider.getDartDataStoreClient().fetchJsonData(
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

    /**
     * Fetches and parses the {@code dart-contains} JSON payload for the given dart name into a set of values.
     *
     * <p>The payload is expected to contain a {@code "data"} array of strings; on any failure the error
     * is recorded via {@link DartAspects#DART_GCS_FETCH_FAILURES} and an empty set is returned.
     *
     * @param dartName     the name of the dart whose set payload should be fetched
     * @param meterManager the meter manager used to record fetch failures
     * @param gaugeManager the gauge manager used to record path and size telemetry
     * @return the parsed {@code Set<String>} of values, or an empty set when the payload cannot be parsed
     */
    private Set<String> getSetOfObjects(String dartName, MeterStatsManager meterManager, GaugeStatsManager gaugeManager) {
        String jsonData = clientProvider.getDartDataStoreClient().fetchJsonData(DartContains.class.getSimpleName(), gaugeManager, this.bucketId, DART_CONTAINS_DIRECTORY + dartName);
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
