package com.siginex.api.dto.organizacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Create-organizacion request body. Only format validation lives here
 * (controller concern); the tenant is resolved server-side by the delegate and
 * is never accepted from the client.
 *
 * <p>Uses {@code @Getter @Setter @NoArgsConstructor} (not {@code @Value}) so
 * Spring MVC / Jackson can deserialize it.
 */
@Getter
@Setter
@NoArgsConstructor
public class CrearOrganizacionRequest {

    @NotBlank
    private String nombre;

    private String sector;

    @Pattern(regexp = "micro|pequena|mediana|grande")
    private String tamano;
}
