package io.condense.digest;

import io.condense.filing.Significance;
import java.time.LocalDate;
import java.util.List;

public record DigestItem(
        String ticker,
        String companyName,
        String formType,
        LocalDate filedOn,
        String headline,
        List<String> bullets,
        Significance significance,
        String sourceUrl) {
}
