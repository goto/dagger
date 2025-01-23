package com.gotocompany.dagger.functions.udfs.scalar;

import com.gotocompany.dagger.functions.udfs.scalar.dart.DartAspects;
import com.gotocompany.dagger.functions.udfs.scalar.dart.DartScalarUdf;
import com.gotocompany.dagger.functions.udfs.scalar.dart.store.DartDataStore;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.SetCache;

import java.util.HashMap;
import java.util.Map;

/**
 * The DartContains udf.
 */
public class DartContains extends DartScalarUdf {
    private final DartDataStore dataStore;
    private final Map<String, SetCache> setCache;

    /**
     * Instantiates a new Dart contains.
     *
     * @param dataStore the data store
     */
    public DartContains(DartDataStore dataStore) {
        this.dataStore = dataStore;
        setCache = new HashMap<>();
    }

    /**
     * To check if a data point in the message is present in the Redis collection.
     *
     * @param listName the list name
     * @param field    the field
     * @return the boolean
     */
    public boolean eval(String listName, String field) {
        return eval(listName, field, 1);
    }

    /**
     * To check if a data point in the message is present in the Redis collection.
     *
     * @param listName           the list name
     * @param field              the field
     * @param refreshRateInHours ttl
     * @return the boolean
     * @author gaurav.s
     * @team DE
     */
    public boolean eval(String listName, String field, Integer refreshRateInHours) {
        SetCache listData = getListData(listName, field, refreshRateInHours);
        boolean isPresent = listData.contains(field);
        updateMetrics(isPresent);
        return isPresent;
    }

    /**
     * Check if a data point in the message is present in the GCS bucket.
     *
     * @param listName the list name
     * @param field    the field
     * @param regex    the regex
     * @return the boolean
     */
    public boolean eval(String listName, String field, String regex) {
        return eval(listName, field, regex, 1);
    }

    /**
     * Check if a data point in the message is present in the GCS bucket.
     *
     * @param listName           the list name
     * @param field              the field
     * @param regex              the regex
     * @param refreshRateInHours the refresh rate in hours
     * @return the boolean
     */
    public boolean eval(String listName, String field, String regex, Integer refreshRateInHours) {
        SetCache listData = getListData(listName, field, refreshRateInHours);
        boolean isPresent = listData.matches(field, regex);
        updateMetrics(isPresent);
        return isPresent;
    }

    private SetCache getListData(String listName, String field, int refreshRateInHours) {
        if (setCache.isEmpty() || !setCache.containsKey(listName) || setCache.get(listName).hasExpired(refreshRateInHours) || setCache.get(listName).isEmpty()) {
            setCache.put(listName, dataStore.getSet(listName, getMeterStatsManager(), getGaugeStatsManager()));
            getMeterStatsManager().markEvent(DartAspects.DART_GCS_FETCH_SUCCESS);
        }
        return setCache.get(listName);
    }

    private void updateMetrics(boolean isPresent) {
        if (isPresent) {
            getMeterStatsManager().markEvent(DartAspects.DART_CACHE_HIT);
        } else {
            getMeterStatsManager().markEvent(DartAspects.DART_CACHE_MISS);
        }
    }
}
