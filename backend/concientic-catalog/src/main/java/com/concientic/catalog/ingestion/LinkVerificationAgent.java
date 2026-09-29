package com.concientic.catalog.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Service
public class LinkVerificationAgent implements ResourceVerifier {
    private final HttpClient httpClient;
    private final Duration requestTimeout;
    private final int maxBodyBytes;
    private final boolean allowLocalAddresses;
    private final HtmlSemanticAnalyzer analyzer = new HtmlSemanticAnalyzer();

    public LinkVerificationAgent(
            @Value("${concientic.verification.timeout-seconds:15}") long timeoutSeconds,
            @Value("${concientic.verification.max-body-bytes:1000000}") int maxBodyBytes) {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), Duration.ofSeconds(timeoutSeconds), maxBodyBytes, false);
    }

    LinkVerificationAgent(HttpClient httpClient, Duration requestTimeout, int maxBodyBytes, boolean allowLocalAddresses) {
        this.httpClient = httpClient;
        this.requestTimeout = requestTimeout;
        this.maxBodyBytes = maxBodyBytes;
        this.allowLocalAddresses = allowLocalAddresses;
    }

    @Override
    public VerificationResult verify(ResourceCandidate candidate) {
        URI requestedUrl = candidate.sourceUrl();
        Instant started = Instant.now();
        try {
            UrlSafetyValidator.validate(requestedUrl, allowLocalAddresses);
            HttpRequest request = HttpRequest.newBuilder(requestedUrl)
                    .timeout(requestTimeout)
                    .header("Accept", "text/html,application/xhtml+xml")
                    .header("User-Agent", "ConcienTIC-Learning-Intelligence/0.1 (+https://concientic.com)")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            long elapsed = Duration.between(started, Instant.now()).toMillis();
            if (response.body().length > maxBodyBytes) {
                return failure(requestedUrl, response.uri(), response.statusCode(), elapsed, "BODY_TOO_LARGE", "Response exceeds configured body limit");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 400) {
                AccessStatus status = response.statusCode() == 404 || response.statusCode() == 410 ? AccessStatus.REMOVED : AccessStatus.TEMPORARILY_UNAVAILABLE;
                return new VerificationResult(status, requestedUrl, response.uri(), null, response.statusCode(), elapsed, null, null, false, false, false, Instant.now(), "HTTP status " + response.statusCode(), "HTTP_STATUS", "Resource did not return a successful HTTP response");
            }
            String contentType = response.headers().firstValue("content-type").orElse("").toLowerCase();
            if (!contentType.isBlank() && !contentType.contains("text/html") && !contentType.contains("application/xhtml+xml")) {
                return failure(requestedUrl, response.uri(), response.statusCode(), elapsed, "UNSUPPORTED_CONTENT_TYPE", "Resource is not HTML");
            }
            String html = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
            HtmlSemanticAnalyzer.Analysis analysis = analyzer.analyze(requestedUrl, response.uri(), html, candidate.expectedTitle());
            AccessStatus status = response.uri().equals(requestedUrl) ? analysis.status() : analysis.status() == AccessStatus.ACTIVE ? AccessStatus.LINK_CHANGED : analysis.status();
            return new VerificationResult(status, requestedUrl, response.uri(), analysis.canonicalUrl(), response.statusCode(), elapsed, analysis.title(), analysis.description(), analysis.semanticMatch(), analysis.requiresAccount(), analysis.requiresPayment(), Instant.now(), "HTTP and semantic verification completed", null, null);
        } catch (IllegalArgumentException exception) {
            return failure(requestedUrl, requestedUrl, null, elapsed(started), "UNSAFE_URL", exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return failure(requestedUrl, requestedUrl, null, elapsed(started), "INTERRUPTED", "Verification was interrupted");
        } catch (IOException | RuntimeException exception) {
            return failure(requestedUrl, requestedUrl, null, elapsed(started), "REQUEST_FAILED", exception.getMessage());
        }
    }

    private VerificationResult failure(URI requested, URI finalUrl, Integer status, long elapsed, String code, String message) {
        AccessStatus accessStatus = "UNSAFE_URL".equals(code) ? AccessStatus.BLOCKED : AccessStatus.TEMPORARILY_UNAVAILABLE;
        return new VerificationResult(accessStatus, requested, finalUrl, null, status, elapsed, null, null, false, false, false, Instant.now(), message, code, message);
    }

    private static long elapsed(Instant started) {
        return Duration.between(started, Instant.now()).toMillis();
    }
}
