package io.condense.ingest.edgar;

import java.util.List;

public record EdgarSubmissions(String cik, String name, Filings filings) {

    public record Filings(Recent recent) {
    }

    public record Recent(
            List<String> accessionNumber,
            List<String> filingDate,
            List<String> reportDate,
            List<String> form,
            List<String> primaryDocument,
            List<String> primaryDocDescription) {
    }
}
