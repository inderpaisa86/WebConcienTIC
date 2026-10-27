package com.siginex.api.repository;

import java.util.UUID;

/**
 * Persists audit events into {@code siginex.evento_auditoria}. The payload must
 * never contain passwords or hashes; that invariant is enforced by the service.
 */
public interface AuditoriaRepository {

    void insertar(UUID tenantId, String actor, String accion, String entidad, String entidadId, String payloadJson);
}
