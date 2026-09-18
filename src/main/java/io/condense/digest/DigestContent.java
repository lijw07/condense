package io.condense.digest;

import java.time.LocalDate;
import java.util.List;

public record DigestContent(LocalDate digestDate, List<DigestItem> items, String manageUrl, String unsubscribeUrl) {

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
