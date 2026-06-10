package com.gotocompany.dagger.core.sink.csv.writemode;

import com.gotocompany.dagger.core.utils.Constants;

/**
 * Resolves the configured {@code SINK_CSV_WRITE_MODE} into a {@link FileWriteStrategy}. Extensible:
 * a future UPSERT (merge-by-key) mode can be added here without touching the writer.
 */
public class FileWriteStrategyFactory {

    private FileWriteStrategyFactory() {
    }

    public static FileWriteStrategy getWriteStrategy(String writeMode) {
        String normalizedWriteMode = writeMode == null ? "" : writeMode.trim().toUpperCase();
        switch (normalizedWriteMode) {
            case Constants.SINK_CSV_WRITE_MODE_APPEND:
                return new AppendWriteStrategy();
            case Constants.SINK_CSV_WRITE_MODE_OVERWRITE:
                return new OverwriteWriteStrategy();
            default:
                throw new IllegalArgumentException("Unsupported CSV sink write mode: '" + writeMode
                        + "'. Supported values: " + Constants.SINK_CSV_WRITE_MODE_APPEND + ", " + Constants.SINK_CSV_WRITE_MODE_OVERWRITE);
        }
    }
}
