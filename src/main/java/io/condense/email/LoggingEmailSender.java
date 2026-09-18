package io.condense.email;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "condense.email.provider", havingValue = "logging", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public String send(EmailMessage message) {
        String messageId = UUID.randomUUID().toString();
        log.info("Email not dispatched, no provider configured: to={} subject={} id={}",
                message.to(), message.subject(), messageId);
        log.debug("Body:\n{}", message.textBody());
        return messageId;
    }
}
