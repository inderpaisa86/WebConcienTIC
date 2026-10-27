package com.siginex.api.service.kb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Jackson parse POJO for a module node of {@code banco.json}. The JSON key
 * {@code short} is a Java reserved word, so it is mapped to the
 * {@code shortNombre} field via {@link JsonProperty}. {@code escala} may arrive
 * as an empty string for some modules; the seed normalizes {@code ""} to
 * {@code null}.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoModuloJson {

    private String id;
    private String nombre;

    @JsonProperty("short")
    private String shortNombre;

    private BigDecimal weight;
    private String escala;
    private List<BancoPreguntaJson> preguntas;
}
