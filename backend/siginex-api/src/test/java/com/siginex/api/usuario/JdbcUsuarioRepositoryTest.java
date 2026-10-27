package com.siginex.api.usuario;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcUsuarioRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    void insertarIncludesTenantIdAndPasswordHash() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Usuario persistido = new Usuario(id, tenantId, "u@x.com", "U", null, "miembro", true, Instant.now());
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(persistido);
        var repository = new JdbcUsuarioRepository(jdbc);

        Usuario aInsertar = new Usuario(null, tenantId, "u@x.com", "U", "argon2-hash", "miembro", true, null);
        Usuario resultado = repository.insertar(aInsertar);

        assertThat(resultado).isEqualTo(persistido);
        var sql = ArgumentCaptor.forClass(String.class);
        var params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).queryForObject(sql.capture(), params.capture(), any(RowMapper.class));
        assertThat(sql.getValue()).contains("INSERT INTO siginex.usuario");
        assertThat(sql.getValue()).contains("tenant_id");
        assertThat(sql.getValue()).contains("password_hash");
        assertThat(params.getValue().getValue("tenantId")).isEqualTo(tenantId);
        assertThat(params.getValue().getValue("passwordHash")).isEqualTo("argon2-hash");
    }

    @Test
    void cambiarActivoUpdatesByIdWithActivoFlag() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var repository = new JdbcUsuarioRepository(jdbc);
        UUID id = UUID.randomUUID();

        repository.cambiarActivo(id, false);

        var sql = ArgumentCaptor.forClass(String.class);
        var params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).update(sql.capture(), params.capture());
        assertThat(sql.getValue()).contains("UPDATE siginex.usuario SET activo = :activo");
        assertThat(sql.getValue()).contains("WHERE id = :id");
        assertThat(params.getValue().getValue("id")).isEqualTo(id);
        assertThat(params.getValue().getValue("activo")).isEqualTo(false);
    }

    @Test
    void existeEmailUsesLowerEmail() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Boolean.class)))
                .thenReturn(true);
        var repository = new JdbcUsuarioRepository(jdbc);

        assertThat(repository.existeEmail("USER@x.com")).isTrue();

        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(sql.capture(), any(MapSqlParameterSource.class), eq(Boolean.class));
        assertThat(sql.getValue()).contains("lower(email) = lower(:email)");
    }
}
