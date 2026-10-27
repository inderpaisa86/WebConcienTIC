package com.siginex.api.service;

import java.util.Map;
import java.util.UUID;

/**
 * Records audit events (login, logout, user management, ...). Implemented by
 * {@link AuditoriaServiceImpl}.
 */
public interface AuditoriaService {

    /**
     * Records an audit event. The payload must contain only non-sensitive
     * fields; implementations reject secret-like keys as defense in depth.
     */
    void registrar(UUID tenantId, String actor, String accion, String entidad, String entidadId, Map<String, ?> payload);
}
