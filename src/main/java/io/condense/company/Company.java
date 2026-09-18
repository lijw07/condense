package io.condense.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "company")
public class Company {

    @Id
    @Column(length = 10)
    private String cik;

    @Column(nullable = false, unique = true, length = 16)
    private String ticker;

    @Column(nullable = false)
    private String name;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Company() {
    }

    public Company(String cik, String ticker, String name) {
        this.cik = cik;
        this.ticker = ticker;
        this.name = name;
    }

    public String getCik() {
        return cik;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public void rename(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }
}
