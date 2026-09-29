package com.concientic.catalog.catalog;

import com.concientic.catalog.domain.ResourceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcResourceRepositoryTest {

    @Test
    void appliesSearchFiltersToTheResultQuery() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.query(anyString(), any(MapSqlParameterSource.class), org.mockito.ArgumentMatchers.<RowMapper<ResourceResponse>>any()))
                .thenReturn(List.of());
        var repository = new JdbcResourceRepository(jdbc);

        repository.search("inteligencia", "ia", "es", "Inicial", "Curso", 0, 24);

        var query = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(query.capture(), any(MapSqlParameterSource.class), org.mockito.ArgumentMatchers.<RowMapper<ResourceResponse>>any());
        assertThat(query.getValue()).contains("search_document @@ plainto_tsquery");
        assertThat(query.getValue()).contains("rc.competency_id = :competency");
        assertThat(query.getValue()).contains(":language = ANY(r.languages)");
        assertThat(query.getValue()).contains("r.level = :level");
        assertThat(query.getValue()).contains("r.format = :format");
    }
}
