package com.gotocompany.dagger.core.processors.longbow.storage;

import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.processors.longbow.enums.LongbowStorageType;
import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;
import com.gotocompany.dagger.core.utils.Constants;
import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A class that responsible to store the event to big table for longbow.
 */
public class LongbowStore {

    private final LongbowOperationStrategy longbowOperationStrategy;

    private LongbowStore(LongbowOperationStrategy longbowOperationStrategy) {
        this.longbowOperationStrategy = longbowOperationStrategy;
    }

    /**
     * Create longbow store.
     *
     * @param configuration the configuration
     * @return the longbow store
     * @throws IOException the io exception
     */
    public static LongbowStore create(Configuration configuration) throws IOException {
        if (LongbowStorageType.TABLESTORE.equals(LongbowStorageType.valueOf(configuration.getString(Constants.PROCESSOR_LONGBOW_STORAGE_TYPE)))) {
            return new LongbowStore(new TablestoreLongbowOperationStrategy(configuration));
        }
        return new LongbowStore(new BigTableLongbowOperationStrategy(configuration));
    }

    /**
     * Check if the table exists.
     *
     * @param tableId the table id
     * @return the boolean
     */
    public boolean tableExists(String tableId) throws ExecutionException, InterruptedException {
        return longbowOperationStrategy.tableExists(tableId);
    }

    /**
     * Create the table.
     *
     * @param maxAgeDuration   the max age duration
     * @param columnFamilyName the column family name
     * @param tableId          the table id
     * @throws Exception the exception
     */
    public void createTable(Duration maxAgeDuration, String columnFamilyName, String tableId) throws Exception {
        longbowOperationStrategy.createTable(maxAgeDuration, columnFamilyName, tableId);
    }

    /**
     * Put completable future.
     *
     * @param putRequest the put request
     * @return the completable future
     */
    public CompletableFuture<Void> put(PutRequest putRequest) {
        return longbowOperationStrategy.put(putRequest);
    }

    /**
     * Scan all completable future.
     *
     * @param scanRequest the scan request
     * @return the completable future
     */
    public CompletableFuture<List<ScanResult>> scanAll(ScanRequest scanRequest) {
        return longbowOperationStrategy.scanAll(scanRequest);
    }

    /**
     * Close the client.
     *
     * @throws IOException the io exception
     */
    public void close() throws IOException {
        if (longbowOperationStrategy != null) {
            longbowOperationStrategy.close();
        }
    }

}
