package com.siginex.api.delegate;

import com.siginex.api.domain.EmpresaCreada;
import com.siginex.api.dto.plataforma.CrearEmpresaRequest;

/**
 * Reglas de negocio y autorización del alta de empresa (Fase 5, operación de
 * plataforma). El controller delega aquí tras la validación de formato; esta
 * capa decide si la operación procede y luego entrega el trabajo de datos al
 * {@link com.siginex.api.service.PlataformaService}. Implementado por
 * {@link PlataformaDelegateImpl}.
 *
 * <p>Autorización: el alta NO usa sesión de tenant. Se autoriza con una CLAVE
 * DE PLATAFORMA (secreto de operador) que el controller recibe por cabecera y
 * pasa aquí; el delegate la compara en tiempo constante con la clave esperada
 * (config). Sin clave válida: 401. Sin clave configurada en el servidor: 503.
 */
public interface PlataformaDelegate {

    /**
     * Crea un tenant (empresa) y su primer usuario admin.
     *
     * @param clavePlataforma valor de la cabecera {@code X-Siginex-Admin-Key}
     *                        (puede ser null si la cabecera no vino).
     * @param request         datos de la empresa y del admin (formato ya validado).
     */
    EmpresaCreada crearEmpresa(String clavePlataforma, CrearEmpresaRequest request);
}
