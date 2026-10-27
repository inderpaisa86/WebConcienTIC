package com.siginex.api.repository;

import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;

import java.util.List;

/**
 * Read-only data access for the GLOBAL knowledge bank (KB). The KB carries no
 * tenant and no Row-Level Security, so these reads run directly (no
 * {@code runInTenant}). Implemented by {@link JdbcKbRepository}.
 */
public interface KbRepository {

    /** Lists every module ordered by {@code orden}. */
    List<KbModulo> listarModulos();

    /** Lists the questions of a module ordered by {@code orden}. */
    List<KbPregunta> listarPreguntas(String moduloId);
}
