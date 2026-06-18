package com.gotocompany.dagger.functions.udfs.scalar;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.gotocompany.dagger.common.udfs.ScalarUdf;

/**
 * The type S2AreaInKm2 udf.
 */
public class S2AreaInKm2 extends ScalarUdf {

    /**
     * Total surface area of the Earth in square kilometres, used to scale the cell's fractional area.
     */
    private static final long TOTAL_EARTH_AREA_KM2 = 510072000;

    /**
     * Constant factor ({@code 4}) from the sphere-area formula {@code 4 * PI}, used to normalise the area.
     */
    private static final long FACTOR = 4;

    /**
     * compute area in km2 for a specific s2 cell.
     *
     * @param s2id s2id of the cell
     * @return area in km2
     * @author leyi.seah
     * @team DS Marketplace Pricing
     */
    public double eval(String s2id) {
        long id = Long.parseLong(s2id);
        S2Cell s2Cell = getS2CellfromId(id);
        return (s2Cell.exactArea() * TOTAL_EARTH_AREA_KM2) / (FACTOR * Math.PI);
    }

    /**
     * Builds the {@code S2Cell} corresponding to the given S2 cell identifier.
     *
     * @param id the numeric S2 cell identifier
     * @return the {@code S2Cell} for the supplied identifier
     */
    private S2Cell getS2CellfromId(long id) {
        return new S2Cell(new S2CellId(id));
    }
}
