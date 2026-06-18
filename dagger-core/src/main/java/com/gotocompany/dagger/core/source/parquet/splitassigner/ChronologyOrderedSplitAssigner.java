package com.gotocompany.dagger.core.source.parquet.splitassigner;

import com.gotocompany.dagger.core.exception.PathParserNotProvidedException;
import com.gotocompany.dagger.core.metrics.aspects.ChronologyOrderedSplitAssignerAspects;
import com.gotocompany.dagger.core.metrics.reporters.statsd.SerializedStatsDReporterSupplier;
import com.gotocompany.dagger.core.metrics.reporters.statsd.StatsDErrorReporter;
import com.gotocompany.dagger.core.metrics.reporters.statsd.manager.DaggerGaugeManager;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.ComponentTags;
import com.gotocompany.dagger.core.metrics.reporters.statsd.tags.StatsDTag;
import com.gotocompany.dagger.core.source.config.models.TimeRangePool;
import com.gotocompany.dagger.core.source.parquet.path.PathParser;
import org.apache.flink.connector.file.src.FileSourceSplit;
import org.apache.flink.connector.file.src.assigners.FileSplitAssigner;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import static com.google.api.client.util.Preconditions.checkArgument;

/**
 * {@link FileSplitAssigner} that hands out Parquet file splits in ascending chronological order of
 * the event-time {@link Instant} parsed from each split's file path.
 *
 * <p>Discovered splits are enriched into {@link InstantEnrichedSplit}s and held in a
 * {@link PriorityBlockingQueue} ordered by their instant, so earlier partitions are always processed
 * first. An optional {@link TimeRangePool} filters out splits whose instant falls outside the
 * configured date ranges. Split discovery and assignment progress are exported as StatsD gauges, and
 * a path that cannot be parsed results in a fatal, reported {@link IllegalArgumentException}.
 * Instances are created through the nested {@link ChronologyOrderedSplitAssignerBuilder}.
 */
public class ChronologyOrderedSplitAssigner implements FileSplitAssigner {
    /**
     * Priority queue of pending splits, ordered by ascending event-time instant.
     */
    private final PriorityBlockingQueue<InstantEnrichedSplit> unassignedSplits;
    /**
     * Initial capacity of the priority queue (required by its constructor; not a hard limit).
     */
    private static final int INITIAL_DEFAULT_CAPACITY = 11;
    /**
     * Parser that extracts the event-time instant from each split's file path.
     */
    private PathParser pathParser;
    /**
     * Optional set of allowed time ranges; splits outside it are discarded. May be {@code null}.
     */
    private TimeRangePool timeRangePool;
    /**
     * Gauge manager exporting split discovery and assignment counts to StatsD.
     */
    private DaggerGaugeManager daggerGaugeManager;
    /**
     * Reporter used to surface fatal path-parsing errors to StatsD.
     */
    private final StatsDErrorReporter statsDErrorReporter;

    /**
     * Creates an assigner over the discovered splits; use
     * {@link ChronologyOrderedSplitAssignerBuilder}.
     *
     * <p>Initializes the priority queue with the instant-based comparator and the gauge manager, then
     * validates and enqueues the supplied splits.
     *
     * @param fileSourceSplits       the splits discovered by Flink for this source
     * @param pathParser             the parser extracting an instant from each split's path
     * @param timeRangePool          optional allowed time ranges, or {@code null} to accept all
     * @param statsDReporterSupplier supplier of the StatsD reporter for metrics and errors
     */
    private ChronologyOrderedSplitAssigner(Collection<FileSourceSplit> fileSourceSplits, PathParser pathParser,
                                           TimeRangePool timeRangePool, SerializedStatsDReporterSupplier statsDReporterSupplier) {
        this.pathParser = pathParser;
        this.timeRangePool = timeRangePool;
        this.statsDErrorReporter = new StatsDErrorReporter(statsDReporterSupplier);
        this.unassignedSplits = new PriorityBlockingQueue<>(INITIAL_DEFAULT_CAPACITY, getFileSourceSplitComparator());
        this.daggerGaugeManager = new DaggerGaugeManager(statsDReporterSupplier);
        initAndValidate(fileSourceSplits);
    }

    /**
     * Registers split-assigner gauges and enqueues every discovered split after validation.
     *
     * <p>Records the total discovered, total recorded (after filtering), and awaiting-assignment
     * counts as StatsD gauges.
     *
     * @param fileSourceSplits the splits discovered by Flink for this source
     */
    private void initAndValidate(Collection<FileSourceSplit> fileSourceSplits) {
        StatsDTag[] splitAssignerTags = ComponentTags.getSplitAssignerTags();
        daggerGaugeManager.register(splitAssignerTags);
        daggerGaugeManager.markValue(ChronologyOrderedSplitAssignerAspects.TOTAL_SPLITS_DISCOVERED, fileSourceSplits.size());
        for (FileSourceSplit split : fileSourceSplits) {
            validateAndAddSplits(split);
        }
        daggerGaugeManager.markValue(ChronologyOrderedSplitAssignerAspects.TOTAL_SPLITS_RECORDED, unassignedSplits.size());
        daggerGaugeManager.markValue(ChronologyOrderedSplitAssignerAspects.SPLITS_AWAITING_ASSIGNMENT, unassignedSplits.size());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Polls the earliest (lowest-instant) split from the queue, updating the awaiting-assignment
     * gauge. The {@code hostname} hint is ignored because ordering is purely chronological.
     *
     * @param hostname the host requesting a split, or {@code null}; ignored by this assigner
     * @return the next split in chronological order, or {@link Optional#empty()} if none remain
     */
    @Override
    public Optional<FileSourceSplit> getNext(@Nullable String hostname) {
        InstantEnrichedSplit instantEnrichedSplit = unassignedSplits.poll();
        if (instantEnrichedSplit == null) {
            return Optional.empty();
        }
        daggerGaugeManager.markValue(ChronologyOrderedSplitAssignerAspects.SPLITS_AWAITING_ASSIGNMENT, unassignedSplits.size());
        return Optional.of(instantEnrichedSplit.getFileSourceSplit());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Validates and re-enqueues the given splits (for example, splits returned by a failed
     * reader), preserving the chronological ordering of the queue.
     *
     * @param splits the splits to add for (re)assignment
     */
    @Override
    public void addSplits(Collection<FileSourceSplit> splits) {
        for (FileSourceSplit split : splits) {
            validateAndAddSplits(split);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the underlying {@link FileSourceSplit}s still awaiting assignment, unwrapped from
     * their {@link InstantEnrichedSplit} holders. The returned order is not guaranteed to be sorted.
     *
     * @return the file source splits not yet assigned
     */
    @Override
    public Collection<FileSourceSplit> remainingSplits() {
        return unassignedSplits
                .stream()
                .map(InstantEnrichedSplit::getFileSourceSplit)
                .collect(Collectors.toList());
    }

    /**
     * Parses a split's instant and enqueues it when it falls within the configured time ranges.
     *
     * <p>Splits whose instant is outside the {@link TimeRangePool} are silently dropped. A path that
     * cannot be parsed results in a fatal, reported {@link IllegalArgumentException}.
     *
     * @param split the split to validate and conditionally enqueue
     * @throws IllegalArgumentException if the split's path cannot be parsed into an instant
     */
    private void validateAndAddSplits(FileSourceSplit split) {
        try {
            Instant instant = pathParser.instantFromFilePath(split.path());
            if (timeRangePool == null || timeRangePool.contains(instant)) {
                this.unassignedSplits.add(new InstantEnrichedSplit(split, instant));
            }
        } catch (ParseException ex) {
            IllegalArgumentException exception = new IllegalArgumentException(ex);
            statsDErrorReporter.reportFatalException(exception);
            throw exception;
        }
    }

    /**
     * Builds the comparator that orders splits by ascending event-time instant.
     *
     * @return a comparator placing earlier instants before later ones
     */
    private Comparator<InstantEnrichedSplit> getFileSourceSplitComparator() {
        return (instantEnrichedSplit1, instantEnrichedSplit2) -> {
            Instant instant1 = instantEnrichedSplit1.getInstant();
            Instant instant2 = instantEnrichedSplit2.getInstant();
            if (instant1.isBefore(instant2)) {
                return -1;
            } else if (instant1.isAfter(instant2)) {
                return 1;
            } else {
                return 0;
            }
        };
    }

    /**
     * Serializable builder for {@link ChronologyOrderedSplitAssigner}.
     *
     * <p>The split collection is supplied later by Flink (via {@link #build(Collection)} used as a
     * {@code FileSplitAssigner.Provider}), so only the path parser, optional date range, and StatsD
     * supplier are configured here.
     */
    public static class ChronologyOrderedSplitAssignerBuilder implements Serializable {
        /**
         * Parser used to extract an instant from each split's file path; required.
         */
        private PathParser pathParser;
        /**
         * Optional allowed time ranges used to filter splits; may remain {@code null}.
         */
        private TimeRangePool parquetFileDateRange;
        /**
         * Supplier of the StatsD reporter for metrics and error reporting; required.
         */
        private SerializedStatsDReporterSupplier statsDReporterSupplier;

        /**
         * Sets the path parser used to derive each split's event-time instant.
         *
         * @param parser the path parser to use
         * @return this builder
         */
        public ChronologyOrderedSplitAssignerBuilder addPathParser(PathParser parser) {
            this.pathParser = parser;
            return this;
        }

        /**
         * Sets the optional time ranges used to filter splits by their instant.
         *
         * @param timeRangePool the allowed time ranges, or {@code null} to accept all splits
         * @return this builder
         */
        public ChronologyOrderedSplitAssignerBuilder addTimeRanges(TimeRangePool timeRangePool) {
            this.parquetFileDateRange = timeRangePool;
            return this;
        }

        /**
         * Sets the supplier of the StatsD reporter for metrics and error reporting.
         *
         * @param supplier the StatsD reporter supplier
         * @return this builder
         */
        public ChronologyOrderedSplitAssignerBuilder addStatsDReporterSupplier(SerializedStatsDReporterSupplier supplier) {
            this.statsDReporterSupplier = supplier;
            return this;
        }

        /**
         * Builds the assigner for the splits Flink has discovered.
         *
         * <p>This method matches the {@code FileSplitAssigner.Provider} functional shape and is what
         * {@code ParquetDaggerSource} passes to the file source. It requires a StatsD supplier and a
         * path parser; a missing dependency is reported to StatsD and thrown.
         *
         * @param fileSourceSplits the splits discovered by Flink for this source
         * @return a configured {@link ChronologyOrderedSplitAssigner}
         * @throws IllegalArgumentException       if no StatsD reporter supplier was configured
         * @throws PathParserNotProvidedException if no path parser was configured
         */
        public ChronologyOrderedSplitAssigner build(Collection<FileSourceSplit> fileSourceSplits) {
            checkArgument(statsDReporterSupplier != null, "SerializedStatsDReporterSupplier is required but is set as null");
            if (pathParser == null) {
                PathParserNotProvidedException exception = new PathParserNotProvidedException("Path parser is null");
                new StatsDErrorReporter(statsDReporterSupplier).reportFatalException(exception);
                throw exception;
            }
            return new ChronologyOrderedSplitAssigner(fileSourceSplits, pathParser, parquetFileDateRange, statsDReporterSupplier);
        }
    }
}
