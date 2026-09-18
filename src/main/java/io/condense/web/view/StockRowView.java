package io.condense.web.view;

public record StockRowView(String ticker,
                           String name,
                           boolean hasFilings,
                           String lastFormType,
                           String lastFiledOn,
                           LatestBriefView latestBrief) {

    public boolean hasLatestBrief() {
        return latestBrief != null;
    }

    public String panelId() {
        return "panel-" + ticker.toLowerCase().replace('.', '-');
    }
}
