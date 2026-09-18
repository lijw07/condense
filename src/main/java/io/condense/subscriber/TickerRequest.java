package io.condense.subscriber;

import jakarta.validation.constraints.NotBlank;

public record TickerRequest(@NotBlank String ticker) {
}
