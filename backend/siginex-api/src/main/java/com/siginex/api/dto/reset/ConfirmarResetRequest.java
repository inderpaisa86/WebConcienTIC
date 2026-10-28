package com.siginex.api.dto.reset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de "confirmar recuperación": el token del enlace y la nueva
 * contraseña. Solo validación de formato aquí; la validez del token (no usado,
 * no expirado, hash coincide) y la política de contraseña las comprueba el
 * delegate.
 *
 * <p>Política de contraseña mínima 12 (igual que el alta de usuarios); se valida
 * también en el delegate como defensa en profundidad.
 */
public record ConfirmarResetRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 12) String nuevaPassword) {
}
