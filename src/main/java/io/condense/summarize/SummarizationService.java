package io.condense.summarize;

import io.condense.company.Company;
import io.condense.company.CompanyRepository;
import io.condense.filing.Filing;
import io.condense.filing.FilingSummary;
import io.condense.filing.FilingSummaryRepository;
import io.condense.ingest.edgar.DocumentTextExtractor;
import io.condense.ingest.edgar.EdgarClient;
import io.condense.config.EdgarProperties;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SummarizationService {

    private static final Logger log = LoggerFactory.getLogger(SummarizationService.class);

    private final EdgarClient edgarClient;
    private final DocumentTextExtractor textExtractor;
    private final Summarizer summarizer;
    private final FilingSummaryRepository summaries;
    private final CompanyRepository companies;
    private final EdgarProperties edgarProperties;

    public SummarizationService(EdgarClient edgarClient,
                                DocumentTextExtractor textExtractor,
                                Summarizer summarizer,
                                FilingSummaryRepository summaries,
                                CompanyRepository companies,
                                EdgarProperties edgarProperties) {
        this.edgarClient = edgarClient;
        this.textExtractor = textExtractor;
        this.summarizer = summarizer;
        this.summaries = summaries;
        this.companies = companies;
        this.edgarProperties = edgarProperties;
    }

    @Transactional
    public Optional<FilingSummary> summarize(Filing filing) {
        Optional<FilingSummary> existing = summaries.findByFilingId(filing.getId());
        if (existing.isPresent()) {
            return existing;
        }
        String documentText = textExtractor.extract(
                edgarClient.fetchDocument(filing.getPrimaryDocumentUrl()),
                edgarProperties.maxDocumentChars());
        if (documentText.isBlank()) {
            log.warn("No extractable text for filing {}", filing.getAccessionNumber());
            return Optional.empty();
        }
        Company company = companies.findById(filing.getCik()).orElse(null);
        SummaryResult result = summarizer.summarize(new SummaryRequest(
                company == null ? filing.getCik() : company.getTicker(),
                company == null ? "" : company.getName(),
                filing.getFormType(),
                filing.getFiledOn().toString(),
                documentText));
        return Optional.of(summaries.save(new FilingSummary(
                filing,
                result.model(),
                result.headline(),
                result.bullets(),
                result.significance(),
                documentText.length())));
    }
}
