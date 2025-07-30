package com.gotocompany.dagger.core.processors.longbow.storage.adapters;

import com.alicloud.openservices.tablestore.model.Request;

public interface TablestoreRequestAdapter<T, R extends Request> {
    R adapt(T request);
}
