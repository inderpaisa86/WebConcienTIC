package com.concientic.catalog.ingestion;

import java.util.UUID;

public interface ResourceObservationStore {
    PersistedCounts persist(UUID runId, ResearchCycleResult result);

    record PersistedCounts(int resourcesPersisted, int checksPersisted, int reviewItemsCreated,
                           int changesDetected, int duplicatesDetected) {
        public PersistedCounts(int resourcesPersisted, int checksPersisted, int reviewItemsCreated) {
            this(resourcesPersisted, checksPersisted, reviewItemsCreated, 0, 0);
        }
    }
}
