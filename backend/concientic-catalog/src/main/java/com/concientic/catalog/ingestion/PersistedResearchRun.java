package com.concientic.catalog.ingestion;

import java.util.UUID;

public record PersistedResearchRun(
        UUID runId,
        ResearchCycleResult cycle,
        ResourceObservationStore.PersistedCounts persistedCounts) {
}
