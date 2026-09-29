package com.concientic.catalog.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class JdbcIngestionRunStore implements IngestionRunStore {
    private static final Logger log = LoggerFactory.getLogger(JdbcIngestionRunStore.class);

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcIngestionRunStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void start(UUID runId, Instant startedAt, String timezone) {
        log.info("Inserting ingestion_runs start record runId={}", runId);
        int rows = jdbc.update("""
                INSERT INTO ingestion_runs (id, status, started_at, timezone)
                VALUES (:id, 'STARTED', :startedAt, :timezone)
                """, new MapSqlParameterSource()
                .addValue("id", runId)
                .addValue("startedAt", startedAt)
                .addValue("timezone", timezone));
        log.info("Inserted ingestion_runs start record runId={} rows={}", runId, rows);
    }

    @Override
    public void finish(UUID runId, String status, ResearchCycleResult result, String errorMessage) {
        int rows = jdbc.update("""
                UPDATE ingestion_runs
                SET status = :status,
                    finished_at = CURRENT_TIMESTAMP,
                    resources_discovered = :discovered,
                    resources_updated = :updated,
                    resources_removed = :removed,
                    review_required = :reviewRequired,
                    error_message = :errorMessage
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", runId)
                .addValue("status", status)
                .addValue("discovered", result.candidatesDiscovered())
                .addValue("updated", 0)
                .addValue("removed", result.observations().stream().filter(observation -> observation.verification().accessStatus() == AccessStatus.REMOVED).count())
                .addValue("reviewRequired", result.reviewRequired())
                .addValue("errorMessage", errorMessage));
        log.info("Updated ingestion_runs finish record runId={} status={} rows={} errorPresent={}", runId, status, rows, errorMessage != null && !errorMessage.isBlank());
    }
}
