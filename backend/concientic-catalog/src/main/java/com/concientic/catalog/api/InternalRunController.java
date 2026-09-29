package com.concientic.catalog.api;

import com.concientic.catalog.ingestion.DailyRunOrchestrator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/runs")
public class InternalRunController {
    private final DailyRunOrchestrator orchestrator;
    private final String configuredToken;

    public InternalRunController(DailyRunOrchestrator orchestrator, @Value("${concientic.ingestion.token:}") String configuredToken) {
        this.orchestrator = orchestrator;
        this.configuredToken = configuredToken;
    }

    @PostMapping("/daily")
    public DailyRunResponse run(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (configuredToken.isBlank() || authorization == null || !authorization.equals("Bearer " + configuredToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid ingestion token");
        }
        return orchestrator.runResearchCycle();
    }
}
