package com.concientic.catalog.catalog;

import com.concientic.catalog.domain.ResourceResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcResourceRepository implements ResourceRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcResourceRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ResourceResponse> search(String query, String competency, String language, String level, String format, String provider, int offset, int limit) {
        String sql = "SELECT r.* FROM resources r "
                + whereClause(query, competency, language, level, format, provider)
                + " ORDER BY COALESCE(r.overall_score, 0) DESC, r.last_verified_at DESC NULLS LAST LIMIT :limit OFFSET :offset";
        MapSqlParameterSource parameters = parameters(query, competency, language, level, format, provider)
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbc.query(sql, parameters, resourceRowMapper());
    }

    @Override
    public long count(String query, String competency, String language, String level, String format, String provider) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM resources r " + whereClause(query, competency, language, level, format, provider), parameters(query, competency, language, level, format, provider), Long.class);
    }

    @Override
    public long countPublished() {
        Long result = jdbc.queryForObject("SELECT COUNT(*) FROM resources WHERE status IN ('ACTIVE', 'UPDATED', 'VERIFIED', 'LINK_CHANGED') AND free_status IN ('FREE', 'FREE_CONTENT_PAID_CERTIFICATE', 'AUDIT_FREE', 'PARTIAL_FREE')", new MapSqlParameterSource(), Long.class);
        return result == null ? 0 : result;
    }

    private String whereClause(String query, String competency, String language, String level, String format, String provider) {
        StringBuilder where = new StringBuilder("WHERE r.status IN ('ACTIVE', 'UPDATED', 'VERIFIED', 'LINK_CHANGED') AND r.free_status IN ('FREE', 'FREE_CONTENT_PAID_CERTIFICATE', 'AUDIT_FREE', 'PARTIAL_FREE')");
        if (query != null && !query.isBlank()) where.append(" AND r.search_document @@ plainto_tsquery('simple', :query)");
        if (competency != null && !competency.isBlank()) where.append(" AND (r.primary_competency = :competency OR EXISTS (SELECT 1 FROM resource_competencies rc WHERE rc.resource_id = r.id AND rc.competency_id = :competency))");
        if (language != null && !language.isBlank()) where.append(" AND :language = ANY(r.languages)");
        if (level != null && !level.isBlank()) where.append(" AND r.level = :level");
        if (format != null && !format.isBlank()) where.append(" AND r.format = :format");
        if (provider != null && !provider.isBlank()) where.append(" AND r.provider = :provider");
        return where.toString();
    }

    private MapSqlParameterSource parameters(String query, String competency, String language, String level, String format, String provider) {
        return new MapSqlParameterSource()
                .addValue("query", query)
                .addValue("competency", competency)
                .addValue("language", language)
                .addValue("level", level)
                .addValue("format", format)
                .addValue("provider", provider);
    }

    private RowMapper<ResourceResponse> resourceRowMapper() {
        return (rs, rowNum) -> new ResourceResponse(
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("short_description"),
                rs.getString("provider"),
                rs.getString("provider_type"),
                rs.getString("country"),
                rs.getString("source_url"),
                rs.getString("canonical_url"),
                rs.getString("verified_url"),
                rs.getString("url_status"),
                (Integer) rs.getObject("http_status"),
                instant(rs, "last_verified_at"),
                instant(rs, "first_discovered_at"),
                instant(rs, "last_updated_at"),
                enumValue(com.concientic.catalog.domain.FreeStatus.class, rs.getString("free_status")),
                rs.getString("free_explanation"),
                array(rs.getArray("languages")),
                rs.getString("level"),
                rs.getString("duration"),
                rs.getString("format"),
                rs.getString("certificate"),
                rs.getString("primary_competency"),
                List.of(),
                rs.getString("guardian_primary"),
                List.of(),
                array(rs.getArray("topics")),
                array(rs.getArray("audience")),
                rs.getString("trust_level"),
                number(rs, "trust_score"),
                number(rs, "relevance_score"),
                number(rs, "quality_score"),
                number(rs, "freshness_score"),
                number(rs, "verification_score"),
                number(rs, "overall_score"),
                List.of(),
                rs.getString("reason_for_inclusion"),
                enumValue(com.concientic.catalog.domain.ResourceStatus.class, rs.getString("status")),
                rs.getObject("duplicate_of", UUID.class),
                instant(rs, "source_last_checked"),
                rs.getString("notes"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        var value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }

    private static Double number(ResultSet rs, String column) throws SQLException {
        var value = rs.getBigDecimal(column);
        return value == null ? null : value.doubleValue();
    }

    private static List<String> array(Array array) throws SQLException {
        if (array == null) return List.of();
        Object value = array.getArray();
        if (!(value instanceof Object[] values)) return List.of();
        List<String> result = new ArrayList<>(values.length);
        for (Object item : values) if (item != null) result.add(item.toString());
        return List.copyOf(result);
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }
}
