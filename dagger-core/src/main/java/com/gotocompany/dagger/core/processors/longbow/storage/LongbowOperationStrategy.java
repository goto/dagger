package com.gotocompany.dagger.core.processors.longbow.storage;

import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;
import org.threeten.bp.Duration;

import java.io.IOException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface LongbowOperationStrategy {

    boolean tableExists(String tableId) throws ExecutionException, InterruptedException;
    void createTable(Duration maxAgeDuration, String columnFamilyName, String tableId) throws ExecutionException, InterruptedException;
    CompletableFuture<Void> put(PutRequest putRequest);
    CompletableFuture<List<ScanResult>> scanAll(ScanRequest scanRequest);
    void close() throws IOException;

}
