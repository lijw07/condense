package io.condense.company;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyDirectory {

    private final CompanyRepository companies;
    private final CompanyTickerRepository tickers;

    public CompanyDirectory(CompanyRepository companies, CompanyTickerRepository tickers) {
        this.companies = companies;
        this.tickers = tickers;
    }

    @Transactional(readOnly = true)
    public Optional<Company> findByTicker(String ticker) {
        return tickers.findById(normalize(ticker))
                .map(CompanyTicker::getCik)
                .flatMap(companies::findById);
    }

    @Transactional(readOnly = true)
    public List<CompanySearchResult> search(String query, int limit) {
        String normalized = normalize(query);
        if (normalized.length() < 1) {
            return List.of();
        }
        return tickers.search(normalized, normalized + "%", "%" + normalized + "%", PageRequest.of(0, limit));
    }

    private String normalize(String ticker) {
        return ticker == null ? "" : ticker.trim().toUpperCase();
    }
}
