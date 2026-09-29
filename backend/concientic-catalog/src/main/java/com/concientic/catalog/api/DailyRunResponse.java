package com.concientic.catalog.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DailyRunResponse(
        UUID runId,
        String status,
        Instant startedAt,
        String timezone,
        boolean dryRun,
        List<String> stages,
        List<String> warnings,
        int candidatesDiscovered,
        int candidatesVerified,
        int activeCandidates,
        int reviewRequired) {
}
