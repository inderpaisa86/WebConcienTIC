package com.siginex.api.dto.calculo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuerpo del request para {@code POST /api/v1/diagnosticos/{id}/benchmark}:
 * el nivel tecnológico declarado de la empresa ({@code 1..5}). El tenant y el
 * id del diagnóstico se resuelven en servidor; nunca se aceptan en el body.
 *
 * <p>Bean Validation {@code @Min(1) @Max(5)} valida el nivel en el borde del
 * controller (defensa en profundidad; el service vuelve a validar, diseño §9.6).
 * Usa {@code @Getter @Setter @NoArgsConstructor} (no {@code @Value}) para que
 * Spring MVC / Jackson pueda deserializarlo.
 */
@Getter
@Setter
@NoArgsConstructor
public class RegistrarBenchmarkRequest {

    @Min(1)
    @Max(5)
    private int nivel;
}
