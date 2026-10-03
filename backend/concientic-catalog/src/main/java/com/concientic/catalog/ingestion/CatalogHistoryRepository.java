package com.concientic.catalog.ingestion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CatalogHistoryRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CatalogHistoryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ResourceSnapshot> latestSnapshot(UUID resourceId) {
        List<ResourceSnapshot> snapshots = jdbc.query("""
                SELECT title, canonical_url, access_status, free_status, version_number
                FROM resource_versions
                WHERE resource_id = :resourceId
                ORDER BY version_number DESC
                LIMIT 1
                """, new MapSqlParameterSource().addValue("resourceId", resourceId), (rs, rowNum) ->
                new ResourceSnapshot(
                        rs.getString("title"),
                        uri(rs.getString("canonical_url")),
                        AccessStatus.valueOf(rs.getString("access_status")),
                        null,
                        null,
                        null,
                        rs.getString("free_status")));
        return snapshots.stream().findFirst();
    }

    public void saveVersionAndChanges(UUID resourceId, UUID runId, ResourceSnapshot previous,
                                      ResourceSnapshot current, List<ResourceChange> changes) {
        Integer currentVersion = jdbc.queryForObject("""
                SELECT COALESCE(MAX(version_number), 0) + 1
                FROM resource_versions
                WHERE resource_id = :resourceId
                """, new MapSqlParameterSource().addValue("resourceId", resourceId), Integer.class);
        int version = currentVersion == null ? 1 : currentVersion;
        UUID versionId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO resource_versions (
                    id, resource_id, ingestion_run_id, version_number, title, canonical_url,
                    access_status, free_status, resource_status, snapshot_hash, captured_at)
                VALUES (
                    :id, :resourceId, :runId, :version, :title, :canonicalUrl,
                    :accessStatus, :freeStatus, :resourceStatus, :snapshotHash, :capturedAt)
                ON CONFLICT (resource_id, ingestion_run_id) DO NOTHING
                """, new MapSqlParameterSource()
                .addValue("id", versionId)
                .addValue("resourceId", resourceId)
                .addValue("runId", runId)
                .addValue("version", version)
                .addValue("title", current.title())
                .addValue("canonicalUrl", current.canonicalUrl() == null ? null : current.canonicalUrl().toString())
                .addValue("accessStatus", current.accessStatus().name())
                .addValue("freeStatus", current.freeStatus())
                .addValue("resourceStatus", resourceStatus(current))
                .addValue("snapshotHash", hash(current))
                .addValue("capturedAt", Timestamp.from(Instant.now())));

        for (ResourceChange change : changes) {
            jdbc.update("""
                    INSERT INTO resource_changes (
                        id, resource_id, ingestion_run_id, field_name, previous_value,
                        current_value, change_type, evidence_reference)
                    VALUES (:id, :resourceId, :runId, :fieldName, :previousValue,
                            :currentValue, :changeType, :evidenceReference)
                    """, new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID())
                    .addValue("resourceId", resourceId)
                    .addValue("runId", runId)
                    .addValue("fieldName", change.fieldName())
                    .addValue("previousValue", change.previousValue())
                    .addValue("currentValue", change.currentValue())
                    .addValue("changeType", change.changeType())
                    .addValue("evidenceReference", "resource_versions:" + versionId));
        }
    }

    public Optional<UUID> findDuplicate(UUID resourceId, String canonicalUrl, String provider, String title) {
        String normalizedTitle = normalizeTitle(title);
        List<UUID> matches = jdbc.query("""
                SELECT id
                FROM resources
                WHERE id <> :resourceId
                  AND status <> 'DUPLICATE'
                  AND (
                      (:canonicalUrl IS NOT NULL AND canonical_url = :canonicalUrl)
                      OR (:normalizedTitle <> '' AND provider = :provider
                          AND lower(regexp_replace(title, '[^a-z0-9]+', '', 'g')) = :normalizedTitle)
                  )
                ORDER BY first_discovered_at ASC, id ASC
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("resourceId", resourceId)
                .addValue("canonicalUrl", canonicalUrl)
                .addValue("provider", provider)
                .addValue("normalizedTitle", normalizedTitle),
                (rs, rowNum) -> rs.getObject("id", UUID.class));
        return matches.stream().findFirst();
    }

    public boolean markDuplicate(UUID resourceId, UUID winnerId) {
        int rows = jdbc.update("""
                UPDATE resources
                SET duplicate_of = :winnerId,
                    status = 'DUPLICATE',
                    reason_for_inclusion = 'Duplicado detectado automáticamente',
                    last_updated_at = CURRENT_TIMESTAMP
                WHERE id = :resourceId
                  AND id <> :winnerId
                  AND NOT EXISTS (
                      SELECT 1 FROM resource_human_decisions hd
                      WHERE hd.resource_id = resources.id
                        AND hd.decision IN ('APPROVED', 'CORRECTED'))
                """, new MapSqlParameterSource()
                .addValue("resourceId", resourceId)
                .addValue("winnerId", winnerId));
        return rows > 0;
    }

    public void saveReport(UUID runId, String status, ResearchCycleResult result,
                           ResourceObservationStore.PersistedCounts counts) {
        LinkedHashMap<String, Object> report = new LinkedHashMap<>();
        report.put("runId", runId);
        report.put("status", status);
        report.put("generatedAt", Instant.now().toString());
        report.put("candidatesDiscovered", result.candidatesDiscovered());
        report.put("candidatesVerified", result.candidatesVerified());
        report.put("activeCandidates", result.activeCandidates());
        report.put("reviewRequired", result.reviewRequired());
        report.put("resourcesPersisted", counts.resourcesPersisted());
        report.put("checksPersisted", counts.checksPersisted());
        report.put("reviewItemsCreated", counts.reviewItemsCreated());
        report.put("changesDetected", counts.changesDetected());
        report.put("duplicatesDetected", counts.duplicatesDetected());
        report.put("warnings", result.warnings());
        try {
            jdbc.update("""
                    INSERT INTO daily_reports (run_id, status, report_json)
                    VALUES (:runId, :status, CAST(:reportJson AS jsonb))
                    ON CONFLICT (run_id) DO UPDATE SET
                        status = EXCLUDED.status,
                        generated_at = CURRENT_TIMESTAMP,
                        report_json = EXCLUDED.report_json
                    """, new MapSqlParameterSource()
                    .addValue("runId", runId)
                    .addValue("status", status)
                    .addValue("reportJson", objectMapper.writeValueAsString(report)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize daily catalog report", exception);
        }
    }

    public String findReport(UUID runId) {
        return jdbc.queryForObject("SELECT report_json::text FROM daily_reports WHERE run_id = :runId",
                new MapSqlParameterSource().addValue("runId", runId), String.class);
    }

    private static URI uri(String value) {
        return value == null || value.isBlank() ? null : URI.create(value);
    }

    private static String resourceStatus(ResourceSnapshot snapshot) {
        return switch (snapshot.accessStatus()) {
            case ACTIVE -> "VERIFIED";
            case LINK_CHANGED -> "LINK_CHANGED";
            case REMOVED -> "REMOVED";
            case TEMPORARILY_UNAVAILABLE -> "TEMPORARILY_UNAVAILABLE";
            case BLOCKED, UNKNOWN -> "REVIEW_REQUIRED";
        };
    }

    private static String normalizeTitle(String title) {
        if (title == null) return "";
        return Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "");
    }

    private static String hash(ResourceSnapshot snapshot) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(snapshot.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash resource snapshot", exception);
        }
    }
}
