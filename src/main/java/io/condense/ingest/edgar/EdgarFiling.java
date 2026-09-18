package io.condense.ingest.edgar;

import java.time.LocalDate;

public record EdgarFiling(
        String cik,
        String accessionNumber,
        String formType,
        LocalDate filedOn,
        LocalDate periodEnd,
        String primaryDocumentUrl,
        String description) {
}
