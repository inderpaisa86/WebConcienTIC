package com.siginex.api.service;

import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;

import java.util.List;

/**
 * Read-only coordination for the GLOBAL knowledge bank (KB). Unlike the
 * tenant-scoped services, the KB has no tenant and no Row-Level Security, so no
 * operation runs inside {@code runInTenant}. Implemented by
 * {@link KbServiceImpl}.
 */
public interface KbService {

    List<KbModulo> listarModulos();

    List<KbPregunta> listarPreguntas(String moduloId);
}
