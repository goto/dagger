package com.gotocompany.dagger.functions.udfs.scalar;

import com.gotocompany.dagger.functions.exceptions.KeyDoesNotExistException;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartAspects;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartScalarUdf;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.DartDataStore;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.MapCache;

import java.util.HashMap;
import java.util.Map;

/**
 * The DartGet udf.
 */
public class DartGet extends DartScalarUdf {
    private final DartDataStore dataStore;
    private final Map<String, MapCache> cache;

    /**
     * Instantiates a new Dart get.
     *
     * @param dataStore the data store
     */
    public DartGet(DartDataStore dataStore) {
        this.dataStore = dataStore;
        cache = new HashMap<>();
    }

    /**
     * To fetch a corresponding value in a collection given a key from data point.
     *
     * @param collectionName     the collection name
     * @param key                the key
     * @param refreshRateInHours ttl
     * @return the value in string
     * @author gaurav.s
     * @team DE
     */
    public String eval(String collectionName, String key, Integer refreshRateInHours) {
        if (cache.isEmpty() || !cache.containsKey(collectionName) || cache.get(collectionName).hasExpired(refreshRateInHours) || cache.get(collectionName).isEmpty()) {
            cache.put(collectionName, dataStore.getMap(collectionName, getMeterStatsManager(), getGaugeStatsManager()));
            getMeterStatsManager().markEvent(DartAspects.DART_GCS_FETCH_SUCCESS);
        }
        getMeterStatsManager().markEvent(DartAspects.DART_CACHE_HIT);
        return cache.get(collectionName).get(key);
    }

    /**
     * Corresponding value in a GCS bucket given a key from data point.
     *
     * @param collectionName     the collection name
     * @param key                the key
     * @param refreshRateInHours the refresh rate in hours
     * @param defaultValue       the default value
     * @return the string
     */
    public String eval(String collectionName, String key, Integer refreshRateInHours, String defaultValue) {
        try {
            return eval(collectionName, key, refreshRateInHours);
        } catch (KeyDoesNotExistException e) {
            getMeterStatsManager().markEvent(DartAspects.DART_CACHE_MISS);
            return defaultValue;
        }
    }
}
