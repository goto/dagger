package com.gotocompany.dagger.core.processors.longbow.model.adapters;

import com.gotocompany.dagger.core.processors.longbow.model.ScanResult;

public interface ScanResultAdapter<T> {
    ScanResult adapt(T result);
}
