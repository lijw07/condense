package io.condense.ingest.edgar;

import io.condense.filing.Filing;
import io.condense.filing.FilingRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilingIngestService {

    private static final Logger log = LoggerFactory.getLogger(FilingIngestService.class);

    private final EdgarClient edgarClient;
    private final FilingRepository filings;

    public FilingIngestService(EdgarClient edgarClient, FilingRepository filings) {
        this.edgarClient = edgarClient;
        this.filings = filings;
    }

    @Transactional
    public List<Filing> ingest(String cik) {
        List<Filing> created = new ArrayList<>();
        for (EdgarFiling candidate : edgarClient.recentFilings(cik)) {
            if (filings.existsByAccessionNumber(candidate.accessionNumber())) {
                continue;
            }
            created.add(filings.save(new Filing(
                    candidate.cik(),
                    candidate.accessionNumber(),
                    candidate.formType(),
                    candidate.filedOn(),
                    candidate.periodEnd(),
                    candidate.primaryDocumentUrl(),
                    candidate.description())));
        }
        if (!created.isEmpty()) {
            log.info("Ingested {} new filings for CIK {}", created.size(), cik);
        }
        return created;
    }
}
