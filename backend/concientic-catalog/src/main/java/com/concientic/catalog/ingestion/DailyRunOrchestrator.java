package com.concientic.catalog.ingestion;

import com.concientic.catalog.api.DailyRunResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DailyRunOrchestrator {
    private final String timezone;
    private final IngestionRunService ingestionRunService;

    public DailyRunOrchestrator(
            @Value("${concientic.timezone:America/Bogota}") String timezone,
            IngestionRunService ingestionRunService) {
        this.timezone = timezone;
        this.ingestionRunService = ingestionRunService;
    }

    DailyRunOrchestrator(String timezone) {
        this.timezone = timezone;
        this.ingestionRunService = null;
    }

    public DailyRunResponse startDryRun() {
        return response("NOT_WIRED", true, List.of("La ejecución de investigación todavía no se ha iniciado."), 0, 0, 0, 0);
    }

    public DailyRunResponse runResearchCycle() {
        if (ingestionRunService == null) {
            return startDryRun();
        }
        try {
            PersistedResearchRun persistedRun = ingestionRunService.execute();
            ResearchCycleResult result = persistedRun.cycle();
            List<String> warnings = new ArrayList<>(result.warnings());
            warnings.add("Resultados persistidos en PostgreSQL; ningún recurso fue publicado automáticamente.");
            return response(persistedRun.runId(), "PERSISTED_NOT_PUBLISHED", false, warnings, result.candidatesDiscovered(), result.candidatesVerified(), result.activeCandidates(), result.reviewRequired());
        } catch (RuntimeException exception) {
            return response(UUID.randomUUID(), "FAILED", false, List.of("Research cycle failed: " + safeMessage(exception)), 0, 0, 0, 0);
        }
    }

    private DailyRunResponse response(String status, boolean dryRun, List<String> warnings, int discovered, int verified, int active, int reviewRequired) {
        return response(UUID.randomUUID(), status, dryRun, warnings, discovered, verified, active, reviewRequired);
    }

    private DailyRunResponse response(UUID runId, String status, boolean dryRun, List<String> warnings, int discovered, int verified, int active, int reviewRequired) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
        return new DailyRunResponse(runId, status, now.toInstant(), timezone, dryRun, PipelineStages.ORDER, warnings, discovered, verified, active, reviewRequired);
    }

    private static String safeMessage(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
