package com.concientic.catalog.ingestion;

import java.time.Instant;
import java.util.UUID;

public interface IngestionRunStore {
    void start(UUID runId, Instant startedAt, String timezone);

    void finish(UUID runId, String status, ResearchCycleResult result, String errorMessage);
}
