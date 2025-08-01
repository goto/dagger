package com.gotocompany.dagger.core.processors.longbow.storage;

import com.alicloud.openservices.tablestore.AsyncClient;
import com.alicloud.openservices.tablestore.TableStoreCallback;
import com.alicloud.openservices.tablestore.model.CreateTableRequest;
import com.alicloud.openservices.tablestore.model.GetRangeRequest;
import com.alicloud.openservices.tablestore.model.GetRangeResponse;
import com.alicloud.openservices.tablestore.model.ListTableResponse;
import com.alicloud.openservices.tablestore.model.PrimaryKeyBuilder;
import com.alicloud.openservices.tablestore.model.PrimaryKeySchema;
import com.alicloud.openservices.tablestore.model.PrimaryKeyType;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.PutRowRequest;
import com.alicloud.openservices.tablestore.model.PutRowResponse;
import com.alicloud.openservices.tablestore.model.RangeRowQueryCriteria;
import com.alicloud.openservices.tablestore.model.Request;
import com.alicloud.openservices.tablestore.model.Response;
import com.alicloud.openservices.tablestore.model.Row;
import com.alicloud.openservices.tablestore.model.TableMeta;
import com.alicloud.openservices.tablestore.model.TableOptions;
import com.gotocompany.dagger.common.configuration.Configuration;
import com.gotocompany.dagger.core.processors.longbow.storage.adapters.HBasePutToTablestoreRequestAdapter;
import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;
import com.gotocompany.dagger.core.processors.longbow.model.adapters.ScanResultAdapter;
import com.gotocompany.dagger.core.processors.longbow.model.adapters.TablestoreRowToScanResultAdapter;
import com.gotocompany.dagger.core.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TablestoreLongbowOperationStrategy implements LongbowOperationStrategy {

    private static final Logger log = LoggerFactory.getLogger(TablestoreLongbowOperationStrategy.class);
    private final AsyncClient asyncClient;
    private final String primaryKeyName;
    private final HBasePutToTablestoreRequestAdapter putRequestAdapter;
    private final ScanResultAdapter<Row> rowToScanResultAdapter;

    public TablestoreLongbowOperationStrategy(Configuration configuration) {
        this.asyncClient = new AsyncClient(
                configuration.getString(Constants.PROCESSOR_LONGBOW_TABLESTORE_CLIENT_ENDPOINT),
                configuration.getString(Constants.PROCESSOR_LONGBOW_TABLESTORE_CLIENT_ACCESS_KEY_ID),
                configuration.getString(Constants.PROCESSOR_LONGBOW_TABLESTORE_CLIENT_ACCESS_KEY_SECRET),
                configuration.getString(Constants.PROCESSOR_LONGBOW_TABLESTORE_CLIENT_INSTANCE_NAME)
        );
        this.primaryKeyName = configuration.getString(Constants.PROCESSOR_LONGBOW_TABLESTORE_PRIMARY_KEY_NAME);
        this.putRequestAdapter = new HBasePutToTablestoreRequestAdapter(
                this.primaryKeyName,
                configuration.getString(Constants.PROCESSOR_LONGBOW_TABLESTORE_TABLE_ID)
        );
        this.rowToScanResultAdapter = new TablestoreRowToScanResultAdapter(Constants.LONGBOW_COLUMN_FAMILY_DEFAULT);
    }

    @Override
    public boolean tableExists(String tableId) throws ExecutionException, InterruptedException {
        ListTableResponse listTableResponse = asyncClient.listTable(new NoOpTablestoreCallback<>()).get();
        return listTableResponse.getTableNames().contains(tableId);
    }

    @Override
    public void createTable(Duration maxAgeDuration, String columnFamilyName, String tableId) throws ExecutionException, InterruptedException {
        TableMeta tableMeta = new TableMeta(tableId);
        tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema(this.primaryKeyName, PrimaryKeyType.STRING));
        TableOptions tableOptions = new TableOptions(maxAgeDuration.toSecondsPart(), 1);
        CreateTableRequest createTableRequest = new CreateTableRequest(
                tableMeta,
                tableOptions
        );
        asyncClient.createTable(createTableRequest, new NoOpTablestoreCallback<>()).get();
    }

    @Override
    public CompletableFuture<Void> put(PutRequest putRequest) {
        PutRowRequest putRowRequest = putRequestAdapter.adapt(putRequest.get());
        CompletableFuture<Void> future = new CompletableFuture<>();
        asyncClient.putRow(putRowRequest, new TableStoreCallback<PutRowRequest, PutRowResponse>() {
            @Override
            public void onCompleted(PutRowRequest request, PutRowResponse result) {
                future.complete(null);
            }

            @Override
            public void onFailed(PutRowRequest request, Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<List<ScanResult>> scanAll(ScanRequest scanRequest) {
        RangeRowQueryCriteria rangeRowQueryCriteria = new RangeRowQueryCriteria(scanRequest.getTableId());
        PrimaryKeyBuilder startPrimaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        startPrimaryKeyBuilder.addPrimaryKeyColumn(this.primaryKeyName, PrimaryKeyValue.fromBinary(scanRequest.get().getStartRow()));
        PrimaryKeyBuilder stopPrimaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        stopPrimaryKeyBuilder.addPrimaryKeyColumn(this.primaryKeyName, PrimaryKeyValue.fromBinary(scanRequest.get().getStopRow()));
        rangeRowQueryCriteria.setInclusiveStartPrimaryKey(startPrimaryKeyBuilder.build());
        rangeRowQueryCriteria.setExclusiveEndPrimaryKey(stopPrimaryKeyBuilder.build());
        rangeRowQueryCriteria.setMaxVersions(1);
        CompletableFuture<List<ScanResult>> future = new CompletableFuture<>();
        asyncClient.getRange(new GetRangeRequest(rangeRowQueryCriteria), new TableStoreCallback<GetRangeRequest, GetRangeResponse>() {
            @Override
            public void onCompleted(GetRangeRequest getRangeRequest, GetRangeResponse getRangeResponse) {
                future.complete(getRangeResponse.getRows()
                        .stream()
                        .map(rowToScanResultAdapter::adapt)
                        .collect(Collectors.toList()));
            }

            @Override
            public void onFailed(GetRangeRequest getRangeRequest, Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public void close() throws IOException {
        if (asyncClient != null) {
            try {
                asyncClient.shutdown();
            } catch (Exception e) {
                throw new IOException("Failed to close Tablestore client", e);
            }
        }
    }

    private static class NoOpTablestoreCallback<T extends Request, V extends Response> implements TableStoreCallback<T, V> {
        @Override
        public void onCompleted(T t, V v) {

        }

        @Override
        public void onFailed(T t, Exception e) {
            log.error("Tablestore operation failed", e);
        }
    }
}
