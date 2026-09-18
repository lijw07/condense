package io.condense.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "company_ticker")
public class CompanyTicker {

    @Id
    @Column(length = 16)
    private String ticker;

    @Column(nullable = false, length = 10)
    private String cik;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected CompanyTicker() {
    }

    public CompanyTicker(String ticker, String cik) {
        this.ticker = ticker;
        this.cik = cik;
    }

    public String getTicker() {
        return ticker;
    }

    public String getCik() {
        return cik;
    }
}
