package com.siginex.api.dto.usuario;


import com.siginex.api.domain.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Create-user request body. Only format validation lives here (controller
 * concern); business rules (admin authorization, duplicate email, tenant
 * scoping) are the delegate's job.
 *
 * <p>The {@code tenant_id} is never accepted from the client: the user is
 * always created in the authenticated admin's own tenant.
 */
public record CrearUsuarioRequest(
        @NotBlank @Email String email,
        @NotBlank String nombre,
        @NotBlank @Size(min = 12) String password,
        @NotBlank @Pattern(regexp = "admin|miembro") String rol) {
}
