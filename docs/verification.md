# Políticas de confianza, gratuidad y verificación

## 1. Confianza de fuentes

| Nivel | Fuente | Publicación automática |
|---|---|---|
| A | Gobierno, universidad, organismo internacional, empresa responsable del contenido | Sí, si el recurso supera las demás verificaciones |
| B | Plataforma educativa reconocida con contenido verificable | Sí, con evidencia de proveedor y acceso |
| C | ONG, fundación, asociación o institución especializada | Revisión de calidad recomendada |
| D | Fuente secundaria | Normalmente `REVIEW_REQUIRED` |

La marca de una organización no basta para publicar. El recurso individual debe verificarse.

Se rechazan como fuente primaria blogs SEO, afiliados, agregadores desconocidos, copias sin atribución y contenido generado sin autoría verificable.

## 2. Gratuidad

Estados exactos:

```text
FREE
FREE_CONTENT_PAID_CERTIFICATE
AUDIT_FREE
PARTIAL_FREE
SCHOLARSHIP
TRIAL
PAID
UNKNOWN
```

Reglas:

- `FREE`: contenido principal accesible sin pago.
- `FREE_CONTENT_PAID_CERTIFICATE`: contenido accesible; certificado de pago.
- `AUDIT_FREE`: modalidad de auditoría gratuita claramente identificada.
- `PARTIAL_FREE`: solo una parte es gratuita; publicar solo con explicación explícita.
- `SCHOLARSHIP`: acceso condicionado a beca; no etiquetar como gratuito universal.
- `TRIAL`: prueba temporal; no es gratuito.
- `PAID`: excluir.
- `UNKNOWN`: revisión obligatoria.

Todo recurso debe tener `free_explanation` con evidencia y fecha. Nunca inferir gratuidad por el título, la marca o un resultado de buscador.

## 3. Verificación HTTP

Para cada URL:

1. aceptar solamente HTTP/HTTPS;
2. bloquear localhost, redes privadas, metadata services y esquemas peligrosos;
3. limitar redirects, tamaño, tiempo y concurrencia;
4. comprobar SSL/certificado en el cliente HTTP;
5. registrar código, tiempo, URL final y canonical;
6. conservar la URL previa si cambia;
7. respetar robots, términos y rate limits.

Un `200` no prueba que el recurso exista. La respuesta pasa a verificación semántica.

## 4. Verificación semántica

Debe comprobarse que la página final:

- corresponde al proveedor esperado;
- contiene título o identificador compatible;
- describe el recurso esperado;
- no es una página de error con HTTP 200;
- no indica retiro, archivado o pago obligatorio;
- muestra idioma, modalidad o duración solo cuando están presentes;
- coincide con la URL y el candidato;
- permite acceso en las condiciones declaradas.

Si existe contenido contradictorio, el campo debe quedar sin confirmar y el recurso pasa a revisión.

## 5. Estados

```text
DISCOVERED
UNDER_REVIEW
VERIFIED
ACTIVE
UPDATED
LINK_CHANGED
TEMPORARILY_UNAVAILABLE
REMOVED
PAYMENT_REQUIRED
DUPLICATE
REJECTED
REVIEW_REQUIRED
```

Reglas de transición:

- Un error aislado no convierte `ACTIVE` en `REMOVED`.
- Varios fallos confirmados pueden producir `TEMPORARILY_UNAVAILABLE` y luego `REVIEW_REQUIRED`.
- Una página retirada explícitamente puede producir `REMOVED` de inmediato.
- Un cambio de URL conserva `url_previous` y registra `LINK_CHANGED`.
- Un cambio de precio produce `PRICE_CHANGED` en historial y puede retirar la tarjeta.

## 6. Evidencia

Cada decisión importante conserva:

- URL observada.
- Fecha/hora UTC.
- Tipo de evidencia.
- Resumen factual.
- Hash de metadatos, cuando corresponda.
- Agente y versión de política que tomó la decisión.

No almacenar el contenido protegido completo; guardar el mínimo necesario para auditar.
