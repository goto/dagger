package com.gotocompany.dagger.functions.udfs.aggregate;

import com.gotocompany.dagger.common.udfs.AggregateUdf;
import com.gotocompany.dagger.functions.common.Constants;
import com.gotocompany.dagger.functions.udfs.aggregate.accumulator.FeatureWithTypeAccumulator;
import com.gotocompany.dagger.functions.exceptions.InvalidNumberOfArgumentsException;
import com.gotocompany.dagger.functions.udfs.aggregate.feast.handler.ValueEnum;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.annotation.InputGroup;
import org.apache.flink.types.Row;

/**
 * User-defined aggregate function to get Features with type.
 */
@FunctionHint(output = @DataTypeHint("RAW"))
public class FeaturesWithType extends AggregateUdf<Row[], FeatureWithTypeAccumulator> {

    /**
     * Creates a fresh, empty {@link FeatureWithTypeAccumulator} for a new aggregation group.
     *
     * <p>Flink invokes this once per aggregation key to obtain the mutable state used to
     * collect typed key/value feature triplets.
     *
     * @return a new, empty {@link FeatureWithTypeAccumulator} instance
     */
    @Override
    public FeatureWithTypeAccumulator createAccumulator() {
        return new FeatureWithTypeAccumulator();
    }

    /**
     * Returns the accumulated typed features as an array of Feast feature {@link Row} values.
     *
     * <p>Flink calls this to produce the final aggregation output, converting every collected
     * typed feature triplet into its {@link Row} representation.
     *
     * @param featureAccumulator the accumulator holding the collected typed features
     * @return an array of {@link Row} values, one per accumulated feature
     */
    @Override
    public Row[] getValue(FeatureWithTypeAccumulator featureAccumulator) {
        return featureAccumulator.getFeaturesAsRows();
    }

    /**
     * Converts the given list of objects to a FeatureRow type to store in feast(https://github.com/feast-dev/feast)
     * with key and values from first two args from every triplet passed in args and data type according to third element of triplet.
     *
     * @param featureAccumulator the feature accumulator
     * @param objects            the objects
     * @return featuresWithRow the list of featureRows in form of flink Row
     * @author grace.christina
     * @team DS
     */
    public void accumulate(FeatureWithTypeAccumulator featureAccumulator, @DataTypeHint(inputGroup = InputGroup.ANY) Object... objects) {
        validate(objects);
        for (int elementIndex = 0; elementIndex < objects.length; elementIndex += Constants.NUMBER_OF_ARGUMENTS_IN_FEATURE_ACCUMULATOR) {
            featureAccumulator.add(String.valueOf(objects[elementIndex]), objects[elementIndex + 1], ValueEnum.valueOf(String.valueOf(objects[elementIndex + 2])));
        }
    }

    /**
     * Retract.
     *
     * @param featureAccumulator the feature accumulator
     * @param objects            the objects
     */
    public void retract(FeatureWithTypeAccumulator featureAccumulator, Object... objects) {
        validate(objects);
        for (int elementIndex = 0; elementIndex < objects.length; elementIndex += Constants.NUMBER_OF_ARGUMENTS_IN_FEATURE_ACCUMULATOR) {
            featureAccumulator.remove(String.valueOf(objects[elementIndex]), objects[elementIndex + 1], ValueEnum.valueOf(String.valueOf(objects[elementIndex + 2])));
        }
    }

    /**
     * Merges the typed features collected by other accumulators into the target accumulator.
     *
     * <p>Every feature triplet from each {@link FeatureWithTypeAccumulator} in {@code it} is
     * re-added to {@code featureWithTypeAccumulator}, combining partial aggregates produced in
     * parallel while preserving the de-duplication keyed on feature name and value.
     *
     * @param featureWithTypeAccumulator the accumulator that receives the merged features
     * @param it                         the other accumulators whose features are merged in
     */
    public void merge(FeatureWithTypeAccumulator featureWithTypeAccumulator, Iterable<FeatureWithTypeAccumulator> it) {
        for (FeatureWithTypeAccumulator accumulatorInstance : it) {
            accumulatorInstance.getFeatures().forEach((s, tuple3) -> featureWithTypeAccumulator.add(tuple3.f0, tuple3.f1, tuple3.f2));
        }
    }

    /**
     * Validates that the supplied arguments form complete feature triplets.
     *
     * <p>Each feature requires a fixed number of arguments
     * ({@link Constants#NUMBER_OF_ARGUMENTS_IN_FEATURE_ACCUMULATOR}: name, value and type), so the
     * total number of arguments must be an exact multiple of that group size.
     *
     * @param objects the raw arguments passed to {@code accumulate} or {@code retract}
     * @throws InvalidNumberOfArgumentsException if the number of arguments is not a multiple of the
     *                                           required triplet size
     */
    private void validate(Object[] objects) {
        if (objects.length % Constants.NUMBER_OF_ARGUMENTS_IN_FEATURE_ACCUMULATOR != 0) {
            throw new InvalidNumberOfArgumentsException();
        }
    }


}
