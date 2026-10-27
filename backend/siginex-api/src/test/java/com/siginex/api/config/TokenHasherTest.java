package com.siginex.api.config;


import com.siginex.api.config.TokenHasher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenHasherTest {

    private final TokenHasher hasher = new TokenHasher();

    @Test
    void producesKnownStableSha256Hex() {
        // SHA-256("abc") is a well-known test vector.
        assertThat(hasher.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void sameInputYieldsSameHash() {
        assertThat(hasher.hash("token-value")).isEqualTo(hasher.hash("token-value"));
    }

    @Test
    void rejectsBlankToken() {
        assertThatThrownBy(() -> hasher.hash("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> hasher.hash(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
