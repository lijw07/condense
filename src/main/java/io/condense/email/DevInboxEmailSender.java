package io.condense.email;

import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "condense.email.provider", havingValue = "dev-inbox")
public class DevInboxEmailSender implements EmailSender {

    private final DevInbox inbox;

    public DevInboxEmailSender(DevInbox inbox) {
        this.inbox = inbox;
    }

    @Override
    public String send(EmailMessage message) {
        inbox.capture(message);
        return UUID.randomUUID().toString();
    }
}
