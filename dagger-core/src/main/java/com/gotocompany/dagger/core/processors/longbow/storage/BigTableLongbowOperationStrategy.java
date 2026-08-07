package com.gotocompany.dagger.core.processors.longbow.storage;

import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;
import com.gotocompany.dagger.core.processors.longbow.model.adapters.HBaseResultToScanResultAdapter;
import com.gotocompany.dagger.core.processors.longbow.model.adapters.ScanResultAdapter;
import com.gotocompany.dagger.core.utils.Constants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AdvancedScanResultConsumer;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.BigtableAsyncConnection;
import org.apache.hadoop.hbase.client.Result;
import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.google.cloud.bigtable.admin.v2.models.GCRules.GCRULES;

public class BigTableLongbowOperationStrategy implements LongbowOperationStrategy {

    private final BigtableTableAdminClient adminClient;
    private final BigtableAsyncConnection tableClient;
    private final Map<String, AsyncTable<AdvancedScanResultConsumer>> tables;
    private final ScanResultAdapter<Result> scanResultAdapter;

    public BigTableLongbowOperationStrategy(Configuration configuration) throws IOException {
        String gcpProjectID = configuration.getString(Constants.PROCESSOR_LONGBOW_GCP_PROJECT_ID_KEY, Constants.PROCESSOR_LONGBOW_GCP_PROJECT_ID_DEFAULT);
        String gcpInstanceID = configuration.getString(Constants.PROCESSOR_LONGBOW_GCP_INSTANCE_ID_KEY, Constants.PROCESSOR_LONGBOW_GCP_INSTANCE_ID_DEFAULT);
        org.apache.hadoop.conf.Configuration bigTableConfiguration = BigtableConfiguration.configure(gcpProjectID, gcpInstanceID);
        this.adminClient = BigtableTableAdminClient.create(gcpProjectID, gcpInstanceID);
        this.tableClient = new BigtableAsyncConnection(bigTableConfiguration);
        this.tables = new HashMap<>();
        this.scanResultAdapter = new HBaseResultToScanResultAdapter();
    }

    @Override
    public boolean tableExists(String tableId) {
        return adminClient.exists(tableId);
    }

    @Override
    public void createTable(Duration maxAgeDuration, String columnFamilyName, String tableId) {
        adminClient.createTable(CreateTableRequest.of(tableId).addFamily(columnFamilyName,
                GCRULES.union()
                        .rule(GCRULES.maxVersions(1))
                        .rule(GCRULES.maxAge(maxAgeDuration))));
    }

    @Override
    public CompletableFuture<Void> put(PutRequest putRequest) {
        return getTable(putRequest.getTableId()).put(putRequest.get());
    }

    @Override
    public CompletableFuture<List<ScanResult>> scanAll(ScanRequest scanRequest) {
        return getTable(scanRequest.getTableId())
                .scanAll(scanRequest.get())
                .thenApply(results -> results.stream()
                        .map(this.scanResultAdapter::adapt)
                        .collect(Collectors.toList()));
    }

    @Override
    public void close() throws IOException {
        if (tableClient != null) {
            tableClient.close();
        }
        if (adminClient != null) {
            adminClient.close();
        }
    }

    private AsyncTable<AdvancedScanResultConsumer> getTable(String tableId) {
        if (!tables.containsKey(tableId)) {
            tables.put(tableId, tableClient.getTable(TableName.valueOf(tableId)));
        }
        return tables.get(tableId);
    }

}
