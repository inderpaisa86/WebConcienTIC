package com.concientic.catalog.ingestion;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LinkVerificationAgentTest {
    private HttpServer server;
    private LinkVerificationAgent verifier;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/active", exchange -> respond(exchange, 200, fixture("openlearn-active.html")));
        server.createContext("/paid", exchange -> respond(exchange, 200, fixture("openlearn-paid.html")));
        server.createContext("/redirect", exchange -> {
            exchange.getResponseHeaders().add("Location", "/active");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
        verifier = new LinkVerificationAgent(
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build(),
                Duration.ofSeconds(5),
                100_000,
                true);
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void followsRedirectsAndMarksChangedUrl() {
        var candidate = candidate("/redirect");

        var result = verifier.verify(candidate);

        assertThat(result.accessStatus()).isEqualTo(AccessStatus.LINK_CHANGED);
        assertThat(result.finalUrl().getPath()).isEqualTo("/active");
        assertThat(result.semanticMatch()).isTrue();
        assertThat(result.isPublishableAccess()).isTrue();
    }

    @Test
    void detectsPaymentEvenWhenHttpIsSuccessful() {
        var result = verifier.verify(candidate("/paid"));

        assertThat(result.httpStatus()).isEqualTo(200);
        assertThat(result.requiresPayment()).isTrue();
        assertThat(result.isPublishableAccess()).isFalse();
    }

    private ResourceCandidate candidate(String path) {
        return new ResourceCandidate(null, "fixture", URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path), "Fixture", "other", null, java.util.List.of());
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String fixture(String name) {
        try (var stream = LinkVerificationAgentTest.class.getResourceAsStream("/fixtures/" + name)) {
            if (stream == null) throw new IllegalStateException("Missing fixture " + name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
