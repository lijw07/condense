package io.condense.subscriber;

import java.util.List;

public record SubscriberView(String email, String status, String cadence, List<String> tickers) {
}
