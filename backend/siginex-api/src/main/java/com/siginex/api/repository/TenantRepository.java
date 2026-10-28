package com.siginex.api.repository;

import com.siginex.api.domain.EmpresaCreada;

/**
 * Acceso a datos de la tabla {@code siginex.tenant}, raíz del aislamiento y
 * SIN RLS. Se usa en el alta de empresa (Fase 5), que corre FUERA de un tenant
 * fijado ({@code runWithoutTenant}) porque el tenant aún no existe. SQL only;
 * la transacción la gobierna el service.
 */
public interface TenantRepository {

    /**
     * Inserta un tenant con el nombre y slug dados y devuelve sus datos de
     * identidad (sin usuario todavía). El id lo genera la BD.
     */
    EmpresaCreada insertarTenant(String nombre, String slug);

    /** ¿Existe ya un tenant con ese slug? (slug es UNIQUE). */
    boolean existeSlug(String slug);
}
