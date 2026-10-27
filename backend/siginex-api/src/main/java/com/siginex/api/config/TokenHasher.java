package com.siginex.api.config;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes opaque session tokens with SHA-256 and returns a lowercase hex string.
 *
 * <p>The opaque token is high-entropy random data, so a fast cryptographic hash
 * is appropriate (no need for a password KDF). Only the hash is persisted and
 * looked up; the plaintext token lives only in the client cookie and is never
 * stored or logged.
 */
@Component
public class TokenHasher {

    public String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token is required");
        }
        MessageDigest digest = newDigest();
        byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return toHex(bytes);
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16));
            hex.append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }
}
