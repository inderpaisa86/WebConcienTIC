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
import java.util.Set;
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

            ResourceStatus resourceStatus = titleMissing ? ResourceStatus.REVIEW_REQUIRED : mapStatus(verification.accessStatus());
            FreeStatus freeStatus = inferFreeStatus(candidate, verification);
            UUID resourceId = upsertResource(candidate, verification, title, resourceStatus, freeStatus);
            persistCheck(resourceId, runId, verification);
            persistClassification(resourceId, candidate);
            resources++;
            checks++;

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

    private UUID upsertResource(ResourceCandidate candidate, VerificationResult verification, String title,
                                 ResourceStatus status, FreeStatus freeStatus) {
        UUID stableId = UUID.nameUUIDFromBytes(candidate.sourceUrl().toString().getBytes(StandardCharsets.UTF_8));
        String description = verification.descriptionExcerpt() == null || verification.descriptionExcerpt().isBlank()
                ? "[Sin descripción verificada]"
                : verification.descriptionExcerpt();
        String verifiedUrl = verification.finalUrl() == null ? candidate.sourceUrl().toString() : verification.finalUrl().toString();
        String canonicalUrl = verification.canonicalUrl() == null ? verifiedUrl : verification.canonicalUrl().toString();
        String reason = verification.isAutomaticallyPublishable()
                ? "Acceso HTTP, título y semántica verificados; gratuidad inferida automáticamente"
                : "Requiere revisión: " + verification.evidenceSummary();

        String sql = """
                INSERT INTO resources (
                    id, title, short_description, provider, provider_type, source_url,
                    canonical_url, verified_url, url_status, http_status, last_verified_at,
                    free_status, free_explanation, format, primary_competency, guardian_primary,
                    verification_score, overall_score,
                    reason_for_inclusion, status, source_last_checked, notes)
                VALUES (
                    :id, :title, :description, :provider, :providerType, :sourceUrl,
                    :canonicalUrl, :verifiedUrl, :urlStatus, :httpStatus, :lastVerifiedAt,
                    :freeStatus, :freeExplanation, 'other', :competency, :guardian,
                    :verificationScore, :verificationScore,
                    :reason, :status, :lastVerifiedAt, :notes)
                ON CONFLICT (source_url) DO UPDATE SET
                    title = CASE WHEN EXISTS (
                        SELECT 1 FROM resource_human_decisions hd
                        WHERE hd.resource_id = resources.id
                          AND hd.decision IN ('APPROVED', 'CORRECTED', 'REJECTED'))
                        THEN resources.title ELSE EXCLUDED.title END,
                    short_description = CASE WHEN EXISTS (
                        SELECT 1 FROM resource_human_decisions hd
                        WHERE hd.resource_id = resources.id
                          AND hd.decision IN ('APPROVED', 'CORRECTED', 'REJECTED'))
                        THEN resources.short_description ELSE EXCLUDED.short_description END,
                    provider = EXCLUDED.provider,
                    provider_type = EXCLUDED.provider_type,
                    primary_competency = EXCLUDED.primary_competency,
                    guardian_primary = EXCLUDED.guardian_primary,
                    canonical_url = EXCLUDED.canonical_url,
                    verified_url = EXCLUDED.verified_url,
                    url_status = EXCLUDED.url_status,
                    http_status = EXCLUDED.http_status,
                    last_verified_at = EXCLUDED.last_verified_at,
                    free_status = CASE WHEN EXISTS (
                        SELECT 1 FROM resource_human_decisions hd
                        WHERE hd.resource_id = resources.id
                          AND hd.decision IN ('APPROVED', 'CORRECTED', 'REJECTED'))
                        THEN resources.free_status ELSE EXCLUDED.free_status END,
                    free_explanation = CASE WHEN EXISTS (
                        SELECT 1 FROM resource_human_decisions hd
                        WHERE hd.resource_id = resources.id
                          AND hd.decision IN ('APPROVED', 'CORRECTED', 'REJECTED'))
                        THEN resources.free_explanation ELSE EXCLUDED.free_explanation END,
                    verification_score = EXCLUDED.verification_score,
                    overall_score = EXCLUDED.overall_score,
                    reason_for_inclusion = CASE WHEN EXISTS (
                        SELECT 1 FROM resource_human_decisions hd
                        WHERE hd.resource_id = resources.id
                          AND hd.decision IN ('APPROVED', 'CORRECTED', 'REJECTED'))
                        THEN resources.reason_for_inclusion ELSE EXCLUDED.reason_for_inclusion END,
                    status = CASE WHEN EXISTS (
                        SELECT 1 FROM resource_human_decisions hd
                        WHERE hd.resource_id = resources.id
                          AND hd.decision IN ('APPROVED', 'CORRECTED', 'REJECTED'))
                        THEN resources.status ELSE EXCLUDED.status END,
                    last_updated_at = CURRENT_TIMESTAMP,
                    source_last_checked = EXCLUDED.source_last_checked,
                    notes = CASE WHEN EXISTS (
                        SELECT 1 FROM resource_human_decisions hd
                        WHERE hd.resource_id = resources.id
                          AND hd.decision IN ('APPROVED', 'CORRECTED', 'REJECTED'))
                        THEN resources.notes ELSE EXCLUDED.notes END
                RETURNING id
                """;
        UUID persistedId = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", stableId)
                .addValue("title", title)
                .addValue("description", description)
                .addValue("provider", candidate.provider())
                .addValue("providerType", candidate.providerType())
                .addValue("competency", validCompetency(candidate.competencyHint()))
                .addValue("guardian", validGuardian(candidate.guardianHint()))
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
                SELECT :id, :resourceId, :candidateUrl, :issue, :evidence, :decision, :confidence, :action
                WHERE :resourceId IS NULL OR NOT EXISTS (
                    SELECT 1 FROM resource_human_decisions hd
                    WHERE hd.resource_id = :resourceId)
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

    private void persistClassification(UUID resourceId, ResourceCandidate candidate) {
        String competency = validCompetency(candidate.competencyHint());
        if (competency != null) {
            jdbc.update("""
                    INSERT INTO resource_competencies (
                        resource_id, competency_id, is_primary, assignment_source, confidence_score, explanation)
                    VALUES (:resourceId, :competency, TRUE, 'SOURCE_PAGE_HINT', 0.70,
                            'Clasificación inicial declarada por la configuración de la página semilla')
                    ON CONFLICT (resource_id, competency_id) DO UPDATE SET
                        is_primary = TRUE,
                        assignment_source = EXCLUDED.assignment_source,
                        confidence_score = EXCLUDED.confidence_score,
                        explanation = EXCLUDED.explanation
                    """, new MapSqlParameterSource()
                    .addValue("resourceId", resourceId)
                    .addValue("competency", competency));
        }

        String guardian = validGuardian(candidate.guardianHint());
        if (guardian != null) {
            jdbc.update("""
                    INSERT INTO resource_guardians (
                        resource_id, guardian_name, is_primary, assignment_source, confidence_score, explanation)
                    VALUES (:resourceId, :guardian, TRUE, 'SOURCE_PAGE_HINT', 0.70,
                            'Guardián inicial declarado por la configuración de la página semilla')
                    ON CONFLICT (resource_id, guardian_name) DO UPDATE SET
                        is_primary = TRUE,
                        assignment_source = EXCLUDED.assignment_source,
                        confidence_score = EXCLUDED.confidence_score,
                        explanation = EXCLUDED.explanation
                    """, new MapSqlParameterSource()
                    .addValue("resourceId", resourceId)
                    .addValue("guardian", guardian));
        }
    }

    private static FreeStatus inferFreeStatus(ResourceCandidate candidate, VerificationResult verification) {
        if (verification.requiresPayment()) return FreeStatus.PAID;
        if (!verification.isAutomaticallyPublishable()) return FreeStatus.UNKNOWN;
        if (candidate.freeStatusHint() != null && !candidate.freeStatusHint().isBlank()) {
            try {
                FreeStatus hinted = FreeStatus.valueOf(candidate.freeStatusHint().trim().toUpperCase());
                if (hinted != FreeStatus.PAID && hinted != FreeStatus.TRIAL && hinted != FreeStatus.UNKNOWN) return hinted;
            } catch (IllegalArgumentException ignored) {
                // Invalid seed hints remain UNKNOWN rather than becoming publication evidence.
            }
        }
        return FreeStatus.FREE;
    }

    private static String validCompetency(String value) {
        return value != null && Set.of("informacion", "comunicacion", "creacion", "seguridad",
                "discernimiento", "ia", "bienestar", "ciudadania").contains(value.trim().toLowerCase())
                ? value.trim().toLowerCase() : null;
    }

    private static String validGuardian(String value) {
        return value != null && Set.of("Emi", "Locky", "Lex", "Byte", "Detective DQ", "Nexo", "Nova").contains(value.trim())
                ? value.trim() : null;
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
