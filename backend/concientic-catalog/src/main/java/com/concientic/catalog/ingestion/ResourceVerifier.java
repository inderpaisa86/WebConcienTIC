package com.concientic.catalog.ingestion;

public interface ResourceVerifier {
    VerificationResult verify(ResourceCandidate candidate);
}
