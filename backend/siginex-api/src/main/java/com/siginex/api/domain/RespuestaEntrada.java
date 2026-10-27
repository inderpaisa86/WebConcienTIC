package com.siginex.api.domain;

import lombok.Builder;
import lombok.Value;

/**
 * A single answer to persist: the question id and its value. Immutable value
 * built with Lombok {@code @Value @Builder} (project convention: no
 * {@code record}).
 *
 * <p>Faithful to the original {@code guardarRespuestas}: the {@code skillId} is
 * NOT carried here, it is derived from {@code preguntaId} at persist time
 * (the part before the first {@code '-'}). {@code valor} is one of the
 * {@code siginex.valor_respuesta} enum values.
 */
@Value
@Builder
public class RespuestaEntrada {

    String preguntaId;
    String valor;
}
