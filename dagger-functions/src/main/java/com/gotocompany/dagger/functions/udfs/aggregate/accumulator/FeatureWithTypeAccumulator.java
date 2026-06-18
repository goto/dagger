package com.gotocompany.dagger.functions.udfs.aggregate.accumulator;

import com.gotocompany.dagger.functions.udfs.aggregate.feast.FeatureUtils;
import com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.types.Row;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The accumulator for FeatureWithType udf.
 */
public class FeatureWithTypeAccumulator implements Serializable {
    /**
     * Fixed number of fields used to build each emitted Feast feature {@link Row}.
     */
    private static final Integer FEATURE_ROW_LENGTH = 3;

    /**
     * Backing map of de-duplicated typed feature triplets, keyed by feature name and hash.
     */
    private @DataTypeHint("RAW") HashMap<String, Tuple3<String, Object, ValueEnum>> features = new HashMap<>();

    /**
     * Add features.
     *
     * @param key   the key
     * @param value the value
     * @param type  the type
     */
    public void add(String key, Object value, ValueEnum type) {
        Tuple3<String, Object, ValueEnum> featureTuple = new Tuple3<>(key, value, type);
        features.put(getMapKey(key, featureTuple.hashCode()), featureTuple);
    }

    /**
     * Get features rows.
     *
     * @return the rows
     */
    public Row[] getFeaturesAsRows() {
        ArrayList<Row> featureRows = new ArrayList<>();
        for (Tuple3<String, Object, ValueEnum> feature : features.values()) {
            String key = feature.f0;
            Object value = feature.f1;
            ValueEnum type = feature.f2;
            FeatureUtils.populateFeaturesWithType(featureRows, key, value, type, FEATURE_ROW_LENGTH);
        }
        return featureRows.toArray(new Row[0]);
    }

    /**
     * Remove features.
     *
     * @param key   the key
     * @param value the value
     * @param type  the type
     */
    public void remove(String key, Object value, ValueEnum type) {
        Tuple3<String, Object, ValueEnum> featureTuple = new Tuple3<>(key, value, type);
        features.remove(getMapKey(key, featureTuple.hashCode()));
    }

    /**
     * Returns the backing map of collected typed features.
     *
     * <p>Primarily intended for state access during {@code merge} and for serialization.
     *
     * @return the mutable map of de-duplicated typed feature triplets
     */
    public HashMap<String, Tuple3<String, Object, ValueEnum>> getFeatures() {
        return features;
    }

    /**
     * Replaces the backing map of collected typed features.
     *
     * @param features the map of typed feature triplets to use as the accumulator state
     */
    public void setFeatures(HashMap<String, Tuple3<String, Object, ValueEnum>> features) {
        this.features = features;
    }

    /**
     * Builds the map key used to de-duplicate a feature within the accumulator.
     *
     * <p>The key combines the feature name with the hash code of its full triplet so that
     * features differing in value or type are stored under distinct keys.
     *
     * @param key      the feature name
     * @param hashcode the hash code of the feature triplet
     * @return the composite map key in the form {@code name-hashcode}
     */
    private String getMapKey(String key, Integer hashcode) {
        return String.format("%s-%d", key, hashcode);
    }
}
