package com.siginex.api.repository;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcDiagnosticoRepositoryTest {

    @Test
    void kbVersionVigenteQueriesKbVersionOrderedByPublicadoEnDescLimitOne() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(String.class)))
                .thenReturn("3.0.0");
        var repository = new JdbcDiagnosticoRepository(jdbc);

        String resultado = repository.kbVersionVigente();

        assertThat(resultado).isEqualTo("3.0.0");
        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(sql.capture(), any(MapSqlParameterSource.class), eq(String.class));
        assertThat(sql.getValue()).contains("FROM siginex.kb_version");
        assertThat(sql.getValue()).contains("ORDER BY publicado_en DESC");
        assertThat(sql.getValue()).contains("LIMIT 1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void insertarUsesInsertIntoDiagnosticoWithKbVersion() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID organizacionId = UUID.randomUUID();
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(UUID.class)))
                .thenReturn(id);
        DiagnosticoDetalle persistido = DiagnosticoDetalle.builder()
                .id(id).organizacionId(organizacionId).organizacionNombre("Acme").estado("en_progreso").build();
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(persistido);
        var repository = new JdbcDiagnosticoRepository(jdbc);

        DiagnosticoDetalle resultado = repository.insertar(tenantId, organizacionId, "3.0.0");

        assertThat(resultado).isEqualTo(persistido);
        var sql = ArgumentCaptor.forClass(String.class);
        var params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).queryForObject(sql.capture(), params.capture(), eq(UUID.class));
        assertThat(sql.getValue()).contains("INSERT INTO siginex.diagnostico");
        assertThat(sql.getValue()).contains("kb_version");
        assertThat(params.getValue().getValue("tenantId")).isEqualTo(tenantId);
        assertThat(params.getValue().getValue("organizacionId")).isEqualTo(organizacionId);
        assertThat(params.getValue().getValue("kbVersion")).isEqualTo("3.0.0");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listarJoinsOrganizacionAndOrdersByCreadoEnDesc() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        DiagnosticoResumen resumen = DiagnosticoResumen.builder()
                .id(UUID.randomUUID()).organizacionId(UUID.randomUUID()).organizacionNombre("Acme")
                .estado("en_progreso").creadoEn(Instant.now()).build();
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of(resumen));
        var repository = new JdbcDiagnosticoRepository(jdbc);

        List<DiagnosticoResumen> resultado = repository.listar();

        assertThat(resultado).containsExactly(resumen);
        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), any(RowMapper.class));
        assertThat(sql.getValue()).contains("FROM siginex.diagnostico d");
        assertThat(sql.getValue()).contains("INNER JOIN siginex.organizacion o");
        assertThat(sql.getValue()).contains("ORDER BY d.creado_en DESC");
    }
}
