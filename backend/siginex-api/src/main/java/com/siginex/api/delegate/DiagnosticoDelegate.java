package com.siginex.api.delegate;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import com.siginex.api.domain.SiginexPrincipal;

import java.util.List;
import java.util.UUID;

/**
 * Business coordination for diagnosticos. Resolves the tenant from the
 * authenticated {@link SiginexPrincipal} (never from the client) and enforces
 * the authorization rules. Implemented by {@link DiagnosticoDelegateImpl}.
 */
public interface DiagnosticoDelegate {

    DiagnosticoDetalle crear(SiginexPrincipal actor, UUID organizacionId);

    List<DiagnosticoResumen> listar(SiginexPrincipal actor);

    DiagnosticoDetalle obtener(SiginexPrincipal actor, UUID id);
}
