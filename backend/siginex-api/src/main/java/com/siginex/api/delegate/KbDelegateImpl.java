package com.siginex.api.delegate;

import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;
import com.siginex.api.service.KbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default {@link KbDelegate} implementation. The KB is global and public to any
 * authenticated user, so there is no tenant validation — it simply delegates to
 * {@link KbService}. Kept as its own layer for consistency with the 2b
 * package-by-layer structure.
 */
@Component
@RequiredArgsConstructor
public class KbDelegateImpl implements KbDelegate {

    private final KbService kbService;

    @Override
    public List<KbModulo> listarModulos() {
        return kbService.listarModulos();
    }

    @Override
    public List<KbPregunta> listarPreguntas(String moduloId) {
        return kbService.listarPreguntas(moduloId);
    }
}
