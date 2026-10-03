package com.concientic.catalog.review;

import com.concientic.catalog.api.ResourceCheckResponse;
import com.concientic.catalog.api.ReviewQueueItemResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReviewQueueRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public ReviewQueueRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ReviewQueueItemResponse> search(String status, int offset, int limit) {
        return jdbc.query("""
                SELECT rq.id, rq.resource_id, r.title, rq.candidate_url, rq.issue, rq.evidence,
                       rq.agent_decision, rq.confidence_score, rq.recommended_action, rq.status,
                       rq.created_at, rq.resolved_at, rq.reviewer_id, rq.human_decision,
                       rq.decision_notes, rq.version, r.status AS resource_status, r.free_status
                FROM review_queue rq
                LEFT JOIN resources r ON r.id = rq.resource_id
                WHERE (:status IS NULL OR rq.status = :status)
                ORDER BY rq.created_at DESC
                LIMIT :limit OFFSET :offset
                """, new MapSqlParameterSource()
                .addValue("status", status)
                .addValue("limit", limit)
                .addValue("offset", offset), itemRowMapper());
    }

    public long count(String status) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM review_queue rq
                WHERE (:status IS NULL OR rq.status = :status)
                """, new MapSqlParameterSource().addValue("status", status), Long.class);
        return count == null ? 0 : count;
    }

    public Optional<ReviewQueueItemResponse> find(UUID reviewId) {
        List<ReviewQueueItemResponse> items = jdbc.query("""
                SELECT rq.id, rq.resource_id, r.title, rq.candidate_url, rq.issue, rq.evidence,
                       rq.agent_decision, rq.confidence_score, rq.recommended_action, rq.status,
                       rq.created_at, rq.resolved_at, rq.reviewer_id, rq.human_decision,
                       rq.decision_notes, rq.version, r.status AS resource_status, r.free_status
                FROM review_queue rq
                LEFT JOIN resources r ON r.id = rq.resource_id
                WHERE rq.id = :id
                """, new MapSqlParameterSource().addValue("id", reviewId), itemRowMapper());
        return items.stream().findFirst();
    }

    public List<ResourceCheckResponse> checksFor(UUID resourceId) {
        if (resourceId == null) return List.of();
        return jdbc.query("""
                SELECT id, ingestion_run_id, requested_url, final_url, http_status,
                       access_status, requires_account, requires_payment, checked_at,
                       error_code, error_message
                FROM resource_checks
                WHERE resource_id = :resourceId
                ORDER BY checked_at DESC
                """, new MapSqlParameterSource().addValue("resourceId", resourceId), (rs, rowNum) ->
                new ResourceCheckResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("ingestion_run_id", UUID.class),
                        rs.getString("requested_url"),
                        rs.getString("final_url"),
                        (Integer) rs.getObject("http_status"),
                        rs.getString("access_status"),
                        (Boolean) rs.getObject("requires_account"),
                        (Boolean) rs.getObject("requires_payment"),
                        instant(rs.getTimestamp("checked_at")),
                        rs.getString("error_code"),
                        rs.getString("error_message")));
    }

    public int resolve(UUID reviewId, int version, String status, String reviewerId, String decision, String notes) {
        return jdbc.update("""
                UPDATE review_queue
                SET status = :status,
                    resolved_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP,
                    version = version + 1,
                    reviewer_id = :reviewerId,
                    human_decision = :decision,
                    decision_notes = :notes
                WHERE id = :id AND status = 'OPEN' AND version = :version
                """, new MapSqlParameterSource()
                .addValue("id", reviewId)
                .addValue("version", version)
                .addValue("status", status)
                .addValue("reviewerId", reviewerId)
                .addValue("decision", decision)
                .addValue("notes", notes));
    }

    public void saveHumanDecision(UUID resourceId, String decision, String reviewerId, String notes,
                                  String title, String description, String freeStatus, String resourceStatus) {
        jdbc.update("""
                INSERT INTO resource_human_decisions (
                    resource_id, decision, reviewer_id, decision_notes,
                    approved_title, approved_description, approved_free_status,
                    approved_resource_status, decided_at, updated_at)
                VALUES (
                    :resourceId, :decision, :reviewerId, :notes,
                    :title, :description, :freeStatus, :resourceStatus,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (resource_id) DO UPDATE SET
                    decision = EXCLUDED.decision,
                    reviewer_id = EXCLUDED.reviewer_id,
                    decision_notes = EXCLUDED.decision_notes,
                    approved_title = EXCLUDED.approved_title,
                    approved_description = EXCLUDED.approved_description,
                    approved_free_status = EXCLUDED.approved_free_status,
                    approved_resource_status = EXCLUDED.approved_resource_status,
                    decided_at = EXCLUDED.decided_at,
                    updated_at = CURRENT_TIMESTAMP
                """, new MapSqlParameterSource()
                .addValue("resourceId", resourceId)
                .addValue("decision", decision)
                .addValue("reviewerId", reviewerId)
                .addValue("notes", notes)
                .addValue("title", title)
                .addValue("description", description)
                .addValue("freeStatus", freeStatus)
                .addValue("resourceStatus", resourceStatus));
    }

    public void updateResource(UUID resourceId, String title, String description, String freeStatus,
                               String resourceStatus, String notes) {
        jdbc.update("""
                UPDATE resources
                SET title = :title,
                    short_description = COALESCE(:description, short_description),
                    free_status = :freeStatus,
                    free_explanation = 'Validado por revisión humana',
                    status = :resourceStatus,
                    reason_for_inclusion = 'Validado por revisión humana',
                    notes = COALESCE(:notes, notes),
                    last_updated_at = CURRENT_TIMESTAMP
                WHERE id = :resourceId
                """, new MapSqlParameterSource()
                .addValue("resourceId", resourceId)
                .addValue("title", title)
                .addValue("description", description)
                .addValue("freeStatus", freeStatus)
                .addValue("resourceStatus", resourceStatus)
                .addValue("notes", notes));
    }

    public void audit(UUID reviewId, UUID resourceId, String action, String reviewerId, String notes, String payload) {
        jdbc.update("""
                INSERT INTO review_decision_audit (
                    id, review_queue_id, resource_id, action, reviewer_id, decision_notes, payload)
                VALUES (:id, :reviewId, :resourceId, :action, :reviewerId, :notes, CAST(:payload AS jsonb))
                """, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("reviewId", reviewId)
                .addValue("resourceId", resourceId)
                .addValue("action", action)
                .addValue("reviewerId", reviewerId)
                .addValue("notes", notes)
                .addValue("payload", payload == null ? "{}" : payload));
    }

    private static RowMapper<ReviewQueueItemResponse> itemRowMapper() {
        return (rs, rowNum) -> new ReviewQueueItemResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("resource_id", UUID.class),
                rs.getString("title"),
                rs.getString("candidate_url"),
                rs.getString("issue"),
                rs.getString("evidence"),
                rs.getString("agent_decision"),
                number(rs.getBigDecimal("confidence_score")),
                rs.getString("recommended_action"),
                rs.getString("status"),
                instant(rs.getTimestamp("created_at")),
                instant(rs.getTimestamp("resolved_at")),
                rs.getString("reviewer_id"),
                rs.getString("human_decision"),
                rs.getString("decision_notes"),
                rs.getString("resource_status"),
                rs.getString("free_status"),
                rs.getInt("version"));
    }

    private static Double number(java.math.BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private static java.time.Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}
