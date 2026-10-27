package com.siginex.api.auditoria;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuditoriaServiceTest {

    private final AuditoriaRepository repository = mock(AuditoriaRepository.class);
    private final AuditoriaService service = new AuditoriaService(repository);

    @Test
    void serializesPayloadToJson() {
        UUID tenantId = UUID.randomUUID();

        service.registrar(tenantId, "a@x.com", "login_exitoso", "usuario", "id-1",
                Map.of("email", "a@x.com"));

        var payload = ArgumentCaptor.forClass(String.class);
        verify(repository).insertar(eq(tenantId), eq("a@x.com"), eq("login_exitoso"), eq("usuario"), eq("id-1"), payload.capture());
        assertThat(payload.getValue()).contains("\"email\"").contains("a@x.com");
    }

    @Test
    void rejectsPayloadWithPasswordKey() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("password", "secret");

        assertThatThrownBy(() -> service.registrar(null, "a@x.com", "x", "usuario", null, payload))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).insertar(any(), anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void rejectsPayloadWithHashKey() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("password_hash", "abc");

        assertThatThrownBy(() -> service.registrar(null, "a@x.com", "x", "usuario", null, payload))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsPayloadWithTokenKey() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("session_token", "abc");

        assertThatThrownBy(() -> service.registrar(null, "a@x.com", "x", "usuario", null, payload))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullPayloadIsStoredAsNull() {
        service.registrar(null, "a@x.com", "logout", "usuario", "id-1", null);

        verify(repository).insertar(eq(null), eq("a@x.com"), eq("logout"), eq("usuario"), eq("id-1"), eq(null));
    }
}
