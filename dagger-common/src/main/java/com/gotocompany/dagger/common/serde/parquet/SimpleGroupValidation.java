package com.gotocompany.dagger.common.serde.parquet;

import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.Type;

import static org.apache.parquet.schema.Type.Repetition.OPTIONAL;
import static org.apache.parquet.schema.Type.Repetition.REPEATED;
import static org.apache.parquet.schema.Type.Repetition.REQUIRED;

/**
 * Static helpers that inspect the schema of a Parquet {@link SimpleGroup} to determine how its
 * fields are encoded.
 *
 * <p>Dagger reads Parquet records as {@link SimpleGroup} instances and converts them into Flink
 * rows. Because map-typed fields may be written either with the legacy two-field repeated-group
 * layout or with the standard Parquet {@code MAP} logical-type layout, these utilities let the
 * deserializer detect which encoding is present (and whether a field is present and populated at
 * all) before attempting to read values. The class exposes only static methods and is not intended
 * to be instantiated.
 */
public class SimpleGroupValidation {
    /**
     * Checks that a field is declared in the group's schema and actually carries at least one value.
     *
     * @param simpleGroup the Parquet group to inspect
     * @param fieldName   the name of the field to look for
     * @return {@code true} if the schema contains {@code fieldName} and its repetition count is
     *         non-zero, {@code false} otherwise
     */
    public static boolean checkFieldExistsAndIsInitialized(SimpleGroup simpleGroup, String fieldName) {
        return simpleGroup.getType().containsField(fieldName) && simpleGroup.getFieldRepetitionCount(fieldName) != 0;
    }

    /**
     * This method checks if the map field inside the simple group is
     * serialized using this legacy format:
     * {@code
     * <pre>
     *     repeated group &lt;name&gt; {
     *      &lt;repetition-type&gt; &lt;data-type&gt; key;
     *      &lt;repetition-type&gt; &lt;data-type&gt; value;
     *    }
     * </pre>
     * }
     * The outer group is always repeated. key and value are constant field names.
     *
     * @param simpleGroup The SimpleGroup object inside which the map field is present
     * @param fieldName   The name of the map field
     * @return true, if the map structure follows the spec and false otherwise.
     */
    public static boolean checkIsLegacySimpleGroupMap(SimpleGroup simpleGroup, String fieldName) {
        if (!(simpleGroup.getType().getType(fieldName) instanceof GroupType)) {
            return false;
        }
        GroupType nestedMapGroupType = simpleGroup.getType().getType(fieldName).asGroupType();
        return nestedMapGroupType.isRepetition(Type.Repetition.REPEATED)
                && nestedMapGroupType.getFieldCount() == 2
                && nestedMapGroupType.containsField("key")
                && nestedMapGroupType.containsField("value");
    }

    /**
     * This method checks if the map field inside the simple group is
     * serialized using this standard parquet map specification:
     * {@code
     * <pre>
     *         &lt;repetition-type&gt; group &lt;name&gt; (MAP) {
     *           repeated group key_value {
     *               required &lt;data-type&gt; key;
     *               &lt;repetition-type&gt; &lt;data-type&gt; value;
     *           }
     *          }
     * </pre>
     * }
     * The validation checks below follow the <a href="https://github.com/apache/parquet-format/blob/master/LogicalTypes.md#maps">Apache Parquet LogicalTypes Specification</a>for Maps.
     *
     * @param simpleGroup The SimpleGroup object inside which the map field is present
     * @param fieldName   The name of the map field
     * @return true, if the map structure follows the spec and false otherwise.
     */
    public static boolean checkIsStandardSimpleGroupMap(SimpleGroup simpleGroup, String fieldName) {
        return applyMapFieldValidations(simpleGroup, fieldName)
                && applyNestedKeyValueFieldValidations(simpleGroup, fieldName);
    }

    /**
     * Validates the outer group of a candidate standard Parquet map field.
     *
     * <p>For the field to qualify it must be a {@link GroupType} whose repetition is {@code OPTIONAL}
     * or {@code REQUIRED}, whose logical type annotation is {@link LogicalTypeAnnotation#mapType()},
     * and which has exactly one child (the nested {@code key_value} group).
     *
     * @param simpleGroup the Parquet group containing the candidate map field
     * @param fieldName   the name of the candidate map field
     * @return {@code true} if the outer map group matches the standard specification, {@code false}
     *         otherwise (including when the field is not a group)
     */
    private static boolean applyMapFieldValidations(SimpleGroup simpleGroup, String fieldName) {
        Type mapType = simpleGroup.getType().getType(fieldName);
        if (mapType instanceof GroupType) {
            GroupType mapGroupType = mapType.asGroupType();
            return (mapGroupType.getRepetition().equals(OPTIONAL)
                    || mapGroupType.isRepetition(REQUIRED))
                    && mapGroupType.getLogicalTypeAnnotation().equals(LogicalTypeAnnotation.mapType())
                    && mapGroupType.getFieldCount() == 1;
        }
        return false;
    }

    /**
     * Validates the nested {@code key_value} group of a candidate standard Parquet map field.
     *
     * <p>Assumes the outer field is already known to be a group. The nested {@code key_value} child
     * must exist, be a {@code REPEATED} {@link GroupType}, contain a {@code key} field, and that
     * {@code key} must itself be {@code REQUIRED}, matching the Apache Parquet map specification.
     *
     * @param simpleGroup the Parquet group containing the map field
     * @param fieldName   the name of the map field
     * @return {@code true} if the nested key/value structure matches the specification,
     *         {@code false} otherwise
     */
    private static boolean applyNestedKeyValueFieldValidations(SimpleGroup simpleGroup, String fieldName) {
        GroupType mapGroupType = simpleGroup.getType().getType(fieldName).asGroupType();
        if (mapGroupType.containsField("key_value")) {
            Type nestedKeyValueType = mapGroupType.getType("key_value");
            if (nestedKeyValueType instanceof GroupType) {
                GroupType nestedKeyValueGroupType = nestedKeyValueType.asGroupType();
                return nestedKeyValueGroupType.isRepetition(REPEATED)
                        && nestedKeyValueGroupType.containsField("key")
                        && nestedKeyValueGroupType.getType("key").isRepetition(REQUIRED);
            }
        }
        return false;
    }
}
