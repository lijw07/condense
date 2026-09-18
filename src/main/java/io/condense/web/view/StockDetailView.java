package io.condense.web.view;

import java.util.List;

public record StockDetailView(String ticker,
                              String name,
                              BriefView brief,
                              List<FilingHistoryEntry> filings) {

    public boolean hasBrief() {
        return brief != null;
    }
}
