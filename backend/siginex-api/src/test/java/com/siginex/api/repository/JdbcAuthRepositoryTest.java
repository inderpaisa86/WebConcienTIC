package com.siginex.api.repository;


import com.siginex.api.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.EmptyResultDataAccessException;
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

class JdbcAuthRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    void findAuthByEmailQueriesUsuarioByLowerEmailAndDoesNotSetTenant() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        var repository = new JdbcAuthRepository(jdbc);

        assertThat(repository.findAuthByEmail("USER@x.com")).isEmpty();

        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(sql.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertThat(sql.getValue()).contains("FROM siginex.usuario");
        assertThat(sql.getValue()).contains("lower(email) = lower(:email)");
        assertThat(sql.getValue()).doesNotContain("set_config");
        assertThat(sql.getValue()).doesNotContain("app.tenant_id");
    }

    @Test
    void crearSesionInsertsTokenHashAndExpiry() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var repository = new JdbcAuthRepository(jdbc);

        repository.crearSesion(UUID.randomUUID(), UUID.randomUUID(), "hash-123",
                Instant.parse("2030-01-01T00:00:00Z"), "agent", "127.0.0.1");

        var sql = ArgumentCaptor.forClass(String.class);
        var params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).update(sql.capture(), params.capture());
        assertThat(sql.getValue()).contains("INSERT INTO siginex.sesion");
        assertThat(sql.getValue()).contains("token_hash");
        assertThat(sql.getValue()).contains("expira_en");
        assertThat(params.getValue().getValue("tokenHash")).isEqualTo("hash-123");
        assertThat(params.getValue().getValue("expiraEn")).isNotNull();
    }

    @Test
    void revocarSesionUpdatesByTokenHash() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var repository = new JdbcAuthRepository(jdbc);

        repository.revocarSesion("hash-xyz");

        var sql = ArgumentCaptor.forClass(String.class);
        var params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).update(sql.capture(), params.capture());
        assertThat(sql.getValue()).contains("UPDATE siginex.sesion SET revocada = true");
        assertThat(params.getValue().getValue("tokenHash")).isEqualTo("hash-xyz");
    }

    @Test
    void resetearFallosYLoginClearsCounterAndSetsLastLogin() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var repository = new JdbcAuthRepository(jdbc);
        UUID usuarioId = UUID.randomUUID();

        repository.resetearFallosYLogin(usuarioId);

        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).update(sql.capture(), any(MapSqlParameterSource.class));
        assertThat(sql.getValue()).contains("fallos_login = 0");
        assertThat(sql.getValue()).contains("bloqueado_hasta = NULL");
        assertThat(sql.getValue()).contains("ultimo_login = now()");
    }

    @Test
    void incrementarFallosSetsLockUntilWhenProvided() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var repository = new JdbcAuthRepository(jdbc);
        UUID usuarioId = UUID.randomUUID();
        Instant lockUntil = Instant.parse("2030-01-01T00:00:00Z");

        repository.incrementarFallos(usuarioId, lockUntil);

        var sql = ArgumentCaptor.forClass(String.class);
        var params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).update(sql.capture(), params.capture());
        assertThat(sql.getValue()).contains("fallos_login = fallos_login + 1");
        assertThat(params.getValue().getValue("bloqueadoHasta")).isNotNull();
    }
}
