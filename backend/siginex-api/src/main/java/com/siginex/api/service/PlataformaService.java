package com.siginex.api.service;

import com.siginex.api.domain.EmpresaCreada;
import com.siginex.api.domain.Usuario;

/**
 * Coordinación de datos para el alta de empresa (Fase 5, operación de
 * plataforma). SOLO accede a BD; las reglas de negocio y la autorización por
 * clave de plataforma viven en el delegate. Implementado por
 * {@link PlataformaServiceImpl}.
 *
 * <p>El alta no ocurre dentro de un tenant fijado: el tenant se crea primero
 * FUERA de RLS y el primer usuario admin se inserta ya bajo ese tenant.
 */
public interface PlataformaService {

    /** ¿Existe ya un tenant con ese slug? (slug es UNIQUE, sin RLS). */
    boolean existeSlug(String slug);

    /** ¿Existe ya un usuario con ese email? Comprobación GLOBAL (fuera de RLS). */
    boolean existeEmailGlobal(String email);

    /**
     * Crea el tenant y, bajo ese tenant, su primer usuario admin (ya con el
     * hash Argon2id recibido del delegate). Devuelve los identificadores
     * creados, sin secretos.
     *
     * @param nombre       nombre de la empresa (tenant).
     * @param slug         slug único del tenant.
     * @param adminAInsertar usuario admin a insertar (con passwordHash ya calculado).
     */
    EmpresaCreada crearEmpresaConAdmin(String nombre, String slug, Usuario adminAInsertar);
}
