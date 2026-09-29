package com.concientic.catalog.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResearchCycleService {
    private static final Logger log = LoggerFactory.getLogger(ResearchCycleService.class);

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

        log.info("Starting research cycle adapters={}", adapters.size());
        for (SourceAdapter adapter : adapters) {
            List<ResourceCandidate> candidates;
            try {
                candidates = adapter.discover();
                log.info("Source discovery completed source={} candidates={}", adapter.sourceName(), candidates.size());
            } catch (RuntimeException exception) {
                log.warn("Source discovery failed source={} message={}", adapter.sourceName(), safeMessage(exception), exception);
                warnings.add(adapter.sourceName() + ": discovery failed: " + safeMessage(exception));
                continue;
            }
            candidatesDiscovered += candidates.size();
            for (ResourceCandidate candidate : candidates) {
                VerificationResult verification;
                try {
                    verification = verifier.verify(candidate);
                    log.debug("Candidate verification completed source={} accessStatus={} publishable={}",
                            candidate.sourceName(), verification.accessStatus(), verification.isPublishableAccess());
                } catch (RuntimeException exception) {
                    log.warn("Candidate verification failed source={} message={}", candidate.sourceName(), safeMessage(exception), exception);
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
            log.warn("Research cycle has no source adapters configured");
        }
        log.info("Research cycle summary candidatesDiscovered={} candidatesVerified={} activeCandidates={} reviewRequired={} warnings={}",
                candidatesDiscovered, observations.size(), activeCandidates, reviewRequired, warnings.size());
        return new ResearchCycleResult(candidatesDiscovered, observations.size(), activeCandidates, reviewRequired, observations, warnings);
    }

    private static String safeMessage(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
