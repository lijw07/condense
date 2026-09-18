package io.condense.summarize;

import io.condense.filing.Significance;
import java.util.List;

public record SummaryResult(String model, String headline, List<String> bullets, Significance significance) {
}
