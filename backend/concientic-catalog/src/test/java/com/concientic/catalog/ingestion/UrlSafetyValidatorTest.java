package com.concientic.catalog.ingestion;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class UrlSafetyValidatorTest {

    @Test
    void rejectsLocalAndCredentialBearingUrls() {
        assertThatThrownBy(() -> UrlSafetyValidator.validate(URI.create("http://localhost:8080/internal")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UrlSafetyValidator.validate(URI.create("https://user:password@example.org/course")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsAResolvablePublicHttpsHost() {
        assertThatCode(() -> UrlSafetyValidator.validate(URI.create("https://www.open.edu/openlearn")))
                .doesNotThrowAnyException();
    }
}
