package io.condense.subscriber;

import io.condense.auth.MagicLinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptions;
    private final MagicLinkService magicLinks;
    private final SubscriberViewFactory views;

    public SubscriptionController(SubscriptionService subscriptions,
                                  MagicLinkService magicLinks,
                                  SubscriberViewFactory views) {
        this.subscriptions = subscriptions;
        this.magicLinks = magicLinks;
        this.views = views;
    }

    @PostMapping
    public ResponseEntity<SubscriberView> subscribe(@Valid @RequestBody SubscribeRequest request) {
        String email = subscriptions.subscribe(request.email(), request.tickers(), request.cadenceOrDefault());
        magicLinks.requestLink(email);
        return ResponseEntity.accepted().body(views.forEmail(email));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam String token) {
        subscriptions.unsubscribe(token);
        return ResponseEntity.noContent().build();
    }
}
