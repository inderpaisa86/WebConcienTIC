package com.siginex.api.delegate;

import com.siginex.api.domain.EmpresaCreada;
import com.siginex.api.domain.Usuario;
import com.siginex.api.dto.plataforma.CrearEmpresaRequest;
import com.siginex.api.exception.PlataformaException;
import com.siginex.api.service.AuditoriaService;
import com.siginex.api.service.PlataformaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementación por defecto de {@link PlataformaDelegate}.
 *
 * <p>Autorización por clave de plataforma: se compara la clave recibida con la
 * esperada (config {@code siginex.platform.admin-key}) usando
 * {@link MessageDigest#isEqual} sobre bytes UTF-8, que es de tiempo constante,
 * para no filtrar la longitud ni el prefijo por temporización. Si la clave
 * esperada no está configurada, el alta se rechaza con 503 (no se crea un
 * agujero con clave vacía).
 *
 * <p>Reglas de negocio: slug único derivado del nombre (con sufijo aleatorio si
 * colisiona), email único GLOBAL (409), política de contraseña mínima 12. La
 * contraseña se hashea con {@link PasswordEncoder} (Argon2id) ANTES de bajar al
 * service; la contraseña en claro y el hash nunca se loguean ni se auditan.
 */
@Component
@RequiredArgsConstructor
public class PlataformaDelegateImpl implements PlataformaDelegate {

    private static final String ROL_ADMIN = "admin";
    private static final int MIN_PASSWORD = 12;
    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PlataformaService plataformaService;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    /**
     * Clave de plataforma esperada. Vacía por defecto: sin configurarla el alta
     * queda deshabilitada (503), nunca abierta.
     */
    @Value("${siginex.platform.admin-key:}")
    private String adminKeyEsperada;

    @Override
    public EmpresaCreada crearEmpresa(String clavePlataforma, CrearEmpresaRequest request) {
        exigirClavePlataforma(clavePlataforma);

        // Política de contraseña (defensa en profundidad; el DTO ya valida @Size).
        if (request.adminPassword() == null || request.adminPassword().length() < MIN_PASSWORD) {
            throw new PlataformaException(HttpStatus.BAD_REQUEST,
                    "la contrasena debe tener al menos " + MIN_PASSWORD + " caracteres");
        }

        if (plataformaService.existeEmailGlobal(request.adminEmail())) {
            throw new PlataformaException(HttpStatus.CONFLICT, "el email ya esta registrado");
        }

        String slug = resolverSlugUnico(request.empresaNombre());

        // Hash Argon2id del admin ANTES de bajar al service; nunca se loguea.
        Usuario adminAInsertar = new Usuario(
                null,
                null, // el tenantId lo fija el service con el tenant recién creado
                request.adminEmail(),
                request.adminNombre(),
                passwordEncoder.encode(request.adminPassword()),
                ROL_ADMIN,
                true,
                null);

        EmpresaCreada creada = plataformaService.crearEmpresaConAdmin(
                request.empresaNombre(), slug, adminAInsertar);

        // Auditoría sin secretos (el AuditoriaService además rechaza claves
        // sensibles). tenantId del nuevo tenant; actor = "plataforma".
        auditoriaService.registrar(creada.getTenantId(), "plataforma", "empresa_creada", "tenant",
                creada.getTenantId().toString(),
                Map.of("empresa", creada.getTenantNombre(), "slug", creada.getTenantSlug(),
                        "admin_email", creada.getAdminEmail()));

        return creada;
    }

    /** Comparación en tiempo constante; 401 si no coincide, 503 si no hay clave configurada. */
    private void exigirClavePlataforma(String clavePlataforma) {
        if (adminKeyEsperada == null || adminKeyEsperada.isBlank()) {
            throw new PlataformaException(HttpStatus.SERVICE_UNAVAILABLE,
                    "el alta de empresas no esta habilitada");
        }
        byte[] esperada = adminKeyEsperada.getBytes(StandardCharsets.UTF_8);
        byte[] recibida = (clavePlataforma == null ? "" : clavePlataforma).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(esperada, recibida)) {
            throw new PlataformaException(HttpStatus.UNAUTHORIZED, "no autorizado");
        }
    }

    /** Deriva un slug del nombre y, si ya existe, añade un sufijo aleatorio corto. */
    private String resolverSlugUnico(String nombre) {
        String base = slugify(nombre);
        if (base.isBlank()) {
            base = "empresa";
        }
        String candidato = base;
        for (int intento = 0; intento < 5; intento++) {
            if (!plataformaService.existeSlug(candidato)) {
                return candidato;
            }
            candidato = base + "-" + sufijoAleatorio();
        }
        // Último recurso: base + sufijo largo (altísima probabilidad de unicidad).
        return base + "-" + sufijoAleatorio() + sufijoAleatorio();
    }

    private static String slugify(String texto) {
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = NON_SLUG.matcher(normalizado.toLowerCase(Locale.ROOT)).replaceAll("-");
        return slug.replaceAll("(^-+)|(-+$)", "");
    }

    private static String sufijoAleatorio() {
        int valor = SECURE_RANDOM.nextInt(0x10000); // 0..65535
        return Integer.toHexString(valor);
    }
}
