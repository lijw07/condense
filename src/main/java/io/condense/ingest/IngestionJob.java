package io.condense.ingest;

import io.condense.filing.Filing;
import io.condense.ingest.edgar.FilingIngestService;
import io.condense.subscriber.SubscriptionRepository;
import io.condense.summarize.SummarizationService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IngestionJob {

    private static final Logger log = LoggerFactory.getLogger(IngestionJob.class);

    private final SubscriptionRepository subscriptions;
    private final FilingIngestService filingIngest;
    private final SummarizationService summarization;

    public IngestionJob(SubscriptionRepository subscriptions,
                        FilingIngestService filingIngest,
                        SummarizationService summarization) {
        this.subscriptions = subscriptions;
        this.filingIngest = filingIngest;
        this.summarization = summarization;
    }

    @Scheduled(cron = "${condense.schedule.ingest-cron}", zone = "America/New_York")
    public void run() {
        List<String> ciks = subscriptions.findAllTrackedCiks();
        log.info("Starting ingestion pass over {} tracked companies", ciks.size());
        for (String cik : ciks) {
            try {
                filingIngest.ingest(cik).forEach(this::summarizeQuietly);
            } catch (RuntimeException e) {
                log.error("Ingestion failed for CIK {}", cik, e);
            }
        }
    }

    private void summarizeQuietly(Filing filing) {
        try {
            summarization.summarize(filing);
        } catch (RuntimeException e) {
            log.error("Summarization failed for filing {}", filing.getAccessionNumber(), e);
        }
    }
}
