package io.condense.subscriber;

import jakarta.validation.constraints.NotNull;

public record CadenceRequest(@NotNull Cadence cadence) {
}
