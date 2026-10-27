package com.siginex.api.service;

import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;
import com.siginex.api.repository.KbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default {@link KbService} implementation. The KB is GLOBAL (shared by every
 * tenant), so reads go straight to {@link KbRepository} without fixing a tenant
 * — there is no Row-Level Security to satisfy on {@code kb_modulo} /
 * {@code kb_pregunta}.
 */
@Service
@RequiredArgsConstructor
public class KbServiceImpl implements KbService {

    private final KbRepository kbRepository;

    @Override
    public List<KbModulo> listarModulos() {
        return kbRepository.listarModulos();
    }

    @Override
    public List<KbPregunta> listarPreguntas(String moduloId) {
        return kbRepository.listarPreguntas(moduloId);
    }
}
