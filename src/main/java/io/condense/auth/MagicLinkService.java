package io.condense.auth;

import io.condense.config.AuthProperties;
import io.condense.config.DigestProperties;
import io.condense.email.EmailMessage;
import io.condense.email.EmailRenderer;
import io.condense.email.EmailSender;
import io.condense.subscriber.Subscriber;
import io.condense.subscriber.SubscriberRepository;
import io.condense.support.ResourceNotFoundException;
import io.condense.support.TokenGenerator;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MagicLinkService {

    private static final Logger log = LoggerFactory.getLogger(MagicLinkService.class);

    private final MagicLinkTokenRepository tokens;
    private final SubscriberRepository subscribers;
    private final TokenGenerator tokenGenerator;
    private final EmailSender emailSender;
    private final EmailRenderer emailRenderer;
    private final DigestProperties digestProperties;
    private final AuthProperties authProperties;

    public MagicLinkService(MagicLinkTokenRepository tokens,
                            SubscriberRepository subscribers,
                            TokenGenerator tokenGenerator,
                            EmailSender emailSender,
                            EmailRenderer emailRenderer,
                            DigestProperties digestProperties,
                            AuthProperties authProperties) {
        this.tokens = tokens;
        this.subscribers = subscribers;
        this.tokenGenerator = tokenGenerator;
        this.emailSender = emailSender;
        this.emailRenderer = emailRenderer;
        this.digestProperties = digestProperties;
        this.authProperties = authProperties;
    }

    @Transactional
    public void requestLink(String email) {
        subscribers.findByEmailIgnoreCase(email.trim().toLowerCase())
                .ifPresentOrElse(
                        subscriber -> send(subscriber, purposeFor(subscriber)),
                        () -> log.info("Link requested for an address with no subscription"));
    }

    @Transactional
    public String issueLinkUrl(Subscriber subscriber, MagicLinkPurpose purpose) {
        return linkFor(mint(subscriber, purpose));
    }

    @Transactional
    public UUID verify(String rawToken) {
        MagicLinkToken token = tokens.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new ResourceNotFoundException("Link is invalid"));
        if (!token.isUsable(Instant.now())) {
            throw new ResourceNotFoundException("Link has expired or was already used");
        }
        token.consume();
        Subscriber subscriber = token.getSubscriber();
        if (token.getPurpose() == MagicLinkPurpose.CONFIRM_SUBSCRIPTION) {
            subscriber.confirm();
        }
        return subscriber.getId();
    }

    private void send(Subscriber subscriber, MagicLinkPurpose purpose) {
        String link = linkFor(mint(subscriber, purpose));
        String html = emailRenderer.render("email/magic-link", Map.of(
                "link", link,
                "heading", headingFor(purpose),
                "intro", introFor(purpose),
                "buttonLabel", buttonLabelFor(purpose),
                "lifetimeHours", authProperties.linkLifetime().toHours()));
        emailSender.send(new EmailMessage(
                subscriber.getEmail(),
                subjectFor(purpose),
                html,
                "%s\n\n%s\n\nThis link expires in %d hours."
                        .formatted(headingFor(purpose), link, authProperties.linkLifetime().toHours()),
                unsubscribeUrl(subscriber)));
    }

    private String mint(Subscriber subscriber, MagicLinkPurpose purpose) {
        String rawToken = tokenGenerator.newToken();
        tokens.save(new MagicLinkToken(
                subscriber,
                tokenGenerator.hash(rawToken),
                purpose,
                Instant.now().plus(authProperties.linkLifetime())));
        return rawToken;
    }

    private MagicLinkPurpose purposeFor(Subscriber subscriber) {
        return subscriber.isActive() ? MagicLinkPurpose.SIGN_IN : MagicLinkPurpose.CONFIRM_SUBSCRIPTION;
    }

    private String headingFor(MagicLinkPurpose purpose) {
        return purpose == MagicLinkPurpose.CONFIRM_SUBSCRIPTION
                ? "Confirm your Condense subscription"
                : "Open your Condense settings";
    }

    private String introFor(MagicLinkPurpose purpose) {
        return purpose == MagicLinkPurpose.CONFIRM_SUBSCRIPTION
                ? "Confirm this address and your stocks dashboard opens, where you can pick tickers "
                        + "and choose what lands in your inbox."
                : "This opens your dashboard, where you can change your tickers and preferences.";
    }

    private String buttonLabelFor(MagicLinkPurpose purpose) {
        return purpose == MagicLinkPurpose.CONFIRM_SUBSCRIPTION ? "Verify my email" : "Open my dashboard";
    }

    private String subjectFor(MagicLinkPurpose purpose) {
        return purpose == MagicLinkPurpose.CONFIRM_SUBSCRIPTION
                ? "Confirm your Condense subscription"
                : "Your Condense sign-in link";
    }

    private String linkFor(String rawToken) {
        return "%s/auth/verify?token=%s".formatted(digestProperties.siteUrl(), rawToken);
    }

    private String unsubscribeUrl(Subscriber subscriber) {
        return "%s/api/subscriptions/unsubscribe?token=%s"
                .formatted(digestProperties.siteUrl(), subscriber.getUnsubscribeToken());
    }
}
