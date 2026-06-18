package com.gotocompany.dagger.core.processors.common;

import com.gotocompany.dagger.core.processors.types.Validator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * The Output mapping.
 */
public class OutputMapping implements Serializable, Validator {

    /**
     * The JSON path expression that selects the value to extract from the external response.
     */
    private String path;

    /**
     * Instantiates a new Output mapping.
     *
     * @param path the path
     */
    public OutputMapping(String path) {
        this.path = path;
    }

    /**
     * Gets path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the fields that must be present for this mapping to be considered valid.
     *
     * <p>The output mapping requires a non-null {@code path}, returned keyed by its field name so
     * the validator can ensure it is configured.
     *
     * @return a map of mandatory field names to their configured values
     */
    public HashMap<String, Object> getMandatoryFields() {
        HashMap<String, Object> mandatoryFields = new HashMap<>();
        mandatoryFields.put("path", path);
        return mandatoryFields;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Two output mappings are equal when they refer to the same {@code path}.
     *
     * @param o the object to compare with
     * @return {@code true} if {@code o} is an output mapping with an equal path, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OutputMapping that = (OutputMapping) o;
        return Objects.equals(path, that.path);
    }

    /**
     * {@inheritDoc}
     *
     * @return a hash code derived from the {@code path}
     */
    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
