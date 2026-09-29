package com.concientic.catalog.ingestion;

import java.util.UUID;

public interface ResourceObservationStore {
    PersistedCounts persist(UUID runId, ResearchCycleResult result);

    record PersistedCounts(int resourcesPersisted, int checksPersisted, int reviewItemsCreated) {
    }
}
