package com.siginex.api.repository;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for diagnosticos. Every method runs under the actor's tenant: the
 * service wraps the calls in
 * {@code TenantTransactionTemplate.runInTenant(tenantId, ...)} so PostgreSQL
 * Row-Level Security isolates the rows. SQL only; transaction and tenant
 * scoping are the service's concern.
 *
 * <p>{@link #kbVersionVigente()} reads the global {@code kb_version} table
 * (no RLS) and returns {@code 3.0.0} when it is empty, faithful to the original
 * {@code kbVersionVigente}.
 */
public interface DiagnosticoRepository {

    String kbVersionVigente();

    DiagnosticoDetalle insertar(UUID tenantId, UUID organizacionId, String kbVersion);

    List<DiagnosticoResumen> listar();

    Optional<DiagnosticoDetalle> buscarPorId(UUID id);
}
