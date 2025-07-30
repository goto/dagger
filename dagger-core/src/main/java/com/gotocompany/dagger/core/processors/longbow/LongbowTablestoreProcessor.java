package com.gotocompany.dagger.core.processors.longbow;

import com.gotocompany.dagger.common.core.StreamInfo;
import com.gotocompany.dagger.core.processors.PostProcessorConfig;
import com.gotocompany.dagger.core.processors.types.PostProcessor;

public class LongbowTablestoreProcessor implements PostProcessor  {

    @Override
    public StreamInfo process(StreamInfo streamInfo) {
        return null;
    }

    @Override
    public boolean canProcess(PostProcessorConfig processorConfig) {
        return false;
    }
}
