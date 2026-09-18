package io.condense.subscriber;

import io.condense.company.Company;
import io.condense.company.CompanyDirectory;
import io.condense.config.DigestProperties;
import io.condense.support.SubscriptionLimitExceededException;
import io.condense.support.ResourceNotFoundException;
import io.condense.support.TokenGenerator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private final SubscriberRepository subscribers;
    private final CompanyDirectory companies;
    private final TokenGenerator tokens;
    private final DigestProperties digestProperties;

    public SubscriptionService(SubscriberRepository subscribers,
                               CompanyDirectory companies,
                               TokenGenerator tokens,
                               DigestProperties digestProperties) {
        this.subscribers = subscribers;
        this.companies = companies;
        this.tokens = tokens;
        this.digestProperties = digestProperties;
    }

    @Transactional
    public String subscribe(String email, List<String> tickers, Cadence cadence) {
        String normalizedEmail = email.trim().toLowerCase();
        Subscriber subscriber = subscribers.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> subscribers.save(new Subscriber(normalizedEmail, tokens.newToken())));
        subscriber.changeCadence(cadence);
        tickers.forEach(ticker -> addTicker(subscriber, ticker));
        return subscriber.getEmail();
    }

    @Transactional
    public void addTicker(String email, String ticker) {
        addTicker(requireSubscriber(email), ticker);
    }

    @Transactional
    public void removeTicker(String email, String ticker) {
        requireSubscriber(email).unfollow(requireCompany(ticker).getCik());
    }

    @Transactional
    public void changeCadence(String email, Cadence cadence) {
        requireSubscriber(email).changeCadence(cadence);
    }

    @Transactional
    public void unsubscribeById(UUID subscriberId) {
        subscribers.findById(subscriberId)
                .orElseThrow(() -> new ResourceNotFoundException("No subscriber for that session"))
                .unsubscribe();
    }

    @Transactional
    public void unsubscribe(String unsubscribeToken) {
        Subscriber subscriber = subscribers.findByUnsubscribeToken(unsubscribeToken)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown unsubscribe token"));
        subscriber.unsubscribe();
    }

    @Transactional(readOnly = true)
    public List<Subscriber> activeSubscribersFor(Cadence cadence) {
        return subscribers.findAllByStatusAndCadence(SubscriberStatus.ACTIVE, cadence);
    }

    private void addTicker(Subscriber subscriber, String ticker) {
        Company company = requireCompany(ticker);
        if (subscriber.follows(company.getCik())) {
            return;
        }
        enforceTickerLimit(subscriber);
        subscriber.follow(new Subscription(subscriber, company.getCik()));
    }

    private void enforceTickerLimit(Subscriber subscriber) {
        if (subscriber.getSubscriptions().size() >= digestProperties.maxTickersPerSubscriber()) {
            throw new SubscriptionLimitExceededException(
                    "A subscriber can follow at most %d tickers"
                            .formatted(digestProperties.maxTickersPerSubscriber()));
        }
    }

    private Subscriber requireSubscriber(String email) {
        return subscribers.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("No subscriber for " + email));
    }

    private Company requireCompany(String ticker) {
        return companies.findByTicker(ticker)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown ticker " + ticker));
    }
}
