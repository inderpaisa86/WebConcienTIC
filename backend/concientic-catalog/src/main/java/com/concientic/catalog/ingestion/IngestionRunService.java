package com.concientic.catalog.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class IngestionRunService {
    private final ResearchCycleService researchCycleService;
    private final IngestionRunStore runStore;
    private final ResourceObservationStore observationStore;
    private final String timezone;

    public IngestionRunService(
            ResearchCycleService researchCycleService,
            IngestionRunStore runStore,
            ResourceObservationStore observationStore,
            @Value("${concientic.timezone:America/Bogota}") String timezone) {
        this.researchCycleService = researchCycleService;
        this.runStore = runStore;
        this.observationStore = observationStore;
        this.timezone = timezone;
    }

    public PersistedResearchRun execute() {
        UUID runId = UUID.randomUUID();
        runStore.start(runId, Instant.now(), timezone);
        try {
            ResearchCycleResult cycle = researchCycleService.execute();
            ResourceObservationStore.PersistedCounts persisted = observationStore.persist(runId, cycle);
            String status = cycle.warnings().isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS";
            runStore.finish(runId, status, cycle, null);
            return new PersistedResearchRun(runId, cycle, persisted);
        } catch (RuntimeException exception) {
            runStore.finish(runId, "FAILED", new ResearchCycleResult(0, 0, 0, 0, java.util.List.of(), java.util.List.of()), exception.getMessage());
            throw exception;
        }
    }
}
