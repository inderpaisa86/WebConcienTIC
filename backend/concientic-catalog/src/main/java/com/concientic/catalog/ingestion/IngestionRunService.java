package com.concientic.catalog.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class IngestionRunService {
    private static final Logger log = LoggerFactory.getLogger(IngestionRunService.class);

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
        boolean runStarted = false;
        try {
            log.info("Starting ingestion run runId={} timezone={}", runId, timezone);
            runStore.start(runId, Instant.now(), timezone);
            runStarted = true;
            ResearchCycleResult cycle = researchCycleService.execute();
            log.info("Research cycle completed runId={} candidatesDiscovered={} candidatesVerified={} activeCandidates={} reviewRequired={} warnings={}",
                    runId, cycle.candidatesDiscovered(), cycle.candidatesVerified(), cycle.activeCandidates(), cycle.reviewRequired(), cycle.warnings().size());
            ResourceObservationStore.PersistedCounts persisted = observationStore.persist(runId, cycle);
            log.info("Research observations persisted runId={} resourcesPersisted={} checksPersisted={} reviewItemsCreated={}",
                    runId, persisted.resourcesPersisted(), persisted.checksPersisted(), persisted.reviewItemsCreated());
            String status = cycle.warnings().isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS";
            runStore.finish(runId, status, cycle, null);
            log.info("Ingestion run finished runId={} status={}", runId, status);
            return new PersistedResearchRun(runId, cycle, persisted);
        } catch (RuntimeException exception) {
            log.error("Ingestion run failed runId={} message={}", runId, exception.getMessage(), exception);
            if (runStarted) {
                try {
                    runStore.finish(runId, "FAILED", new ResearchCycleResult(0, 0, 0, 0, java.util.List.of(), java.util.List.of()), exception.getMessage());
                } catch (RuntimeException finishException) {
                    log.error("Could not mark ingestion run as FAILED runId={} message={}", runId, finishException.getMessage(), finishException);
                }
            }
            throw exception;
        }
    }
}
