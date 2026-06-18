package com.gotocompany.dagger.functions.udfs.aggregate.accumulator;

import org.apache.flink.table.annotation.DataTypeHint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The accumulator for CollectArray udf.
 */
public class ArrayAccumulator implements Serializable {

    /**
     * Backing list that stores every object collected by the owning aggregation.
     */
    private @DataTypeHint("RAW") List<Object> arrayList = new ArrayList<>();

    /**
     * Add object to array list.
     *
     * @param object the object
     */
    public void add(Object object) {
        arrayList.add(object);
    }

    /**
     * Emit array list.
     *
     * @return the array list
     */
    public List<Object> emit() {
        return arrayList;
    }

    /**
     * Returns the backing list of accumulated objects.
     *
     * <p>Primarily intended for state access during {@code merge} and for serialization.
     *
     * @return the mutable list of collected objects
     */
    public List<Object> getArrayList() {
        return arrayList;
    }

    /**
     * Replaces the backing list of accumulated objects.
     *
     * @param arrayList the list of objects to use as the accumulator state
     */
    public void setArrayList(List<Object> arrayList) {
        this.arrayList = arrayList;
    }
}
