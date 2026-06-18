package com.gotocompany.dagger.functions.udfs.aggregate.feast.handler;

/**
 * The enum Value.
 */
public enum ValueEnum {
    /**
     * Boolean type value enum.
     */
    BooleanType(6),
    /**
     * Byte type value enum.
     */
    ByteType(0),
    /**
     * Double type value enum.
     */
    DoubleType(4),
    /**
     * Float type value enum.
     */
    FloatType(5),
    /**
     * Integer type value enum.
     */
    IntegerType(2),
    /**
     * Long type value enum.
     */
    LongType(3),
    /**
     * String type value enum.
     */
    StringType(1),
    /**
     * Timestamp type value enum.
     */
    TimestampType(7);

    /**
     * The Feast value slot index associated with this type.
     */
    private Integer value;

    /**
     * Creates an enum constant bound to its Feast value slot index.
     *
     * @param value the Feast value slot index this type maps to
     */
    ValueEnum(Integer value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public Integer getValue() {
        return value;
    }
}
