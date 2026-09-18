package io.condense.ingest.edgar;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompanyTickerEntry(
        @JsonProperty("cik_str") long cikStr,
        String ticker,
        String title) {

    public String paddedCik() {
        return "%010d".formatted(cikStr);
    }
}
