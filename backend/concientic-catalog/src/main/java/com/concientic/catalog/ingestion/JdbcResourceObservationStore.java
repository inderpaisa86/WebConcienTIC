package com.concientic.catalog.ingestion;

import com.concientic.catalog.catalog.PublicationPolicy;
import com.concientic.catalog.domain.FreeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.concientic.catalog.domain.ResourceStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class JdbcResourceObservationStore implements ResourceObservationStore {
    private static final Logger log = LoggerFactory.getLogger(JdbcResourceObservationStore.class);

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcResourceObservationStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PersistedCounts persist(UUID runId, ResearchCycleResult result) {
        log.info("Persisting research observations runId={} observations={}", runId, result.observations().size());
        int resources = 0;
        int checks = 0;
        int reviews = 0;
        for (ResearchObservation observation : result.observations()) {
            ResourceCandidate candidate = observation.candidate();
            VerificationResult verification = observation.verification();
            boolean titleMissing = verification.title() == null || verification.title().isBlank();
            String title = titleMissing ? fallbackTitle(candidate) : verification.title();
            if (titleMissing) {
                log.warn("Verified title missing; persisting provisional title runId={} source={} provisionalTitle={}",
                        runId, candidate.sourceName(), title);
            }

            UUID resourceId = upsertResource(candidate, verification, title);
            persistCheck(resourceId, runId, verification);
            resources++;
            checks++;

            ResourceStatus resourceStatus = mapStatus(verification.accessStatus());
            FreeStatus freeStatus = verification.requiresPayment() ? FreeStatus.PAID : FreeStatus.UNKNOWN;
            PublicationPolicy.Decision decision = PublicationPolicy.evaluate(resourceStatus, freeStatus, true);
            log.info("Resource upserted runId={} resourceId={} source={} accessStatus={} resourceStatus={} freeStatus={} publishable={} titleFallback={}",
                    runId, resourceId, candidate.sourceName(), verification.accessStatus(), resourceStatus, freeStatus, decision.publishable(), titleMissing);
            if (titleMissing) {
                createReviewItem(resourceId, candidate, "MISSING_VERIFIED_TITLE", verification, "Confirm resource identity and replace provisional title");
                reviews++;
            } else if (!decision.publishable()) {
                createReviewItem(resourceId, candidate, decision.explanation(), verification, "Verify classification and free access before publication");
                reviews++;
            }
        }
        log.info("Research observations persistence finished runId={} resourcesPersisted={} checksPersisted={} reviewItemsCreated={}", runId, resources, checks, reviews);
        return new PersistedCounts(resources, checks, reviews);
    }

    private UUID upsertResource(ResourceCandidate candidate, VerificationResult verification, String title) {
        UUID stableId = UUID.nameUUIDFromBytes(candidate.sourceUrl().toString().getBytes(StandardCharsets.UTF_8));
        ResourceStatus status = mapStatus(verification.accessStatus());
        FreeStatus freeStatus = verification.requiresPayment() ? FreeStatus.PAID : FreeStatus.UNKNOWN;
        String description = verification.descriptionExcerpt() == null || verification.descriptionExcerpt().isBlank()
                ? "[Sin descripción verificada]"
                : verification.descriptionExcerpt();
        String verifiedUrl = verification.finalUrl() == null ? candidate.sourceUrl().toString() : verification.finalUrl().toString();
        String canonicalUrl = verification.canonicalUrl() == null ? verifiedUrl : verification.canonicalUrl().toString();
        String reason = verification.isPublishableAccess()
                ? "Acceso HTTP y semántica verificados; clasificación pendiente"
                : "Requiere revisión: " + verification.evidenceSummary();

        String sql = """
                INSERT INTO resources (
                    id, title, short_description, provider, provider_type, source_url,
                    canonical_url, verified_url, url_status, http_status, last_verified_at,
                    free_status, free_explanation, format, verification_score, overall_score,
                    reason_for_inclusion, status, source_last_checked, notes)
                VALUES (
                    :id, :title, :description, :provider, :providerType, :sourceUrl,
                    :canonicalUrl, :verifiedUrl, :urlStatus, :httpStatus, :lastVerifiedAt,
                    :freeStatus, :freeExplanation, 'other', :verificationScore, :verificationScore,
                    :reason, :status, :lastVerifiedAt, :notes)
                ON CONFLICT (source_url) DO UPDATE SET
                    title = EXCLUDED.title,
                    short_description = EXCLUDED.short_description,
                    provider = EXCLUDED.provider,
                    provider_type = EXCLUDED.provider_type,
                    canonical_url = EXCLUDED.canonical_url,
                    verified_url = EXCLUDED.verified_url,
                    url_status = EXCLUDED.url_status,
                    http_status = EXCLUDED.http_status,
                    last_verified_at = EXCLUDED.last_verified_at,
                    free_status = EXCLUDED.free_status,
                    free_explanation = EXCLUDED.free_explanation,
                    verification_score = EXCLUDED.verification_score,
                    overall_score = EXCLUDED.overall_score,
                    reason_for_inclusion = EXCLUDED.reason_for_inclusion,
                    status = EXCLUDED.status,
                    last_updated_at = CURRENT_TIMESTAMP,
                    source_last_checked = EXCLUDED.source_last_checked,
                    notes = EXCLUDED.notes
                RETURNING id
                """;
        UUID persistedId = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", stableId)
                .addValue("title", title)
                .addValue("description", description)
                .addValue("provider", candidate.provider())
                .addValue("providerType", candidate.providerType())
                .addValue("sourceUrl", candidate.sourceUrl().toString())
                .addValue("canonicalUrl", canonicalUrl)
                .addValue("verifiedUrl", verifiedUrl)
                .addValue("urlStatus", verification.accessStatus().name())
                .addValue("httpStatus", verification.httpStatus())
                .addValue("lastVerifiedAt", timestamp(verification.checkedAt()))
                .addValue("freeStatus", freeStatus.name())
                .addValue("freeExplanation", freeStatus == FreeStatus.PAID ? "La página presenta señales de pago." : "La gratuidad aún no ha sido verificada.")
                .addValue("verificationScore", verification.semanticMatch() ? 1.0 : 0.0)
                .addValue("reason", reason)
                .addValue("status", status.name())
                .addValue("notes", verification.errorMessage()), UUID.class);
        return persistedId == null ? stableId : persistedId;
    }

    private void persistCheck(UUID resourceId, UUID runId, VerificationResult verification) {
        jdbc.update("""
                INSERT INTO resource_checks (
                    id, resource_id, ingestion_run_id, requested_url, final_url, canonical_url,
                    http_status, response_time_ms, access_status, requires_account,
                    requires_payment, checked_at, error_code, error_message)
                VALUES (
                    :id, :resourceId, :runId, :requestedUrl, :finalUrl, :canonicalUrl,
                    :httpStatus, :responseTimeMs, :accessStatus, :requiresAccount,
                    :requiresPayment, :checkedAt, :errorCode, :errorMessage)
                """, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("resourceId", resourceId)
                .addValue("runId", runId)
                .addValue("requestedUrl", verification.requestedUrl().toString())
                .addValue("finalUrl", verification.finalUrl() == null ? null : verification.finalUrl().toString())
                .addValue("canonicalUrl", verification.canonicalUrl() == null ? null : verification.canonicalUrl().toString())
                .addValue("httpStatus", verification.httpStatus())
                .addValue("responseTimeMs", verification.responseTimeMs())
                .addValue("accessStatus", verification.accessStatus().name())
                .addValue("requiresAccount", verification.requiresAccount())
                .addValue("requiresPayment", verification.requiresPayment())
                .addValue("checkedAt", timestamp(verification.checkedAt()))
                .addValue("errorCode", verification.errorCode())
                .addValue("errorMessage", verification.errorMessage()));
    }

    private void createReviewItem(UUID resourceId, ResourceCandidate candidate, String issue, VerificationResult verification, String action) {
        jdbc.update("""
                INSERT INTO review_queue (
                    id, resource_id, candidate_url, issue, evidence, agent_decision,
                    confidence_score, recommended_action)
                VALUES (:id, :resourceId, :candidateUrl, :issue, :evidence, :decision, :confidence, :action)
                """, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("resourceId", resourceId)
                .addValue("candidateUrl", candidate.sourceUrl().toString())
                .addValue("issue", issue)
                .addValue("evidence", verification.evidenceSummary())
                .addValue("decision", verification.accessStatus().name())
                .addValue("confidence", verification.semanticMatch() ? 1.0 : 0.0)
                .addValue("action", action));
    }

    private static String fallbackTitle(ResourceCandidate candidate) {
        if (candidate.expectedTitle() != null && !candidate.expectedTitle().isBlank()) {
            return candidate.expectedTitle().trim();
        }
        String path = candidate.sourceUrl().getPath();
        if (path != null) {
            String[] segments = path.split("/");
            for (int index = segments.length - 1; index >= 0; index--) {
                String segment = segments[index].replace('-', ' ').replace('_', ' ').trim();
                if (!segment.isBlank()) return titleCase(segment);
            }
        }
        return candidate.sourceUrl().getHost() == null ? "Recurso sin título verificado" : candidate.sourceUrl().getHost();
    }

    private static String titleCase(String value) {
        StringBuilder result = new StringBuilder();
        for (String word : value.split("\\s+")) {
            if (word.isBlank()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static Timestamp timestamp(Instant value) {
        return Timestamp.from(value == null ? Instant.now() : value);
    }

    private static ResourceStatus mapStatus(AccessStatus status) {
        return switch (status) {
            case ACTIVE -> ResourceStatus.VERIFIED;
            case LINK_CHANGED -> ResourceStatus.LINK_CHANGED;
            case TEMPORARILY_UNAVAILABLE -> ResourceStatus.TEMPORARILY_UNAVAILABLE;
            case REMOVED -> ResourceStatus.REMOVED;
            case BLOCKED, UNKNOWN -> ResourceStatus.REVIEW_REQUIRED;
        };
    }
}
