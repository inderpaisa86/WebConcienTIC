package com.siginex.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Implementación de {@link EmailSender} con la API HTTP de Resend
 * ({@code POST https://api.resend.com/emails}). Se activa con
 * {@code siginex.email.provider=resend}. No usa SDK: una llamada HTTP con
 * {@link HttpClient} basta y evita dependencias nuevas.
 *
 * <p>Config requerida al activarla: {@code siginex.email.resend-api-key}
 * (secreto, por entorno) y {@code siginex.email.from} (remitente verificado).
 *
 * <p>Seguridad: el cuerpo del correo contiene el enlace con el token, pero NI el
 * token NI el enlace se loguean. Si el envío falla, se registra el fallo sin el
 * enlace y se propaga para que el delegate decida (el flujo de solicitud no
 * revela el fallo al cliente para no romper la no-enumeración).
 */
@Service
@ConditionalOnProperty(name = "siginex.email.provider", havingValue = "resend")
@Slf4j
public class ResendEmailSender implements EmailSender {

    private static final URI RESEND_ENDPOINT = URI.create("https://api.resend.com/emails");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${siginex.email.resend-api-key:}")
    private String apiKey;

    @Value("${siginex.email.from:}")
    private String from;

    @Override
    public void enviarRecuperacion(String destinatario, String enlaceReset) {
        if (apiKey == null || apiKey.isBlank() || from == null || from.isBlank()) {
            // No hay credenciales: no revientes el flujo, pero deja constancia
            // (sin el enlace). El operador debe configurar la key y el from.
            log.warn("[email:resend] proveedor activado sin api-key/from; no se envio a {}", destinatario);
            return;
        }

        String html = """
                <h2>Recuperacion de contrasena - SIGINEX</h2>
                <p>Has solicitado restablecer tu contrasena. El enlace caduca pronto y es de un solo uso.</p>
                <p><a href="%s">Restablecer mi contrasena</a></p>
                <p>Si no lo solicitaste, ignora este correo.</p>
                """.formatted(enlaceReset);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "from", from,
                    "to", new String[] {destinatario},
                    "subject", "Recuperacion de contrasena - SIGINEX",
                    "html", html));
        } catch (Exception e) {
            throw new IllegalStateException("no se pudo serializar el correo de recuperacion", e);
        }

        HttpRequest request = HttpRequest.newBuilder(RESEND_ENDPOINT)
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                // No logueamos el enlace; solo el código de estado.
                log.warn("[email:resend] envio a {} devolvio HTTP {}", destinatario, response.statusCode());
                throw new IllegalStateException("resend respondio " + response.statusCode());
            }
            log.info("[email:resend] correo de recuperacion enviado a {}", destinatario);
        } catch (java.io.IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("fallo al enviar el correo de recuperacion", e);
        }
    }
}
