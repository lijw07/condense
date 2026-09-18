package io.condense.digest;

import io.condense.company.Company;
import io.condense.company.CompanyRepository;
import io.condense.config.DigestProperties;
import io.condense.filing.Filing;
import io.condense.filing.FilingRepository;
import io.condense.filing.FilingSummary;
import io.condense.filing.FilingSummaryRepository;
import io.condense.subscriber.Subscriber;
import io.condense.subscriber.Subscription;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DigestBuilder {

    private final FilingRepository filings;
    private final FilingSummaryRepository summaries;
    private final CompanyRepository companies;
    private final DigestProperties properties;

    public DigestBuilder(FilingRepository filings,
                         FilingSummaryRepository summaries,
                         CompanyRepository companies,
                         DigestProperties properties) {
        this.filings = filings;
        this.summaries = summaries;
        this.companies = companies;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public DigestContent build(Subscriber subscriber, LocalDate digestDate, LocalDate since, String manageUrl) {
        Set<String> ciks = subscriber.getSubscriptions().stream()
                .map(Subscription::getCik)
                .collect(Collectors.toSet());
        if (ciks.isEmpty()) {
            return new DigestContent(digestDate, List.of(), manageUrl, unsubscribeUrl(subscriber));
        }

        List<Filing> candidates =
                filings.findAllByCikInAndFiledOnGreaterThanEqualOrderByFiledOnDesc(ciks, since);
        Map<UUID, FilingSummary> summaryByFiling = summaries
                .findAllByFilingIdIn(candidates.stream().map(Filing::getId).toList())
                .stream()
                .collect(Collectors.toMap(summary -> summary.getFiling().getId(), Function.identity()));

        List<DigestItem> items = candidates.stream()
                .filter(filing -> summaryByFiling.containsKey(filing.getId()))
                .map(filing -> toItem(filing, summaryByFiling.get(filing.getId())))
                .sorted(byImportance())
                .limit(properties.maxFilingsPerDigest())
                .toList();

        return new DigestContent(digestDate, items, manageUrl, unsubscribeUrl(subscriber));
    }

    private Comparator<DigestItem> byImportance() {
        return Comparator
                .comparing(DigestItem::significance, Comparator.reverseOrder())
                .thenComparing(DigestItem::filedOn, Comparator.reverseOrder());
    }

    private DigestItem toItem(Filing filing, FilingSummary summary) {
        Company company = companies.findById(filing.getCik()).orElse(null);
        return new DigestItem(
                company == null ? filing.getCik() : company.getTicker(),
                company == null ? "" : company.getName(),
                filing.getFormType(),
                filing.getFiledOn(),
                summary.getHeadline(),
                summary.getBullets(),
                summary.getSignificance(),
                filing.getPrimaryDocumentUrl());
    }

    private String unsubscribeUrl(Subscriber subscriber) {
        return "%s/api/subscriptions/unsubscribe?token=%s"
                .formatted(properties.siteUrl(), subscriber.getUnsubscribeToken());
    }
}
