package com.siginex.api.dto.reset;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de "solicitar recuperación de contraseña". Solo validación de formato.
 *
 * <p>La respuesta del endpoint es SIEMPRE la misma (204) exista o no el email,
 * para no filtrar si una cuenta existe (sin enumeración, §5 del plan).
 */
public record SolicitarResetRequest(
        @NotBlank @Email String email) {
}
