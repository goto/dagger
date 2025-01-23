package com.gotocompany.dagger.functions.udfs.scalar.dart.store;

import com.gotocompany.dagger.common.metrics.managers.GaugeStatsManager;
import com.gotocompany.dagger.functions.exceptions.BucketDoesNotExistException;
import com.gotocompany.dagger.functions.exceptions.TagDoesNotExistException;

public interface DartDataStoreClient {

    /**
     * If a client could provide implementation to this, use the default data store, else implement DartDataStore along with client implementation.
     *
     * @param udfName           either "DartGet" or "DartContains"
     * @param gaugeStatsManager an instrumentation provider
     * @param bucketName        name of the object storage service bucket
     * @param dartName          from the bucket, this would be either dart-get/path/to/file.json or dart-contains/path/to/file.json
     * @return Content of the file in String format from abc://bucket-name/dart-(get/contains)/path/to/file.json
     * @throws TagDoesNotExistException    if tag doesn't exist
     * @throws BucketDoesNotExistException if bucket doesn't exist
     */
    String fetchJsonData(String udfName, GaugeStatsManager gaugeStatsManager, String bucketName, String dartName)
            throws TagDoesNotExistException, BucketDoesNotExistException;
}
