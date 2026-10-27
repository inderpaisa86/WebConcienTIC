package com.siginex.api.repository;

import com.siginex.api.domain.Organizacion;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcOrganizacionRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    void insertarPortsSqlWithTenantCastAndAplicabilidad() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Organizacion persistida = Organizacion.builder()
                .id(id).tenantId(tenantId).nombre("Acme").sector("tecnologia")
                .tamano("mediana").creadoEn(Instant.now()).build();
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(persistida);
        var repository = new JdbcOrganizacionRepository(jdbc);

        Organizacion aInsertar = Organizacion.builder()
                .tenantId(tenantId).nombre("Acme").sector("tecnologia").tamano("mediana").build();
        Organizacion resultado = repository.insertar(aInsertar);

        assertThat(resultado).isEqualTo(persistida);
        var sql = ArgumentCaptor.forClass(String.class);
        var params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).queryForObject(sql.capture(), params.capture(), any(RowMapper.class));
        assertThat(sql.getValue()).contains("INSERT INTO siginex.organizacion");
        assertThat(sql.getValue()).contains("CAST(:tamano AS siginex.tamano_empresa)");
        assertThat(sql.getValue()).contains("CAST('{}' AS jsonb)");
        assertThat(params.getValue().getValue("tenantId")).isEqualTo(tenantId);
        assertThat(params.getValue().getValue("nombre")).isEqualTo("Acme");
        assertThat(params.getValue().getValue("sector")).isEqualTo("tecnologia");
        assertThat(params.getValue().getValue("tamano")).isEqualTo("mediana");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listarOrdersByCreadoEnDesc() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        Organizacion org = Organizacion.builder()
                .id(UUID.randomUUID()).tenantId(UUID.randomUUID()).nombre("Acme").creadoEn(Instant.now()).build();
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of(org));
        var repository = new JdbcOrganizacionRepository(jdbc);

        List<Organizacion> resultado = repository.listar();

        assertThat(resultado).containsExactly(org);
        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), any(RowMapper.class));
        assertThat(sql.getValue()).contains("FROM siginex.organizacion");
        assertThat(sql.getValue()).contains("ORDER BY creado_en DESC");
    }
}
