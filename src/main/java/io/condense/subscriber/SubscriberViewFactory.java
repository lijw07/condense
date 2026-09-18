package io.condense.subscriber;

import io.condense.company.Company;
import io.condense.company.CompanyRepository;
import io.condense.support.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriberViewFactory {

    private final SubscriberRepository subscribers;
    private final CompanyRepository companies;

    public SubscriberViewFactory(SubscriberRepository subscribers, CompanyRepository companies) {
        this.subscribers = subscribers;
        this.companies = companies;
    }

    @Transactional(readOnly = true)
    public SubscriberView forEmail(String email) {
        return toView(subscribers.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("No subscriber for " + email)));
    }

    @Transactional(readOnly = true)
    public SubscriberView forId(UUID subscriberId) {
        return toView(subscribers.findById(subscriberId)
                .orElseThrow(() -> new ResourceNotFoundException("No subscriber for that session")));
    }

    private SubscriberView toView(Subscriber subscriber) {
        return new SubscriberView(
                subscriber.getEmail(),
                subscriber.getStatus().name(),
                subscriber.getCadence().name(),
                tickersFor(subscriber));
    }

    private List<String> tickersFor(Subscriber subscriber) {
        return subscriber.getSubscriptions().stream()
                .map(Subscription::getCik)
                .map(cik -> companies.findById(cik).map(Company::getTicker).orElse(cik))
                .sorted()
                .toList();
    }
}
