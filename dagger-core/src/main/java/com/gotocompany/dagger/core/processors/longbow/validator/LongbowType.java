package com.gotocompany.dagger.core.processors.longbow.validator;

import com.gotocompany.dagger.core.utils.Constants;

import static com.gotocompany.dagger.common.core.Constants.ROWTIME;

/**
 * The enum Longbow type.
 */
public enum LongbowType {
    /**
     * Read-only Longbow that scans previously written BigTable data back into the stream.
     */
    LongbowRead(LongbowKey.LONGBOW_READ, MandatoryFields.LONGBOW_READ, InvalidFields.LONGBOW_READ),
    /**
     * Write-only Longbow that persists the input stream into BigTable for later reads.
     */
    LongbowWrite(LongbowKey.LONGBOW_WRITE, MandatoryFields.LONGBOW_WRITE, InvalidFields.LONGBOW_WRITE),
    /**
     * Combined Longbow that both writes the input and reads back the configured range in one flow.
     */
    LongbowProcess(LongbowKey.LONGBOW_PROCESS, MandatoryFields.LONGBOW_PROCESS, InvalidFields.LONGBOW_PROCESS);

    /**
     * Suffix stripped from the key name to derive the human-readable Longbow type name.
     */
    private static final String LONGBOW_TYPE_PREFIX = "_key";
    /**
     * The schema key whose presence identifies this Longbow type.
     */
    private String keyName;
    /**
     * The fields that must be present in the schema for this Longbow type to be valid.
     */
    private String[] mandatoryFields;
    /**
     * The fields that must not be present in the schema for this Longbow type to be valid.
     */
    private String[] invalidFields;

    /**
     * Instantiates a new Longbow type.
     *
     * @param keyName         the schema key that identifies this Longbow type
     * @param mandatoryFields the fields required for this Longbow type
     * @param invalidFields   the fields disallowed for this Longbow type
     */
    LongbowType(String keyName, String[] mandatoryFields, String[] invalidFields) {
        this.keyName = keyName;
        this.mandatoryFields = mandatoryFields;
        this.invalidFields = invalidFields;
    }

    /**
     * Gets key name.
     *
     * @return the key name
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Get mandatory fields.
     *
     * @return the mandatory fields
     */
    public String[] getMandatoryFields() {
        return mandatoryFields;
    }

    /**
     * Get invalid fields.
     *
     * @return the array of invalid fields
     */
    public String[] getInvalidFields() {
        return invalidFields;
    }

    /**
     * Gets type name.
     *
     * @return the type name
     */
    public String getTypeName() {
        return keyName.replace(LONGBOW_TYPE_PREFIX, "");
    }

    /**
     * Holds the mandatory schema fields required by each Longbow type.
     */
    private static class MandatoryFields {
        /**
         * Mandatory fields for the combined Longbow process type.
         */
        private static final String[] LONGBOW_PROCESS = new String[]{Constants.LONGBOW_DATA_KEY, Constants.EVENT_TIMESTAMP, ROWTIME};
        /**
         * Mandatory fields for the Longbow write type.
         */
        private static final String[] LONGBOW_WRITE = new String[]{ROWTIME, Constants.EVENT_TIMESTAMP};
        /**
         * Mandatory fields for the Longbow read type.
         */
        private static final String[] LONGBOW_READ = new String[]{Constants.EVENT_TIMESTAMP};
    }

    /**
     * Holds the identifying schema key for each Longbow type.
     */
    private static class LongbowKey {
        /**
         * Schema key identifying the combined Longbow process type.
         */
        private static final String LONGBOW_PROCESS = "longbow_key";
        /**
         * Schema key identifying the Longbow write type.
         */
        private static final String LONGBOW_WRITE = "longbow_write_key";
        /**
         * Schema key identifying the Longbow read type.
         */
        private static final String LONGBOW_READ = "longbow_read_key";
    }

    /**
     * Holds the disallowed schema fields for each Longbow type.
     */
    private static class InvalidFields {
        /**
         * Disallowed fields for the combined Longbow process type.
         */
        private static final String[] LONGBOW_PROCESS = new String[]{Constants.LONGBOW_PROTO_DATA_KEY};
        /**
         * Disallowed fields for the Longbow write type.
         */
        private static final String[] LONGBOW_WRITE = new String[]{Constants.LONGBOW_PROTO_DATA_KEY, Constants.LONGBOW_DATA_KEY, Constants.LONGBOW_LATEST_KEY, Constants.LONGBOW_EARLIEST_KEY, Constants.LONGBOW_DURATION_KEY};
        /**
         * Disallowed fields for the Longbow read type.
         */
        private static final String[] LONGBOW_READ = new String[]{Constants.LONGBOW_DATA_KEY, Constants.LONGBOW_PROTO_DATA_KEY};
    }
}
