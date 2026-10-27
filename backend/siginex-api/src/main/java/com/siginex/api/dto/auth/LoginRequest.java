package com.siginex.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login request body. Only format validation lives here (controller concern);
 * business rules are the delegate's job.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
