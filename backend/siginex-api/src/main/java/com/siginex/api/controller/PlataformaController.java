package com.siginex.api.controller;

import com.siginex.api.delegate.PlataformaDelegate;
import com.siginex.api.domain.EmpresaCreada;
import com.siginex.api.dto.plataforma.CrearEmpresaRequest;
import com.siginex.api.dto.plataforma.EmpresaCreadaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Capa REST del alta de empresa (Fase 5, operación de PLATAFORMA). Valida el
 * formato, pasa la clave de plataforma de la cabecera al delegate y devuelve
 * los identificadores creados. Sin lógica de negocio.
 *
 * <p>Esta ruta NO usa sesión de tenant: se autoriza con la cabecera
 * {@code X-Siginex-Admin-Key} (secreto de operador), que el delegate valida en
 * tiempo constante. Es pública en {@code SecurityConfig} (no hay sesión) y con
 * CSRF ignorado, como {@code POST /auth/login}.
 */
@RestController
@RequestMapping("/api/v1/plataforma")
@RequiredArgsConstructor
public class PlataformaController {

    static final String ADMIN_KEY_HEADER = "X-Siginex-Admin-Key";

    private final PlataformaDelegate plataformaDelegate;

    @PostMapping("/empresas")
    public ResponseEntity<EmpresaCreadaResponse> crearEmpresa(
            @RequestHeader(value = ADMIN_KEY_HEADER, required = false) String adminKey,
            @Valid @RequestBody CrearEmpresaRequest request) {
        EmpresaCreada creada = plataformaDelegate.crearEmpresa(adminKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmpresaCreadaResponse.from(creada));
    }
}
