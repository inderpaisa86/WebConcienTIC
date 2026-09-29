package com.concientic.catalog.ingestion;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public record ResourceCandidate(
        UUID candidateId,
        String sourceName,
        URI sourceUrl,
        String provider,
        String providerType,
        String expectedTitle,
        List<String> expectedLanguages) {

    public ResourceCandidate {
        if (candidateId == null) {
            candidateId = UUID.randomUUID();
        }
        expectedLanguages = expectedLanguages == null ? List.of() : List.copyOf(expectedLanguages);
    }
}
