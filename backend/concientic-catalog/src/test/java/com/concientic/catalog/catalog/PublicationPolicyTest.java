package com.concientic.catalog.catalog;

import com.concientic.catalog.domain.FreeStatus;
import com.concientic.catalog.domain.ResourceStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationPolicyTest {

    @Test
    void acceptsFreeContentWhenEvidenceAndActiveStatusExist() {
        var decision = PublicationPolicy.evaluate(
                ResourceStatus.ACTIVE,
                FreeStatus.FREE_CONTENT_PAID_CERTIFICATE,
                true);

        assertThat(decision.publishable()).isTrue();
    }

    @Test
    void acceptsAValidRedirectWhileKeepingItsLinkChangedStatus() {
        var decision = PublicationPolicy.evaluate(ResourceStatus.LINK_CHANGED, FreeStatus.FREE, true);

        assertThat(decision.publishable()).isTrue();
    }

    @Test
    void rejectsTrialAndPaidContentFromAutomaticPublication() {
        assertThat(PublicationPolicy.evaluate(ResourceStatus.ACTIVE, FreeStatus.TRIAL, true).publishable()).isFalse();
        assertThat(PublicationPolicy.evaluate(ResourceStatus.ACTIVE, FreeStatus.PAID, true).publishable()).isFalse();
    }

    @Test
    void sendsUnknownOrUnverifiedResourcesToReview() {
        var withoutEvidence = PublicationPolicy.evaluate(ResourceStatus.ACTIVE, FreeStatus.FREE, false);
        var unknownAccess = PublicationPolicy.evaluate(ResourceStatus.ACTIVE, FreeStatus.UNKNOWN, true);
        var removed = PublicationPolicy.evaluate(ResourceStatus.REMOVED, FreeStatus.FREE, true);

        assertThat(withoutEvidence.explanation()).startsWith("REVIEW_REQUIRED");
        assertThat(unknownAccess.publishable()).isFalse();
        assertThat(removed.publishable()).isFalse();
    }
}
