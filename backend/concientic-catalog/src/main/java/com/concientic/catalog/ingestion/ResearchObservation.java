package com.concientic.catalog.ingestion;

public record ResearchObservation(
        ResourceCandidate candidate,
        VerificationResult verification) {
}
