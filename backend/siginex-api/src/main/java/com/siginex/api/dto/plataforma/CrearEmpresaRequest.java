package com.siginex.api.dto.plataforma;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición de alta de empresa (Fase 5, operación de plataforma).
 * Solo validación de formato (responsabilidad del controller); las reglas de
 * negocio (autorización por clave de plataforma, slug único, email único,
 * política de contraseña) viven en el delegate.
 *
 * <p>El {@code tenant_id} nunca se acepta del cliente: lo genera la BD. El
 * primer usuario se crea siempre con rol {@code admin}.
 *
 * @param empresaNombre nombre visible de la empresa (tenant).
 * @param adminEmail     email del primer usuario admin (único global).
 * @param adminNombre    nombre del primer usuario admin.
 * @param adminPassword  contraseña del admin; política mínima 12 (igual que el
 *                       alta de usuarios). Se hashea con Argon2id; nunca se
 *                       loguea ni se audita en claro.
 */
public record CrearEmpresaRequest(
        @NotBlank String empresaNombre,
        @NotBlank @Email String adminEmail,
        @NotBlank String adminNombre,
        @NotBlank @Size(min = 12) String adminPassword) {
}
