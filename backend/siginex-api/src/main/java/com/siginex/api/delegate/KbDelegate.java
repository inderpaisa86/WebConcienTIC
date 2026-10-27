package com.siginex.api.delegate;

import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;

import java.util.List;

/**
 * Coordination for the read-only knowledge bank (KB) endpoints. The KB is
 * global, so there are no tenant rules to enforce here; the layer exists to keep
 * the Controller -> Delegate -> Service -> Repository flow consistent with the
 * rest of the API. A valid session is still required (enforced by the security
 * chain). Implemented by {@link KbDelegateImpl}.
 */
public interface KbDelegate {

    List<KbModulo> listarModulos();

    List<KbPregunta> listarPreguntas(String moduloId);
}
