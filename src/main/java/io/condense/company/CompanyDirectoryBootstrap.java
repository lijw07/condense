package io.condense.company;

import io.condense.ingest.edgar.CompanyTickerEntry;
import io.condense.ingest.edgar.EdgarClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "condense.edgar.bootstrap-companies", havingValue = "true", matchIfMissing = true)
public class CompanyDirectoryBootstrap {

    private static final Logger log = LoggerFactory.getLogger(CompanyDirectoryBootstrap.class);

    private final CompanyRepository companies;
    private final CompanyTickerRepository tickers;
    private final EdgarClient edgarClient;

    public CompanyDirectoryBootstrap(CompanyRepository companies,
                                     CompanyTickerRepository tickers,
                                     EdgarClient edgarClient) {
        this.companies = companies;
        this.tickers = tickers;
        this.edgarClient = edgarClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadIfEmpty() {
        if (companies.count() > 0) {
            return;
        }
        try {
            load();
        } catch (RuntimeException e) {
            log.error("Company directory bootstrap failed; tickers will not resolve until it succeeds", e);
        }
    }

    @Transactional
    public void load() {
        Collection<CompanyTickerEntry> entries = usableEntries();
        companies.saveAll(primaryListings(entries));
        tickers.saveAll(allTickers(entries));
        log.info("Loaded {} companies and {} tickers from the SEC directory",
                companies.count(), tickers.count());
    }

    private Collection<CompanyTickerEntry> usableEntries() {
        return edgarClient.companyTickers().values().stream()
                .filter(this::isUsable)
                .toList();
    }

    private List<Company> primaryListings(Collection<CompanyTickerEntry> entries) {
        Map<String, Company> byCik = new LinkedHashMap<>();
        for (CompanyTickerEntry entry : entries) {
            byCik.putIfAbsent(
                    entry.paddedCik(),
                    new Company(entry.paddedCik(), normalize(entry.ticker()), entry.title()));
        }
        return List.copyOf(byCik.values());
    }

    private List<CompanyTicker> allTickers(Collection<CompanyTickerEntry> entries) {
        Map<String, CompanyTicker> byTicker = new LinkedHashMap<>();
        for (CompanyTickerEntry entry : entries) {
            byTicker.putIfAbsent(
                    normalize(entry.ticker()),
                    new CompanyTicker(normalize(entry.ticker()), entry.paddedCik()));
        }
        return new ArrayList<>(byTicker.values());
    }

    private boolean isUsable(CompanyTickerEntry entry) {
        return entry.ticker() != null
                && !entry.ticker().isBlank()
                && entry.ticker().length() <= 16
                && entry.title() != null;
    }

    private String normalize(String ticker) {
        return ticker.trim().toUpperCase();
    }
}
