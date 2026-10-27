package com.siginex.api.service.kb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Jackson parse POJO for the root of {@code classpath:kb/banco.json}. Only the
 * fields the seed needs are mapped; any extra field is ignored.
 *
 * <p>Uses {@code @Getter @Setter @NoArgsConstructor} (not {@code @Value}) so
 * Jackson can deserialize it, following the request-DTO convention of Fase 2b.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoJson {

    @JsonProperty("kb_version")
    private String kbVersion;

    @JsonProperty("total_preguntas")
    private Integer totalPreguntas;

    private String checksum;

    private List<BancoModuloJson> modulos;
}
