package com.concientic.catalog.ingestion;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResearchCycleService {
    private final List<SourceAdapter> adapters;
    private final ResourceVerifier verifier;

    public ResearchCycleService(List<SourceAdapter> adapters, ResourceVerifier verifier) {
        this.adapters = adapters == null ? List.of() : List.copyOf(adapters);
        this.verifier = verifier;
    }

    public ResearchCycleResult execute() {
        List<ResearchObservation> observations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int candidatesDiscovered = 0;
        int activeCandidates = 0;
        int reviewRequired = 0;

        for (SourceAdapter adapter : adapters) {
            List<ResourceCandidate> candidates;
            try {
                candidates = adapter.discover();
            } catch (RuntimeException exception) {
                warnings.add(adapter.sourceName() + ": discovery failed: " + safeMessage(exception));
                continue;
            }
            candidatesDiscovered += candidates.size();
            for (ResourceCandidate candidate : candidates) {
                VerificationResult verification;
                try {
                    verification = verifier.verify(candidate);
                } catch (RuntimeException exception) {
                    warnings.add(candidate.sourceName() + ": verification failed: " + safeMessage(exception));
                    reviewRequired++;
                    continue;
                }
                observations.add(new ResearchObservation(candidate, verification));
                if (verification.isPublishableAccess()) {
                    activeCandidates++;
                } else {
                    reviewRequired++;
                }
            }
        }

        if (adapters.isEmpty()) {
            warnings.add("No source adapters are configured");
        }
        return new ResearchCycleResult(candidatesDiscovered, observations.size(), activeCandidates, reviewRequired, observations, warnings);
    }

    private static String safeMessage(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
