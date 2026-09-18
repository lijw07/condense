package io.condense.filing;

import io.condense.support.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "filing_summary")
public class FilingSummary {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filing_id", nullable = false, unique = true)
    private Filing filing;

    @Column(nullable = false, length = 64)
    private String model;

    @Column(nullable = false)
    private String headline;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false)
    private List<String> bullets;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Significance significance;

    @Column(name = "source_chars", nullable = false)
    private int sourceChars;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected FilingSummary() {
    }

    public FilingSummary(Filing filing,
                         String model,
                         String headline,
                         List<String> bullets,
                         Significance significance,
                         int sourceChars) {
        this.filing = filing;
        this.model = model;
        this.headline = headline;
        this.bullets = bullets;
        this.significance = significance;
        this.sourceChars = sourceChars;
    }

    public Filing getFiling() {
        return filing;
    }

    public String getHeadline() {
        return headline;
    }

    public List<String> getBullets() {
        return List.copyOf(bullets);
    }

    public Significance getSignificance() {
        return significance;
    }

    public String getModel() {
        return model;
    }
}
