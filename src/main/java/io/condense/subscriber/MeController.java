package io.condense.subscriber;

import io.condense.auth.SubscriberSession;
import io.condense.auth.UnauthenticatedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final SubscriberSession subscriberSession;
    private final SubscriberViewFactory views;
    private final SubscriptionService subscriptions;
    private final SubscriberRepository subscribers;

    public MeController(SubscriberSession subscriberSession,
                        SubscriberViewFactory views,
                        SubscriptionService subscriptions,
                        SubscriberRepository subscribers) {
        this.subscriberSession = subscriberSession;
        this.views = views;
        this.subscriptions = subscriptions;
        this.subscribers = subscribers;
    }

    @GetMapping
    public SubscriberView current(HttpServletRequest request) {
        return views.forId(currentSubscriberId(request));
    }

    @PostMapping("/tickers")
    public SubscriberView addTicker(@Valid @RequestBody TickerRequest body, HttpServletRequest request) {
        String email = emailFor(request);
        subscriptions.addTicker(email, body.ticker());
        return views.forEmail(email);
    }

    @DeleteMapping("/tickers/{ticker}")
    public SubscriberView removeTicker(@PathVariable String ticker, HttpServletRequest request) {
        String email = emailFor(request);
        subscriptions.removeTicker(email, ticker);
        return views.forEmail(email);
    }

    @PutMapping("/cadence")
    public SubscriberView changeCadence(@Valid @RequestBody CadenceRequest body, HttpServletRequest request) {
        String email = emailFor(request);
        subscriptions.changeCadence(email, body.cadence());
        return views.forEmail(email);
    }

    @DeleteMapping
    public ResponseEntity<Void> unsubscribe(HttpServletRequest request) {
        subscriptions.unsubscribeById(currentSubscriberId(request));
        subscriberSession.end(request);
        return ResponseEntity.noContent().build();
    }

    private String emailFor(HttpServletRequest request) {
        return subscribers.findById(currentSubscriberId(request))
                .map(Subscriber::getEmail)
                .orElseThrow(() -> new UnauthenticatedException("Session no longer valid"));
    }

    private UUID currentSubscriberId(HttpServletRequest request) {
        return subscriberSession.currentSubscriberId(request)
                .orElseThrow(() -> new UnauthenticatedException("Sign in with the link sent to your email"));
    }
}
