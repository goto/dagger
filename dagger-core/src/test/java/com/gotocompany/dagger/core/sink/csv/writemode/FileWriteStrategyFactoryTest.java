package com.gotocompany.dagger.core.sink.csv.writemode;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FileWriteStrategyFactoryTest {

    @Test
    public void shouldReturnAppendStrategy() {
        assertTrue(FileWriteStrategyFactory.getWriteStrategy("APPEND") instanceof AppendWriteStrategy);
    }

    @Test
    public void shouldReturnOverwriteStrategy() {
        assertTrue(FileWriteStrategyFactory.getWriteStrategy("OVERWRITE") instanceof OverwriteWriteStrategy);
    }

    @Test
    public void shouldBeCaseInsensitiveAndTrimmed() {
        assertTrue(FileWriteStrategyFactory.getWriteStrategy("  overwrite ") instanceof OverwriteWriteStrategy);
        assertTrue(FileWriteStrategyFactory.getWriteStrategy("append") instanceof AppendWriteStrategy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForUnsupportedWriteMode() {
        FileWriteStrategyFactory.getWriteStrategy("UPSERT");
    }
}
