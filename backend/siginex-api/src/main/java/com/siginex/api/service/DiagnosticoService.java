package com.siginex.api.service;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data coordination for diagnosticos. Every operation runs under the actor's
 * tenant so PostgreSQL Row-Level Security isolates the rows. Implemented by
 * {@link DiagnosticoServiceImpl}.
 *
 * <p>This layer holds no business rules; the {@code tenantId} it receives is
 * always resolved server-side by the delegate from the authenticated principal.
 */
public interface DiagnosticoService {

    String kbVersionVigente(UUID tenantId);

    DiagnosticoDetalle crear(UUID tenantId, UUID organizacionId, String kbVersion);

    List<DiagnosticoResumen> listar(UUID tenantId);

    Optional<DiagnosticoDetalle> buscarPorId(UUID tenantId, UUID id);
}
