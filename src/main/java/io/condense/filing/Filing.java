package io.condense.filing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "filing")
public class Filing {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 10)
    private String cik;

    @Column(name = "accession_number", nullable = false, unique = true, length = 32)
    private String accessionNumber;

    @Column(name = "form_type", nullable = false, length = 16)
    private String formType;

    @Column(name = "filed_on", nullable = false)
    private LocalDate filedOn;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "primary_document_url", nullable = false)
    private String primaryDocumentUrl;

    @Column
    private String description;

    @Column(name = "discovered_at", nullable = false)
    private Instant discoveredAt = Instant.now();

    protected Filing() {
    }

    public Filing(String cik,
                  String accessionNumber,
                  String formType,
                  LocalDate filedOn,
                  LocalDate periodEnd,
                  String primaryDocumentUrl,
                  String description) {
        this.cik = cik;
        this.accessionNumber = accessionNumber;
        this.formType = formType;
        this.filedOn = filedOn;
        this.periodEnd = periodEnd;
        this.primaryDocumentUrl = primaryDocumentUrl;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public String getCik() {
        return cik;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public String getFormType() {
        return formType;
    }

    public LocalDate getFiledOn() {
        return filedOn;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public String getPrimaryDocumentUrl() {
        return primaryDocumentUrl;
    }

    public String getDescription() {
        return description;
    }
}
