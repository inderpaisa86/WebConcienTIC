package com.siginex.api.dto.respuesta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Save-batch request body for {@code PUT /api/v1/diagnosticos/{id}/respuestas}:
 * a non-empty list of {@code {preguntaId, valor}} items. The tenant and the
 * diagnostico id are resolved server-side; they are never accepted in the body.
 *
 * <p>Uses {@code @Getter @Setter @NoArgsConstructor} (not {@code @Value}) so
 * Spring MVC / Jackson can deserialize it.
 */
@Getter
@Setter
@NoArgsConstructor
public class GuardarRespuestasRequest {

    @NotEmpty
    @Valid
    private List<RespuestaItem> respuestas;
}
