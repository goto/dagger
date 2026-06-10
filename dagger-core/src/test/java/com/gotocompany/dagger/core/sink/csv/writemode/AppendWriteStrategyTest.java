package com.gotocompany.dagger.core.sink.csv.writemode;

import com.gotocompany.dagger.core.sink.csv.InMemoryFileStorageClient;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AppendWriteStrategyTest {

    private static final String PATH = "/tmp/job/output-09-Jun-2026.csv";
    private static final String HEADER = "service_type,booking_count";

    private AppendWriteStrategy strategy;
    private InMemoryFileStorageClient storageClient;

    @Before
    public void setup() {
        strategy = new AppendWriteStrategy();
        storageClient = new InMemoryFileStorageClient();
    }

    @Test
    public void shouldNotWriteWhenBufferIsEmpty() throws Exception {
        strategy.flush(storageClient, PATH, HEADER, Collections.emptyList());

        assertEquals(0, storageClient.getWriteCount());
        assertFalse(storageClient.exists(PATH));
    }

    @Test
    public void shouldWriteHeaderAndRowsWhenFileIsNew() throws Exception {
        strategy.flush(storageClient, PATH, HEADER, Arrays.asList("GO_RIDE,120", "GO_CAR,85"));

        assertEquals("service_type,booking_count\nGO_RIDE,120\nGO_CAR,85\n", storageClient.readAsString(PATH));
    }

    @Test
    public void shouldAppendRowsWithoutRepeatingHeaderWhenFileExists() throws Exception {
        strategy.flush(storageClient, PATH, HEADER, Collections.singletonList("GO_RIDE,120"));
        strategy.flush(storageClient, PATH, HEADER, Collections.singletonList("GO_CAR,85"));

        assertEquals("service_type,booking_count\nGO_RIDE,120\nGO_CAR,85\n", storageClient.readAsString(PATH));
        assertEquals(2, storageClient.getWriteCount());
    }

    @Test
    public void shouldWriteOnlyRowsWhenHeaderIsDisabled() throws Exception {
        strategy.flush(storageClient, PATH, null, Arrays.asList("GO_RIDE,120", "GO_CAR,85"));

        assertEquals("GO_RIDE,120\nGO_CAR,85\n", storageClient.readAsString(PATH));
    }
}
