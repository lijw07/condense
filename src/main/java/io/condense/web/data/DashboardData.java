package io.condense.web.data;

import io.condense.company.Company;
import io.condense.company.CompanyDirectory;
import io.condense.config.DashboardProperties;
import io.condense.filing.Filing;
import io.condense.filing.FilingRepository;
import io.condense.filing.FilingSummary;
import io.condense.filing.FilingSummaryRepository;
import io.condense.web.view.BriefView;
import io.condense.web.view.CalendarMonthView;
import io.condense.web.view.FilingHistoryEntry;
import io.condense.web.view.KeyPointView;
import io.condense.web.view.LatestBriefView;
import io.condense.web.view.StockDetailView;
import io.condense.web.view.StockRowView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardData {

    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final CompanyDirectory directory;
    private final FilingRepository filings;
    private final FilingSummaryRepository summaries;
    private final DashboardProperties properties;

    public DashboardData(CompanyDirectory directory,
                         FilingRepository filings,
                         FilingSummaryRepository summaries,
                         DashboardProperties properties) {
        this.directory = directory;
        this.filings = filings;
        this.summaries = summaries;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<StockRowView> stocksFor(UUID subscriberId, List<String> tickers) {
        return tickers.stream().map(this::rowFor).toList();
    }

    @Transactional(readOnly = true)
    public Optional<StockDetailView> stockDetail(UUID subscriberId, String ticker) {
        return directory.findByTicker(ticker).map(company -> {
            List<Filing> recent = recentFilings(company.getCik());
            Map<UUID, FilingSummary> byFiling = summariesFor(recent);
            return new StockDetailView(
                    company.getTicker(),
                    company.getName(),
                    latestBrief(company.getTicker(), recent, byFiling).orElse(null),
                    recent.stream().map(this::historyEntry).toList());
        });
    }

    public List<CalendarMonthView> calendarFor(UUID subscriberId, List<String> tickers) {
        return List.of();
    }

    private StockRowView rowFor(String ticker) {
        Optional<Company> company = directory.findByTicker(ticker);
        if (company.isEmpty()) {
            return new StockRowView(ticker, ticker, false, null, null, null);
        }
        List<Filing> recent = recentFilings(company.get().getCik());
        Map<UUID, FilingSummary> byFiling = summariesFor(recent);
        Optional<Filing> summarized = recent.stream().filter(f -> byFiling.containsKey(f.getId())).findFirst();
        return new StockRowView(
                company.get().getTicker(),
                company.get().getName(),
                !recent.isEmpty(),
                summarized.map(Filing::getFormType).orElse(null),
                summarized.map(f -> SHORT_DATE.format(f.getFiledOn())).orElse(null),
                summarized.map(f -> latestPanel(f, byFiling.get(f.getId()))).orElse(null));
    }

    private List<Filing> recentFilings(String cik) {
        return filings.findAllByCikOrderByFiledOnDescIdDesc(cik, PageRequest.of(0, properties.filingHistoryLimit()));
    }

    private Map<UUID, FilingSummary> summariesFor(List<Filing> recent) {
        if (recent.isEmpty()) {
            return Map.of();
        }
        return summaries.findAllByFilingIdIn(recent.stream().map(Filing::getId).toList()).stream()
                .collect(Collectors.toMap(summary -> summary.getFiling().getId(), Function.identity(), (a, b) -> a));
    }

    private LatestBriefView latestPanel(Filing filing, FilingSummary summary) {
        return new LatestBriefView(
                filing.getFormType(),
                LONG_DATE.format(filing.getFiledOn()).toUpperCase(),
                summary.getHeadline(),
                String.join(" ", summary.getBullets()));
    }

    private Optional<BriefView> latestBrief(String ticker, List<Filing> recent, Map<UUID, FilingSummary> byFiling) {
        return recent.stream()
                .filter(filing -> byFiling.containsKey(filing.getId()))
                .findFirst()
                .map(filing -> briefFor(ticker, filing, byFiling.get(filing.getId())));
    }

    private BriefView briefFor(String ticker, Filing filing, FilingSummary summary) {
        List<String> bullets = summary.getBullets();
        return new BriefView(
                "",
                ticker,
                filing.getFormType(),
                LONG_DATE.format(filing.getFiledOn()).toUpperCase(),
                periodLabel(filing.getPeriodEnd()),
                summary.getHeadline(),
                IntStream.range(0, bullets.size())
                        .mapToObj(i -> new KeyPointView(i + 1, bullets.get(i), null))
                        .toList(),
                List.of(),
                filing.getPrimaryDocumentUrl(),
                0);
    }

    private FilingHistoryEntry historyEntry(Filing filing) {
        return new FilingHistoryEntry(
                filing.getFormType(),
                filing.getDescription(),
                SHORT_DATE.format(filing.getFiledOn()));
    }

    private String periodLabel(LocalDate periodEnd) {
        return periodEnd == null ? "" : LONG_DATE.format(periodEnd);
    }
}
