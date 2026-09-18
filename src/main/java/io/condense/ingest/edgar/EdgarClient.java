package io.condense.ingest.edgar;

import io.condense.config.EdgarProperties;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EdgarClient {

    private static final String ARCHIVE_ROOT = "https://www.sec.gov/Archives/edgar/data";
    private static final String COMPANY_TICKERS_URL = "https://www.sec.gov/files/company_tickers.json";

    private final RestClient restClient;
    private final EdgarProperties properties;
    private final RateLimitedGate gate;

    public EdgarClient(RestClient edgarRestClient, EdgarProperties properties) {
        this.restClient = edgarRestClient;
        this.properties = properties;
        this.gate = new RateLimitedGate(properties.minimumRequestInterval());
    }

    public List<EdgarFiling> recentFilings(String cik) {
        gate.await();
        EdgarSubmissions submissions = restClient.get()
                .uri("/submissions/CIK{cik}.json", cik)
                .retrieve()
                .body(EdgarSubmissions.class);
        if (submissions == null || submissions.filings() == null || submissions.filings().recent() == null) {
            return List.of();
        }
        return toFilings(cik, submissions.filings().recent());
    }

    public Map<String, CompanyTickerEntry> companyTickers() {
        gate.await();
        Map<String, CompanyTickerEntry> entries = restClient.get()
                .uri(COMPANY_TICKERS_URL)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return entries == null ? Map.of() : entries;
    }

    public String fetchDocument(String url) {
        gate.await();
        String body = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
        return body == null ? "" : body;
    }

    private List<EdgarFiling> toFilings(String cik, EdgarSubmissions.Recent recent) {
        List<EdgarFiling> filings = new ArrayList<>();
        int count = recent.accessionNumber() == null ? 0 : recent.accessionNumber().size();
        for (int index = 0; index < count; index++) {
            String form = valueAt(recent.form(), index);
            if (!properties.trackedForms().contains(form)) {
                continue;
            }
            String accessionNumber = recent.accessionNumber().get(index);
            filings.add(new EdgarFiling(
                    cik,
                    accessionNumber,
                    form,
                    parseDate(valueAt(recent.filingDate(), index)),
                    parseDate(valueAt(recent.reportDate(), index)),
                    documentUrl(cik, accessionNumber, valueAt(recent.primaryDocument(), index)),
                    valueAt(recent.primaryDocDescription(), index)));
        }
        return filings;
    }

    private String documentUrl(String cik, String accessionNumber, String primaryDocument) {
        return "%s/%s/%s/%s".formatted(
                ARCHIVE_ROOT,
                Long.parseLong(cik),
                accessionNumber.replace("-", ""),
                primaryDocument);
    }

    private String valueAt(List<String> values, int index) {
        if (values == null || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}
