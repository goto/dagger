package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.common.metrics.managers.MeterStatsManager;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.MapCache;
import com.gotocompany.dagger.functions.udfs.scalar.dart.types.SetCache;

/**
 * The interface Data store.
 */
public interface DartDataStore {
    /**
     * Gets set.
     *
     * @param setName the set name
     * @return the set
     */
    SetCache getSet(String setName, MeterStatsManager meterStatsManager, GaugeStatsManager gaugeStatsManager);

    /**
     * Gets map.
     *
     * @param mapName the map name
     * @return the map
     */
    MapCache getMap(String mapName, MeterStatsManager meterStatsManager, GaugeStatsManager gaugeStatsManager);
}
