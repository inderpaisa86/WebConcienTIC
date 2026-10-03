package com.concientic.catalog.api;

import com.concientic.catalog.ingestion.CatalogHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/internal/runs")
public class InternalReportController {
    private final CatalogHistoryRepository historyRepository;
    private final String configuredToken;

    public InternalReportController(
            CatalogHistoryRepository historyRepository,
            @Value("${concientic.ingestion.token:}") String configuredToken) {
        this.historyRepository = historyRepository;
        this.configuredToken = configuredToken;
    }

    @GetMapping("/{runId}/report")
    public ResponseEntity<String> report(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable UUID runId) {
        authorize(authorization);
        try {
            String report = historyRepository.findReport(runId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(report);
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Daily report not found");
        }
    }

    private void authorize(String authorization) {
        if (configuredToken.isBlank() || authorization == null || !authorization.equals("Bearer " + configuredToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid ingestion token");
        }
    }
}
