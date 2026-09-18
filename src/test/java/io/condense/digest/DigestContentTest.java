package io.condense.digest;

import static org.assertj.core.api.Assertions.assertThat;

import io.condense.filing.Significance;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class DigestContentTest {

    @Test
    void reportsEmptyWhenNoItems() {
        DigestContent content = new DigestContent(LocalDate.of(2026, 9, 18), List.of(), "https://example.test/m", "https://example.test/u");

        assertThat(content.isEmpty()).isTrue();
    }

    @Test
    void ordersMaterialFilingsAheadOfRoutineOnes() {
        DigestItem routine = item("AAPL", Significance.ROUTINE, LocalDate.of(2026, 9, 18));
        DigestItem material = item("MSFT", Significance.MATERIAL, LocalDate.of(2026, 9, 16));

        List<DigestItem> ordered = List.of(routine, material).stream()
                .sorted(Comparator
                        .comparing(DigestItem::significance, Comparator.reverseOrder())
                        .thenComparing(DigestItem::filedOn, Comparator.reverseOrder()))
                .toList();

        assertThat(ordered).first().isEqualTo(material);
    }

    private DigestItem item(String ticker, Significance significance, LocalDate filedOn) {
        return new DigestItem(
                ticker,
                ticker + " Inc.",
                "8-K",
                filedOn,
                "Something happened",
                List.of("A fact"),
                significance,
                "https://sec.gov/example");
    }
}
