package com.siginex.api.controller;

import com.siginex.api.delegate.KbDelegate;
import com.siginex.api.dto.kb.KbModuloResponse;
import com.siginex.api.dto.kb.KbPreguntaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST layer for the read-only knowledge bank (KB). The KB is GLOBAL (no tenant,
 * no Row-Level Security), but the endpoints still require a valid session: they
 * fall under {@code anyRequest().authenticated()} of
 * {@link com.siginex.api.config.SecurityConfig}, so no new security rule is
 * needed. Holds no business logic; delegates to {@link KbDelegate}.
 */
@RestController
@RequestMapping("/api/v1/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbDelegate kbDelegate;

    @GetMapping("/modulos")
    public ResponseEntity<List<KbModuloResponse>> listarModulos() {
        List<KbModuloResponse> cuerpo = kbDelegate.listarModulos().stream()
                .map(KbModuloResponse::from)
                .toList();
        return ResponseEntity.ok(cuerpo);
    }

    @GetMapping("/modulos/{moduloId}/preguntas")
    public ResponseEntity<List<KbPreguntaResponse>> listarPreguntas(@PathVariable String moduloId) {
        List<KbPreguntaResponse> cuerpo = kbDelegate.listarPreguntas(moduloId).stream()
                .map(KbPreguntaResponse::from)
                .toList();
        return ResponseEntity.ok(cuerpo);
    }
}
