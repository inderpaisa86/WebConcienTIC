package com.siginex.api.delegate;

import com.siginex.api.domain.RespuestaEntrada;
import com.siginex.api.domain.SiginexPrincipal;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Business coordination for diagnostico answers. Resolves the tenant from the
 * authenticated {@link SiginexPrincipal} (never from the client) and validates
 * that the target diagnostico belongs to the actor's tenant before touching the
 * answers. Implemented by {@link RespuestaDelegateImpl}.
 */
public interface RespuestaDelegate {

    void guardar(SiginexPrincipal actor, UUID diagnosticoId, List<RespuestaEntrada> entradas);

    Map<String, String> obtener(SiginexPrincipal actor, UUID diagnosticoId);
}
