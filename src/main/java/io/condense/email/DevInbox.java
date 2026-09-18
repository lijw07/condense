package io.condense.email;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "condense.email.provider", havingValue = "dev-inbox")
public class DevInbox {

    private static final int CAPACITY = 50;

    private final Deque<CapturedEmail> captured = new ArrayDeque<>();

    public synchronized void capture(EmailMessage message) {
        captured.addFirst(new CapturedEmail(Instant.now(), message));
        while (captured.size() > CAPACITY) {
            captured.removeLast();
        }
    }

    public synchronized List<CapturedEmail> recent() {
        return List.copyOf(captured);
    }

    public synchronized void clear() {
        captured.clear();
    }

    public record CapturedEmail(Instant receivedAt, EmailMessage message) {

        private static final Pattern URL = Pattern.compile("https?://\\S+");

        public String primaryLink() {
            Matcher matcher = URL.matcher(message.textBody());
            return matcher.find() ? matcher.group() : null;
        }
    }
}
