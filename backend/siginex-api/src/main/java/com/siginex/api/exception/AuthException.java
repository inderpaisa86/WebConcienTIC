package com.siginex.api.exception;

/**
 * Thrown when authentication cannot be granted. The message is intentionally
 * generic so the handler can return the same 401 whether the email exists or
 * not, the account is inactive, or it is temporarily locked (resistance to
 * account enumeration, decision D7).
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
