package com.concientic.catalog.ingestion;

import com.concientic.catalog.api.DailyRunResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
            String status = result.activeCandidates() > 0 ? "SUCCESS" : "PERSISTED_NOT_PUBLISHED";
            if (result.activeCandidates() > 0) {
                warnings.add("Recursos verificables publicados automáticamente; los casos no verificables quedaron fuera del catálogo.");
            } else {
                warnings.add("Resultados persistidos en PostgreSQL; ningún recurso cumplió las condiciones de publicación automática.");
            }
            return response(persistedRun.runId(), status, false, warnings, result.candidatesDiscovered(), result.candidatesVerified(), result.activeCandidates(), result.reviewRequired());
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
