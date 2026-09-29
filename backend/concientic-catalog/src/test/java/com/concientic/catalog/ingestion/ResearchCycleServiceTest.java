package com.concientic.catalog.ingestion;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResearchCycleServiceTest {

    @Test
    void keepsUnverifiedAndPaymentCandidatesOutOfActiveResults() {
        var activeCandidate = candidate("https://example.org/active");
        var paidCandidate = candidate("https://example.org/paid");
        SourceAdapter adapter = new SourceAdapter() {
            @Override
            public String sourceName() {
                return "fixture";
            }

            @Override
            public List<ResourceCandidate> discover() {
                return List.of(activeCandidate, paidCandidate);
            }
        };
        ResourceVerifier verifier = candidate -> new VerificationResult(
                candidate == activeCandidate ? AccessStatus.ACTIVE : AccessStatus.UNKNOWN,
                candidate.sourceUrl(), candidate.sourceUrl(), null, 200, 10,
                "Fixture", "Learning resource", candidate == activeCandidate, false,
                candidate == paidCandidate, java.time.Instant.now(), "fixture", null, null);

        var result = new ResearchCycleService(List.of(adapter), verifier).execute();

        assertThat(result.candidatesDiscovered()).isEqualTo(2);
        assertThat(result.candidatesVerified()).isEqualTo(2);
        assertThat(result.activeCandidates()).isEqualTo(1);
        assertThat(result.reviewRequired()).isEqualTo(1);
    }

    private static ResourceCandidate candidate(String url) {
        return new ResourceCandidate(null, "fixture", URI.create(url), "Fixture", "other", null, List.of());
    }
}
