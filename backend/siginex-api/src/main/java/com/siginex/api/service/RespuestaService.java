package com.siginex.api.service;

import com.siginex.api.domain.RespuestaEntrada;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data coordination for diagnostico answers. Every operation runs under the
 * actor's tenant so PostgreSQL Row-Level Security isolates the rows. Implemented
 * by {@link RespuestaServiceImpl}.
 *
 * <p>This layer holds no authorization rules; the {@code tenantId} it receives
 * is always resolved server-side by the delegate from the authenticated
 * principal.
 */
public interface RespuestaService {

    void guardar(UUID tenantId, UUID diagnosticoId, List<RespuestaEntrada> entradas);

    Map<String, String> listar(UUID tenantId, UUID diagnosticoId);
}
