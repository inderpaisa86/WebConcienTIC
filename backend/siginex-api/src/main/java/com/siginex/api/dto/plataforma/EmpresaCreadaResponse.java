package com.siginex.api.dto.plataforma;

import com.siginex.api.domain.EmpresaCreada;

import java.util.UUID;

/**
 * Respuesta del alta de empresa. Nunca expone secretos (ni hash de contraseña
 * ni tokens). Devuelve los identificadores creados para que el operador pueda
 * referirse a ellos.
 */
public record EmpresaCreadaResponse(
        UUID tenantId,
        String tenantNombre,
        String tenantSlug,
        UUID adminUsuarioId,
        String adminEmail) {

    public static EmpresaCreadaResponse from(EmpresaCreada creada) {
        return new EmpresaCreadaResponse(
                creada.getTenantId(),
                creada.getTenantNombre(),
                creada.getTenantSlug(),
                creada.getAdminUsuarioId(),
                creada.getAdminEmail());
    }
}
