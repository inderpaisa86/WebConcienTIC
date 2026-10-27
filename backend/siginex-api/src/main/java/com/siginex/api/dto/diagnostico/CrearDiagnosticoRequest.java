package com.siginex.api.dto.diagnostico;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Create-diagnostico request body. Only format validation lives here
 * (controller concern); the tenant is resolved server-side by the delegate and
 * is never accepted from the client.
 *
 * <p>Uses {@code @Getter @Setter @NoArgsConstructor} (not {@code @Value}) so
 * Spring MVC / Jackson can deserialize it.
 */
@Getter
@Setter
@NoArgsConstructor
public class CrearDiagnosticoRequest {

    @NotNull
    private UUID organizacionId;
}
