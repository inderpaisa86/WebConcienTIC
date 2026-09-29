package com.concientic.catalog.ingestion;

import java.util.List;

public record ResearchCycleResult(
        int candidatesDiscovered,
        int candidatesVerified,
        int activeCandidates,
        int reviewRequired,
        List<ResearchObservation> observations,
        List<String> warnings) {

    public ResearchCycleResult {
        observations = observations == null ? List.of() : List.copyOf(observations);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
