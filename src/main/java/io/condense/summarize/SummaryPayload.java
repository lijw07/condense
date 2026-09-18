package io.condense.summarize;

import java.util.List;

public record SummaryPayload(String headline, List<String> bullets, String significance) {
}
