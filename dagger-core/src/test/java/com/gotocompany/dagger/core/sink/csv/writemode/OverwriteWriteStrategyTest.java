package com.gotocompany.dagger.core.sink.csv.writemode;

import com.gotocompany.dagger.core.sink.csv.InMemoryFileStorageClient;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class OverwriteWriteStrategyTest {

    private static final String PATH = "/tmp/job/output-09-Jun-2026.csv";
    private static final String HEADER = "service_type,booking_count";

    private OverwriteWriteStrategy strategy;
    private InMemoryFileStorageClient storageClient;

    @Before
    public void setup() {
        strategy = new OverwriteWriteStrategy();
        storageClient = new InMemoryFileStorageClient();
    }

    @Test
    public void shouldNotWriteWhenBufferIsEmpty() throws Exception {
        strategy.flush(storageClient, PATH, HEADER, Collections.emptyList());

        assertEquals(0, storageClient.getWriteCount());
        assertFalse(storageClient.exists(PATH));
    }

    @Test
    public void shouldNotBlankExistingFileOnEmptyBuffer() throws Exception {
        strategy.flush(storageClient, PATH, HEADER, Collections.singletonList("GO_RIDE,120"));
        strategy.flush(storageClient, PATH, HEADER, Collections.emptyList());

        assertEquals("service_type,booking_count\nGO_RIDE,120\n", storageClient.readAsString(PATH));
        assertEquals(1, storageClient.getWriteCount());
    }

    @Test
    public void shouldReplaceFileContentWithLatestBuffer() throws Exception {
        strategy.flush(storageClient, PATH, HEADER, Arrays.asList("GO_RIDE,120", "GO_CAR,85"));
        strategy.flush(storageClient, PATH, HEADER, Arrays.asList("GO_RIDE,95", "GO_CAR,70"));

        assertEquals("service_type,booking_count\nGO_RIDE,95\nGO_CAR,70\n", storageClient.readAsString(PATH));
        assertEquals(2, storageClient.getWriteCount());
    }

    @Test
    public void shouldWriteOnlyRowsWhenHeaderIsDisabled() throws Exception {
        strategy.flush(storageClient, PATH, null, Collections.singletonList("GO_RIDE,120"));

        assertEquals("GO_RIDE,120\n", storageClient.readAsString(PATH));
    }
}
