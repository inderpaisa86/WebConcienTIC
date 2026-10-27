package com.siginex.api.dto.respuesta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single answer in the save-batch request body. Only format validation lives
 * here (controller concern): {@code preguntaId} must be present and {@code valor}
 * must be one of the {@code siginex.valor_respuesta} enum values. The database
 * CAST would reject anything else anyway; the {@code @Pattern} fails fast with a
 * 400 instead of a 500.
 *
 * <p>Uses {@code @Getter @Setter @NoArgsConstructor} (not {@code @Value}) so
 * Spring MVC / Jackson can deserialize it.
 */
@Getter
@Setter
@NoArgsConstructor
public class RespuestaItem {

    @NotBlank
    private String preguntaId;

    @NotBlank
    @Pattern(regexp = "0|1|2|si|no|na|sin_dato",
            message = "valor debe ser uno de: 0, 1, 2, si, no, na, sin_dato")
    private String valor;
}
