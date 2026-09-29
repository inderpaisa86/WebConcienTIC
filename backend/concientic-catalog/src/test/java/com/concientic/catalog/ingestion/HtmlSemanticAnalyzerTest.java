package com.concientic.catalog.ingestion;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSemanticAnalyzerTest {
    private final HtmlSemanticAnalyzer analyzer = new HtmlSemanticAnalyzer();

    @Test
    void acceptsARelevantLearningPageAndExtractsEvidence() throws Exception {
        var analysis = analyzer.analyze(
                URI.create("https://www.open.edu/openlearn/source"),
                URI.create("https://www.open.edu/openlearn/digital-skills"),
                fixture("openlearn-active.html"),
                "Digital skills: succeeding in a digital world");

        assertThat(analysis.status()).isEqualTo(AccessStatus.ACTIVE);
        assertThat(analysis.semanticMatch()).isTrue();
        assertThat(analysis.title()).contains("Digital skills");
        assertThat(analysis.canonicalUrl()).isEqualTo(URI.create("https://www.open.edu/openlearn/digital-computing/digital-skills-succeeding-digital-world"));
    }

    @Test
    void rejectsHttp200PageThatSaysTheResourceWasRemoved() {
        var analysis = analyzer.analyze(
                URI.create("https://www.open.edu/openlearn/source"),
                URI.create("https://www.open.edu/openlearn/source"),
                fixture("openlearn-removed.html"),
                null);

        assertThat(analysis.status()).isEqualTo(AccessStatus.REMOVED);
        assertThat(analysis.semanticMatch()).isFalse();
    }

    @Test
    void detectsPaymentSignalsWithoutCallingTheResourceFree() {
        var analysis = analyzer.analyze(
                URI.create("https://www.open.edu/openlearn/source"),
                URI.create("https://www.open.edu/openlearn/source"),
                fixture("openlearn-paid.html"),
                null);

        assertThat(analysis.requiresPayment()).isTrue();
        assertThat(analysis.semanticMatch()).isTrue();
    }

    private static String fixture(String name) {
        try (var stream = HtmlSemanticAnalyzerTest.class.getResourceAsStream("/fixtures/" + name)) {
            if (stream == null) throw new IllegalStateException("Missing fixture " + name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
