package com.siginex.api.auditoria;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Records audit events (login, logout, user management, ...). The payload is
 * serialized to JSON for the {@code jsonb} column.
 *
 * <p>Data minimization (Ley 1581): callers pass only non-sensitive fields.
 * As defense in depth this service rejects any payload key that looks like a
 * secret ({@code password}, {@code hash}, {@code token}, ...) so a credential
 * can never reach the audit log.
 */
@Service
public class AuditoriaService {

    private static final String[] FORBIDDEN_KEY_FRAGMENTS = {"password", "contrasena", "contraseña", "hash", "token", "secret"};

    private final AuditoriaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    public void registrar(UUID tenantId, String actor, String accion, String entidad, String entidadId, Map<String, ?> payload) {
        String payloadJson = serialize(payload);
        repository.insertar(tenantId, actor, accion, entidad, entidadId, payloadJson);
    }

    private String serialize(Map<String, ?> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        rejectSecretKeys(payload);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("audit payload is not serializable", e);
        }
    }

    private static void rejectSecretKeys(Map<String, ?> payload) {
        for (String key : payload.keySet()) {
            if (key == null) {
                continue;
            }
            String lower = key.toLowerCase();
            for (String fragment : FORBIDDEN_KEY_FRAGMENTS) {
                if (lower.contains(fragment)) {
                    throw new IllegalArgumentException("audit payload must not contain secret-like key: " + key);
                }
            }
        }
    }
}
