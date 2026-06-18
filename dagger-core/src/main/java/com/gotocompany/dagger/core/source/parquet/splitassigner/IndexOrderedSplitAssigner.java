package com.gotocompany.dagger.core.source.parquet.splitassigner;

import org.apache.flink.connector.file.src.FileSourceSplit;
import org.apache.flink.connector.file.src.assigners.FileSplitAssigner;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

/* TODO */
/**
 * Placeholder {@link FileSplitAssigner} intended to assign Parquet file splits in their index
 * (discovery) order.
 *
 * <p>This implementation is not yet functional: the constructor ignores the provided splits and the
 * accessor methods return empty or {@code null} results. It corresponds to the
 * {@code EARLIEST_INDEX_FIRST} read-order strategy, which {@code ParquetDaggerSource} currently
 * rejects as unsupported. It is retained as a stub for a future index-ordered assignment strategy.
 */
public class IndexOrderedSplitAssigner implements FileSplitAssigner {

    /**
     * Creates a placeholder assigner; the supplied splits are currently ignored.
     *
     * @param fileSourceSplits the initial set of file splits (not yet used)
     */
    public IndexOrderedSplitAssigner(Collection<FileSourceSplit> fileSourceSplits) {
    }

    /**
     * {@inheritDoc}
     *
     * <p>Not yet implemented; always returns {@link Optional#empty()}.
     *
     * @param hostname the host requesting a split, or {@code null} if not host-local
     * @return {@link Optional#empty()} always
     */
    @Override
    public Optional<FileSourceSplit> getNext(@Nullable String hostname) {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Not yet implemented; the supplied splits are ignored.
     *
     * @param splits the splits to add back for (re)assignment
     */
    @Override
    public void addSplits(Collection<FileSourceSplit> splits) {

    }

    /**
     * {@inheritDoc}
     *
     * <p>Not yet implemented; always returns {@code null}.
     *
     * @return {@code null} always
     */
    @Override
    public Collection<FileSourceSplit> remainingSplits() {
        return null;
    }
}
