package com.concientic.catalog.ingestion;

import java.net.URI;
import java.util.List;

public record ConfiguredSourcePage(
        String sourceName,
        String provider,
        String providerType,
        String url,
        String expectedTitle,
        List<String> expectedLanguages,
        String freeStatusHint,
        String competencyHint,
        String guardianHint) {

    public ResourceCandidate toCandidate() {
        URI sourceUrl = URI.create(url);
        if (!"http".equalsIgnoreCase(sourceUrl.getScheme()) && !"https".equalsIgnoreCase(sourceUrl.getScheme())) {
            throw new IllegalArgumentException("Only HTTP(S) URLs are supported: " + url);
        }
        if (sourceName == null || sourceName.isBlank() || provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("sourceName and provider are required");
        }
        return new ResourceCandidate(null, sourceName.trim(), sourceUrl, provider.trim(),
                providerType == null || providerType.isBlank() ? "other" : providerType.trim(),
                blankToNull(expectedTitle), expectedLanguages, blankToNull(freeStatusHint),
                blankToNull(competencyHint), blankToNull(guardianHint));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
