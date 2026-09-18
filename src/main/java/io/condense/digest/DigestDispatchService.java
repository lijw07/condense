package io.condense.digest;

import io.condense.auth.MagicLinkPurpose;
import io.condense.auth.MagicLinkService;
import io.condense.config.DigestProperties;
import io.condense.email.EmailMessage;
import io.condense.email.EmailRenderer;
import io.condense.email.EmailSender;
import io.condense.subscriber.Cadence;
import io.condense.subscriber.Subscriber;
import io.condense.subscriber.SubscriptionService;
import java.time.LocalDate;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DigestDispatchService {

    private static final Logger log = LoggerFactory.getLogger(DigestDispatchService.class);

    private final SubscriptionService subscriptions;
    private final MagicLinkService magicLinks;
    private final DigestBuilder digestBuilder;
    private final EmailRenderer emailRenderer;
    private final EmailSender emailSender;
    private final DeliveryLogRepository deliveries;
    private final DigestProperties properties;

    public DigestDispatchService(SubscriptionService subscriptions,
                                 MagicLinkService magicLinks,
                                 DigestBuilder digestBuilder,
                                 EmailRenderer emailRenderer,
                                 EmailSender emailSender,
                                 DeliveryLogRepository deliveries,
                                 DigestProperties properties) {
        this.subscriptions = subscriptions;
        this.magicLinks = magicLinks;
        this.digestBuilder = digestBuilder;
        this.emailRenderer = emailRenderer;
        this.emailSender = emailSender;
        this.deliveries = deliveries;
        this.properties = properties;
    }

    public void dispatch(Cadence cadence, LocalDate digestDate) {
        LocalDate since = cadence == Cadence.WEEKLY ? digestDate.minusDays(7) : digestDate.minusDays(1);
        for (Subscriber subscriber : subscriptions.activeSubscribersFor(cadence)) {
            try {
                dispatchTo(subscriber, digestDate, since);
            } catch (RuntimeException e) {
                log.error("Digest delivery failed for {}", subscriber.getId(), e);
                deliveries.save(DeliveryLog.failed(subscriber.getId(), digestDate, e.getMessage()));
            }
        }
    }

    @Transactional
    public DeliveryStatus dispatchTo(Subscriber subscriber, LocalDate digestDate, LocalDate since) {
        if (deliveries.existsBySubscriberIdAndDigestDate(subscriber.getId(), digestDate)) {
            return DeliveryStatus.SKIPPED_EMPTY;
        }
        String manageUrl = magicLinks.issueLinkUrl(subscriber, MagicLinkPurpose.SIGN_IN);
        DigestContent content = digestBuilder.build(subscriber, digestDate, since, manageUrl);
        if (content.isEmpty()) {
            deliveries.save(DeliveryLog.skipped(subscriber.getId(), digestDate));
            return DeliveryStatus.SKIPPED_EMPTY;
        }
        String html = emailRenderer.render("email/digest", Map.of(
                "content", content,
                "siteUrl", properties.siteUrl()));
        String messageId = emailSender.send(new EmailMessage(
                subscriber.getEmail(),
                subjectFor(content),
                html,
                plainTextFor(content),
                content.unsubscribeUrl()));
        deliveries.save(DeliveryLog.sent(subscriber.getId(), digestDate, messageId));
        return DeliveryStatus.SENT;
    }

    private String subjectFor(DigestContent content) {
        DigestItem lead = content.items().getFirst();
        if (content.items().size() == 1) {
            return "%s %s: %s".formatted(lead.ticker(), lead.formType(), lead.headline());
        }
        return "%s %s and %d more filings".formatted(lead.ticker(), lead.formType(), content.items().size() - 1);
    }

    private String plainTextFor(DigestContent content) {
        StringBuilder builder = new StringBuilder();
        for (DigestItem item : content.items()) {
            builder.append("%s %s (%s)\n".formatted(item.ticker(), item.formType(), item.filedOn()));
            builder.append(item.headline()).append('\n');
            item.bullets().forEach(bullet -> builder.append("  - ").append(bullet).append('\n'));
            builder.append(item.sourceUrl()).append("\n\n");
        }
        builder.append("Manage your tickers: ").append(content.manageUrl()).append('\n');
        builder.append("Unsubscribe: ").append(content.unsubscribeUrl()).append('\n');
        return builder.toString();
    }
}
