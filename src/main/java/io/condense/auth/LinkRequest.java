package io.condense.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LinkRequest(@Email @NotNull String email) {
}
