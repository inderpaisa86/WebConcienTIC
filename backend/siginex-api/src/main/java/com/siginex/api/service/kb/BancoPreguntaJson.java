package com.siginex.api.service.kb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Jackson parse POJO for a question node of {@code banco.json}.
 * {@code recomendacion} is the {@code {"0":..,"1":..,"2":..}} map of per-answer
 * recommendation texts, persisted as {@code jsonb}.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoPreguntaJson {

    private String id;
    private String pregunta;
    private String norma;
    private String articulo;
    private String requisito;
    private String criterio;
    private BigDecimal peso;
    private Map<String, String> recomendacion;
}
