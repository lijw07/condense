package io.condense.subscriber;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SubscribeRequest(
        @Email @NotNull String email,
        List<String> tickers,
        Cadence cadence) {

    public SubscribeRequest {
        tickers = tickers == null ? List.of() : List.copyOf(tickers);
    }

    public Cadence cadenceOrDefault() {
        return cadence == null ? Cadence.DAILY : cadence;
    }
}
