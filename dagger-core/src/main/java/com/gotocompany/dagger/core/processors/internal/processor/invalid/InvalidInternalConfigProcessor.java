package com.gotocompany.dagger.core.processors.internal.processor.invalid;

import com.gotocompany.dagger.core.exception.InvalidConfigurationException;
import com.gotocompany.dagger.core.processors.common.RowManager;
import com.gotocompany.dagger.core.processors.internal.InternalSourceConfig;
import com.gotocompany.dagger.core.processors.internal.processor.InternalConfigProcessor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Invalid internal config processor.
 */
public class InvalidInternalConfigProcessor implements InternalConfigProcessor, Serializable {

    /** The internal source configuration whose unsupported type triggered this fallback. */
    private InternalSourceConfig internalSourceConfig;

    /**
     * Instantiates a new Invalid internal config processor.
     *
     * @param internalSourceConfig the internal source config
     */
    public InvalidInternalConfigProcessor(InternalSourceConfig internalSourceConfig) {
        this.internalSourceConfig = internalSourceConfig;
    }

    /**
     * Always reports that no internal config type can be processed.
     *
     * @param type the configured internal source type (ignored)
     * @return {@code false} always
     */
    @Override
    public boolean canProcess(String type) {
        return false;
    }

    /**
     * Always fails because the configured internal source type is unsupported.
     *
     * @param rowManager the row manager wrapping the current record (unused)
     * @throws InvalidConfigurationException always, naming the unsupported type
     */
    public void process(RowManager rowManager) {
        String type = "";
        if (internalSourceConfig != null && StringUtils.isNotEmpty(internalSourceConfig.getType())) {
            type = internalSourceConfig.getType();
        }
        throw new InvalidConfigurationException(String.format("Invalid configuration, type '%s' for custom doesn't exists", type));
    }
}
